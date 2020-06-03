package io.netty.chanel.quic.tls;

import io.netty.chanel.quic.tls.aead.AEAD;
import io.netty.chanel.quic.tls.aead.AEADs;
import io.netty.chanel.quic.tls.aead.HandshakeAEAD;
import io.netty.chanel.quic.tls.aead.OneRttAEAD;
import io.netty.chanel.quic.tls.extensions.ExtensionType;
import io.netty.chanel.quic.tls.extensions.KeyShare;
import io.netty.chanel.quic.tls.extensions.SupportedVersions;
import io.netty.chanel.quic.tls.extensions.TransportParameters;
import io.netty.chanel.quic.tls.messages.ClientFinished;
import io.netty.chanel.quic.tls.messages.ClientHello;
import io.netty.chanel.quic.tls.messages.ServerHandshake.EncryptedExtensions;
import io.netty.chanel.quic.tls.messages.ServerHandshake.ServerCertificate;
import io.netty.chanel.quic.tls.messages.ServerHandshake.ServerCertificateVerify;
import io.netty.chanel.quic.tls.messages.ServerHandshake.ServerHandshakeFinished;
import io.netty.chanel.quic.tls.messages.ServerHello;
import io.netty.chanel.quic.utils.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.util.Optional;

import static io.netty.chanel.quic.tls.aead.Labels.CLIENT_HANDSHAKE_TRAFFIC_SECRET;

public class ClientTlsSession {

  public static class CertificateInvalidException extends Exception {}

  private final Logger log = LoggerFactory.getLogger(ClientTlsSession.class);

  private final TransportParameters transportParameters;

  private final AEADs aeads;
  private final KeyExchange kek;
  private final CertificateValidator certificateValidator;

  private ByteBuf handshakeBuffer;
  private byte[] clientHello;
  private byte[] serverHello;
  private byte[] handshakeSecret;

  public ClientTlsSession(
      final AEAD initialAEAD,
      final TransportParameters transportParameters,
      final CertificateValidator certificateValidator) {
    this.transportParameters = transportParameters;

    aeads = new AEADs(initialAEAD);
    this.certificateValidator = certificateValidator;
    kek = KeyExchange.generate(Group.X25519);
    handshakeBuffer = Unpooled.buffer(); // replace with position keeping buffer
  }

  public byte[] startHandshake() {
    if (clientHello != null) {
      throw new IllegalStateException("Already started");
    }

    final ClientHello ch = ClientHello.defaults(kek, transportParameters);
    clientHello = Bytes.write(bb -> ch.write(bb, true));
    return clientHello;
  }

  public AEAD handleServerHello(final byte[] msg) {
    if (clientHello == null) {
      throw new IllegalStateException("Not started");
    }

    serverHello = msg;

    final ByteBuf bb = Unpooled.wrappedBuffer(msg);
    final ServerHello hello = ServerHello.parse(bb, true);

    final SupportedVersions version =
        (SupportedVersions)
            hello
                .geExtension(ExtensionType.SUPPORTED_VERSIONS)
                .orElseThrow(IllegalArgumentException::new);
    if (!version.equals(SupportedVersions.TLS13)) {
      throw new IllegalArgumentException("Illegal version");
    }

    final KeyShare keyShareExtension =
        (KeyShare)
            hello.geExtension(ExtensionType.KEY_SHARE).orElseThrow(IllegalArgumentException::new);
    final byte[] peerPublicKey = keyShareExtension.getKey(Group.X25519).get();
    final byte[] sharedSecret = kek.generateSharedSecret(peerPublicKey);

    final byte[] helloHash = Hash.sha256(clientHello, serverHello);

    handshakeSecret = HKDF.calculateHandshakeSecret(sharedSecret);

    return HandshakeAEAD.create(handshakeSecret, helloHash, true);
  }

  public synchronized Optional<HandshakeResult> handleHandshake(final byte[] msg)
      throws CertificateInvalidException {
    if (clientHello == null || serverHello == null) {
      throw new IllegalStateException("Got handshake in unexpected state");
    }

    handshakeBuffer.writeBytes(msg);

    handshakeBuffer.markReaderIndex();
    try {
      final int pos = handshakeBuffer.readerIndex();
      EncryptedExtensions.parse(handshakeBuffer, true);
      final ServerCertificate sc = ServerCertificate.parse(handshakeBuffer);

      final byte[] scvBytes = new byte[handshakeBuffer.readerIndex() - pos];
      handshakeBuffer.resetReaderIndex();
      handshakeBuffer.readBytes(scvBytes);

      final ServerCertificateVerify scv = ServerCertificateVerify.parse(handshakeBuffer);

      validateServerCertificateVerify(sc, scv, scvBytes);

      final byte[] finBytes = new byte[handshakeBuffer.readerIndex() - pos];
      handshakeBuffer.resetReaderIndex();
      handshakeBuffer.readBytes(finBytes);

      final ServerHandshakeFinished fin = ServerHandshakeFinished.parse(handshakeBuffer);

      final byte[] helloHash = Hash.sha256(clientHello, serverHello);
      validateServerFinish(fin, helloHash, finBytes);

      if (!certificateValidator.validate(sc.getServerCertificates())) {
        throw new CertificateInvalidException();
      }

      handshakeBuffer.resetReaderIndex();

      final byte[] hs = Bytes.drainToArray(handshakeBuffer);
      handshakeBuffer = Unpooled.buffer();

      final byte[] handshakeHash = Hash.sha256(clientHello, serverHello, hs);

      final AEAD aead = OneRttAEAD.create(handshakeSecret, handshakeHash, true);

      // TODO dedup
      final byte[] clientHandshakeTrafficSecret =
          HKDF.expandLabel(handshakeSecret, CLIENT_HANDSHAKE_TRAFFIC_SECRET, helloHash, 32);

      final ClientFinished clientFinished =
          ClientFinished.create(clientHandshakeTrafficSecret, handshakeHash);

      final byte[] b = Bytes.write(clientFinished);

      return Optional.of(new HandshakeResult(b, aead));
    } catch (final IndexOutOfBoundsException e) {
      // wait for more data
      log.debug("Need more data, waiting...");
      handshakeBuffer.resetReaderIndex();

      return Optional.empty();
    }
  }

  private void validateServerFinish(
      final ServerHandshakeFinished fin, final byte[] helloHash, final byte[] finBytes) {
    // verify server fin
    final byte[] finishedHash = Hash.sha256(clientHello, serverHello, finBytes);

    final byte[] serverHandshakeTrafficSecret =
        HKDF.expandLabel(handshakeSecret, "s hs traffic", helloHash, 32);

    final boolean valid =
        VerifyData.verify(
            fin.getVerificationData(), serverHandshakeTrafficSecret, finishedHash, false);
    if (!valid) {
      throw new RuntimeException("Server verification data not valid");
    }
  }

  private void validateServerCertificateVerify(
      final ServerCertificate sc, final ServerCertificateVerify scv, final byte[] handshakeData) {
    final byte[] toVerify = Hash.sha256(clientHello, serverHello, handshakeData);

    final byte[] serverSig = scv.getSignature();

    final PublicKey serverKey = sc.getAsCertificiates().get(0).getPublicKey();

    final boolean valid = CertificateVerify.verify(serverSig, toVerify, serverKey, false);
    if (!valid) {
      throw new RuntimeException("Invalid server certificate verify");
    }
  }

  public boolean available(final EncryptionLevel encLevel) {
    return aeads.available(encLevel);
  }

  public AEAD getAEAD(final EncryptionLevel level) {
    return aeads.get(level);
  }

  public void setHandshakeAead(final AEAD handshakeAead) {
    aeads.setHandshakeAead(handshakeAead);
  }

  public void setOneRttAead(final AEAD oneRttAead) {
    aeads.setOneRttAead(oneRttAead);
  }

  public void unsetInitialAead() {
    aeads.unsetInitialAead();
  }

  public void unsetHandshakeAead() {
    aeads.unsetHandshakeAead();
  }

  public static class HandshakeResult {
    private final byte[] fin;
    private final AEAD oneRttAead;

    public HandshakeResult(final byte[] fin, final AEAD oneRttAead) {
      this.fin = fin;
      this.oneRttAead = oneRttAead;
    }

    public byte[] getFin() {
      return fin;
    }

    public AEAD getOneRttAead() {
      return oneRttAead;
    }
  }
}

package io.netty.chanel.quic.tls;

import com.google.common.base.Preconditions;
import io.netty.chanel.quic.tls.aead.AEAD;
import io.netty.chanel.quic.tls.aead.AEADs;
import io.netty.chanel.quic.tls.aead.HandshakeAEAD;
import io.netty.chanel.quic.tls.aead.OneRttAEAD;
import io.netty.chanel.quic.tls.extensions.*;
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

import java.security.PrivateKey;
import java.util.List;

import static io.netty.chanel.quic.utils.Bytes.peekToArray;
import static java.util.Objects.requireNonNull;

public class ServerTlsSession {

  private final TransportParameters transportParameters;

  private final AEADs aeads;
  private final KeyExchange kek;

  private final PrivateKey privateKey;
  private final List<byte[]> certificates;
  private byte[] clientHello;
  private byte[] serverHello;
  private byte[] handshake;
  private byte[] handshakeSecret;

  public ServerTlsSession(
      final AEAD initialAEAD,
      final TransportParameters transportParameters,
      final List<byte[]> certificates,
      final PrivateKey privateKey) {
    this.transportParameters = transportParameters;
    Preconditions.checkArgument(!certificates.isEmpty());

    aeads = new AEADs(initialAEAD);
    this.privateKey = privateKey;
    this.certificates = requireNonNull(certificates);
    this.kek = KeyExchange.generate(Group.X25519);
  }

  public ServerHelloAndHandshake handleClientHello(final byte[] msg) {
    clientHello = msg;

    final ClientHello ch = ClientHello.parse(msg, false);

    // verify expected extensions
    final SupportedVersions versions =
        (SupportedVersions)
            ch.getExtension(ExtensionType.SUPPORTED_VERSIONS)
                .orElseThrow(IllegalArgumentException::new);
    if (!versions.getVersions().contains(SupportedVersion.TLS13)) {
      throw new IllegalArgumentException("Illegal version");
    }

    final KeyShare keyShareExtension =
        (KeyShare)
            ch.getExtension(ExtensionType.KEY_SHARE).orElseThrow(IllegalArgumentException::new);

    // create ServerHello
    serverHello = Bytes.write(ServerHello.defaults(kek, transportParameters));

    final ByteBuf handshakeBB = Unpooled.buffer();

    // TODO decide on what parameters to send where
    final EncryptedExtensions ee = EncryptedExtensions.defaults(transportParameters);
    ee.write(handshakeBB);

    final ServerCertificate sc = new ServerCertificate(new byte[0], certificates);
    sc.write(handshakeBB);

    // create server cert verification
    final byte[] toVerify = peekToArray(handshakeBB);

    final byte[] verificationSig =
        CertificateVerify.sign(Hash.sha256(clientHello, serverHello, toVerify), privateKey, false);

    final ServerCertificateVerify scv = new ServerCertificateVerify(2052, verificationSig);
    scv.write(handshakeBB);

    // create server finished
    final byte[] peerPublicKey = keyShareExtension.getKey(Group.X25519).get();
    final byte[] sharedSecret = kek.generateSharedSecret(peerPublicKey);
    handshakeSecret = HKDF.calculateHandshakeSecret(sharedSecret);
    final byte[] helloHash = Hash.sha256(clientHello, serverHello);

    // create handshake AEAD
    final AEAD handshakeAEAD = HandshakeAEAD.create(handshakeSecret, helloHash, false);

    final byte[] serverHandshakeTrafficSecret =
        HKDF.expandLabel(handshakeSecret, "s hs traffic", helloHash, 32);

    // finished_hash = SHA256(Client Hello ... Server Cert Verify)
    final byte[] finishedHash = Hash.sha256(clientHello, serverHello, peekToArray(handshakeBB));

    final byte[] verifyData = VerifyData.create(serverHandshakeTrafficSecret, finishedHash);

    final ServerHandshakeFinished fin = new ServerHandshakeFinished(verifyData);
    fin.write(handshakeBB);

    // create 1-RTT AEAD
    handshake = Bytes.drainToArray(handshakeBB);

    final byte[] handshakeHash = Hash.sha256(clientHello, serverHello, handshake);
    final AEAD oneRttAEAD = OneRttAEAD.create(handshakeSecret, handshakeHash, false);

    return new ServerHelloAndHandshake(serverHello, handshake, handshakeAEAD, oneRttAEAD);
  }

  public synchronized void handleClientFinished(final byte[] msg) {
    if (clientHello == null || serverHello == null || handshake == null) {
      throw new IllegalStateException("Got handshake in unexpected state");
    }

    final ByteBuf bb = Unpooled.wrappedBuffer(msg);
    final ClientFinished fin = ClientFinished.parse(bb);

    final byte[] helloHash = Hash.sha256(clientHello, serverHello);

    final byte[] clientHandshakeTrafficSecret =
        HKDF.expandLabel(handshakeSecret, "c hs traffic", helloHash, 32);

    final byte[] handshakeHash = Hash.sha256(clientHello, serverHello, handshake);

    final boolean valid =
        VerifyData.verify(
            fin.getVerificationData(), clientHandshakeTrafficSecret, handshakeHash, false);

    if (!valid) {
      throw new RuntimeException("Invalid client verification");
    }
  }

  public AEAD getAEAD(final EncryptionLevel level) {
    return aeads.get(level);
  }

  public void setHandshakeAead(final AEAD handshakeAEAD) {
    aeads.setHandshakeAead(handshakeAEAD);
  }

  public void setOneRttAead(final AEAD oneRttAEAD) {
    aeads.setOneRttAead(oneRttAEAD);
  }

  public boolean available(final EncryptionLevel level) {
    return aeads.available(level);
  }

  public void unsetInitialAead() {
    aeads.unsetInitialAead();
  }

  public void unsetHandshakeAead() {
    aeads.unsetHandshakeAead();
  }

  public static class ServerHelloAndHandshake {

    private final byte[] serverHello;
    private final byte[] serverHandshake;

    private final AEAD handshakeAEAD;
    private final AEAD oneRttAEAD;

    public ServerHelloAndHandshake(
        final byte[] serverHello,
        final byte[] serverHandshake,
        final AEAD handshakeAEAD,
        final AEAD oneRttAEAD) {
      this.serverHello = serverHello;
      this.serverHandshake = serverHandshake;
      this.handshakeAEAD = handshakeAEAD;
      this.oneRttAEAD = oneRttAEAD;
    }

    public byte[] getServerHello() {
      return serverHello;
    }

    public byte[] getServerHandshake() {
      return serverHandshake;
    }

    public AEAD getHandshakeAEAD() {
      return handshakeAEAD;
    }

    public AEAD getOneRttAEAD() {
      return oneRttAEAD;
    }
  }
}

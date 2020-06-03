package io.netty.chanel.quic.tls.aead;

import io.netty.chanel.quic.tls.HKDF;

import static io.netty.chanel.quic.tls.aead.Labels.*;

public class HandshakeAEAD {

  public static AEAD create(
      final byte[] handshakeSecret, final byte[] helloHash, final boolean isClient) {

    // client_handshake_traffic_secret = hkdf-Expand-Label(
    //    key = handshake_secret,
    //    label = "c hs traffic",
    //    context = hello_hash,
    //    len = 32)
    final byte[] clientHandshakeTrafficSecret =
        HKDF.expandLabel(handshakeSecret, CLIENT_HANDSHAKE_TRAFFIC_SECRET, helloHash, 32);

    // server_handshake_traffic_secret = hkdf-Expand-Label(
    //    key = handshake_secret,
    //    label = "s hs traffic",
    //    context = hello_hash,
    //    len = 32)
    final byte[] serverHandshakeTrafficSecret =
        HKDF.expandLabel(handshakeSecret, SERVER_HANDSHAKE_TRAFFIC_SECRET, helloHash, 32);

    // client_handshake_key = hkdf-Expand-Label(
    //    key = client_handshake_traffic_secret,
    //    label = "key",
    //    context = "",
    //    len = 16)
    final byte[] clientHandshakeKey =
        HKDF.expandLabel(clientHandshakeTrafficSecret, KEY, new byte[0], 16);

    // server_handshake_key = hkdf-Expand-Label(
    //    key = server_handshake_traffic_secret,
    //    label = "key",
    //    context = "",
    //    len = 16)
    final byte[] serverHandshakeKey =
        HKDF.expandLabel(serverHandshakeTrafficSecret, KEY, new byte[0], 16);

    // client_handshake_iv = hkdf-Expand-Label(
    //    key = client_handshake_traffic_secret,
    //    label = "iv",
    //    context = "",
    //    len = 12)
    final byte[] clientHandshakeIV =
        HKDF.expandLabel(clientHandshakeTrafficSecret, IV, new byte[0], 12);

    // server_handshake_iv = hkdf-Expand-Label(
    //    key = server_handshake_traffic_secret,
    //    label = "iv",
    //    context = "",
    //    len = 12)
    final byte[] serverHandshakeIV =
        HKDF.expandLabel(serverHandshakeTrafficSecret, IV, new byte[0], 12);

    final byte[] clientPnKey =
        HKDF.expandLabel(clientHandshakeTrafficSecret, HP_KEY, new byte[0], 16);
    final byte[] serverPnKey =
        HKDF.expandLabel(serverHandshakeTrafficSecret, HP_KEY, new byte[0], 16);

    if (isClient) {
      return new AEAD(
          clientHandshakeKey,
          serverHandshakeKey,
          clientHandshakeIV,
          serverHandshakeIV,
          clientPnKey,
          serverPnKey);
    } else {
      return new AEAD(
          serverHandshakeKey,
          clientHandshakeKey,
          serverHandshakeIV,
          clientHandshakeIV,
          serverPnKey,
          clientPnKey);
    }
  }
}

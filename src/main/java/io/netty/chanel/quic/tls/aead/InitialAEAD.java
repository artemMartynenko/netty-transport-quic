package io.netty.chanel.quic.tls.aead;

import io.netty.chanel.quic.tls.HKDF;
import io.netty.chanel.quic.utils.Hex;

import static io.netty.chanel.quic.tls.aead.Labels.*;

public class InitialAEAD {

  private static final byte[] QUIC_VERSION_1_SALT =
      Hex.dehex("ef4fb0abb47470c41befcf8031334fae485e09a0");

  public static AEAD create(final byte[] keyMaterial, final boolean isClient) {
    final byte[] initialSecret = HKDF.extract(QUIC_VERSION_1_SALT, keyMaterial);

    final int length = 32;

    final byte[] clientSecret = expand(initialSecret, CLIENT_INITIAL, length);
    final byte[] serverSecret = expand(initialSecret, SERVER_INITIAL, length);

    final byte[] mySecret;
    final byte[] otherSecret;
    if (isClient) {
      mySecret = clientSecret;
      otherSecret = serverSecret;
    } else {
      mySecret = serverSecret;
      otherSecret = clientSecret;
    }

    final byte[] myKey = expand(mySecret, KEY, 16);
    final byte[] myIV = expand(mySecret, IV, 12);

    final byte[] otherKey = expand(otherSecret, KEY, 16);
    final byte[] otherIV = expand(otherSecret, IV, 12);

    final byte[] myPnKey = expand(mySecret, HP_KEY, 16);
    final byte[] otherPnKey = expand(otherSecret, HP_KEY, 16);

    return new AEAD(myKey, otherKey, myIV, otherIV, myPnKey, otherPnKey);
  }

  private static byte[] expand(final byte[] secret, final String label, final int length) {
    return HKDF.expandLabel(secret, label, new byte[0], length);
  }
}

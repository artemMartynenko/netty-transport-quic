package io.netty.chanel.quic.tls;

import java.security.MessageDigest;

public class CryptoEquals {

  public static boolean isEqual(final byte[] a, final byte[] b) {
    return MessageDigest.isEqual(a, b);
  }
}

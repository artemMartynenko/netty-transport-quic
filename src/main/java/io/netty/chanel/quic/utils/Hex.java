package io.netty.chanel.quic.utils;

import com.google.common.io.BaseEncoding;

public class Hex {

  private static final BaseEncoding HEX = BaseEncoding.base16().lowerCase();

  public static String hex(final byte[] b) {
    return HEX.encode(b);
  }

  public static String hex(final byte b) {
    return HEX.encode(new byte[] {b});
  }

  public static byte[] dehex(final String s) {
    return HEX.decode(s.replace(" ", "").toLowerCase());
  }
}

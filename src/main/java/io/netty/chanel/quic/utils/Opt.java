package io.netty.chanel.quic.utils;

import java.util.Optional;

public class Opt {
  public static String toString(final Optional<?> opt) {
    if (opt.isPresent()) {
      return "[" + opt.get().toString() + "]";
    } else {
      return "[]";
    }
  }

  public static String toStringBytes(final Optional<byte[]> opt) {
    if (opt.isPresent()) {
      return "[" + Hex.hex(opt.get()) + "]";
    } else {
      return "[]";
    }
  }
}

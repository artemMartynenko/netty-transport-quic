package io.netty.chanel.quic.base;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import io.netty.chanel.quic.utils.Bytes;
import io.netty.chanel.quic.utils.Varint;

public class PacketNumber {

  public static long parse(final byte[] b) {
    final byte[] pad = new byte[4 - b.length];
    final byte[] bs = Bytes.concat(pad, b);

    return Ints.fromByteArray(bs);
  }

  public static final long MIN = 0;

  public static long validate(final long number) {
    Preconditions.checkArgument(number >= 0);
    Preconditions.checkArgument(number <= Varint.MAX);
    return number;
  }

  public static long next(final long number) {
    return number + 1;
  }

  public static int getLength(final long number) {
    return 4; // TODO
  }

  public static byte[] write(final long number, final int length) {
    final byte[] b = new byte[length];
    for (int j = length; j > 0; j--) {
      b[length - j] = (byte) ((number >> (8 * (j - 1))) & 0xFF);
    }
    return b;
  }

  private PacketNumber() {}
}

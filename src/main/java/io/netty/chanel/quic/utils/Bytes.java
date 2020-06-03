package io.netty.chanel.quic.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;
import java.util.Collection;

public class Bytes {

  public static byte[] drainToArray(final ByteBuf bb) {
    try {
      final byte[] b = new byte[bb.readableBytes()];
      bb.readBytes(b);
      return b;
    } finally {
      bb.release();
    }
  }

  public static byte[] peekToArray(final ByteBuf bb) {
    final byte[] b = new byte[bb.readableBytes()];
    bb.markReaderIndex();
    bb.readBytes(b);
    bb.resetReaderIndex();
    return b;
  }

  public static byte[] concat(final byte[]... bs) {
    if (bs.length == 0) {
      return new byte[0];
    } else if (bs.length == 1) {
      return bs[0];
    }
    return com.google.common.primitives.Bytes.concat(bs);
  }

  public static int read24(final ByteBuf bb) {
    final byte[] b = new byte[3];
    bb.readBytes(b);
    return (b[0] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[2] & 0xFF);
  }

  public static void write24(final ByteBuf bb, final int value) {
    bb.writeByte((value >> 16) & 0xFF);
    bb.writeByte((value >> 8) & 0xFF);
    bb.writeByte(value & 0xFF);
  }

  public static void set24(final ByteBuf bb, final int position, final int value) {
    bb.setByte(position, (value >> 16) & 0xFF);
    bb.setByte(position + 1, (value >> 8) & 0xFF);
    bb.setByte(position + 2, value & 0xFF);
  }

  public static byte[] write(final Writeable... writeables) {
    return write(Arrays.asList(writeables));
  }

  public static byte[] write(final Collection<Writeable> writeables) {
    final ByteBuf bb = Unpooled.buffer();
    for (final Writeable writeable : writeables) {
      writeable.write(bb);
    }
    return drainToArray(bb);
  }
}

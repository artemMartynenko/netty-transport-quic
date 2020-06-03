package io.netty.chanel.quic.base.frames;

import io.netty.buffer.ByteBuf;
import io.netty.chanel.quic.utils.Hex;
import io.netty.chanel.quic.utils.Varint;

public class NewToken extends Frame {

  public static NewToken parse(final ByteBuf bb) {
    final byte type = bb.readByte();
    if (type != FrameType.NEW_TOKEN.getType()) {
      throw new IllegalArgumentException("Illegal frame type");
    }

    final int len = Varint.readAsInt(bb);
    final byte[] token = new byte[len];
    bb.readBytes(token);

    return new NewToken(token);
  }

  private final byte[] token;

  public NewToken(final byte[] token) {
    super(FrameType.NEW_TOKEN);

    this.token = token;
  }

  public byte[] getToken() {
    return token;
  }

  @Override
  public void write(final ByteBuf bb) {
    bb.writeByte(getType().getType());

    Varint.write(token.length, bb);
    bb.writeBytes(token);
  }

  @Override
  public String toString() {
    return "NewToken{" + Hex.hex(token) + '}';
  }
}

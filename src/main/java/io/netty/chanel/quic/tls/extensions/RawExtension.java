package io.netty.chanel.quic.tls.extensions;

import io.netty.chanel.quic.utils.Bytes;
import io.netty.chanel.quic.utils.Hex;
import io.netty.buffer.ByteBuf;

public class RawExtension implements Extension {

  public static RawExtension parse(final ExtensionType type, final ByteBuf bb) {
    final byte[] b = Bytes.peekToArray(bb);
    return new RawExtension(type, b);
  }

  private final ExtensionType type;
  private final byte[] data;

  public RawExtension(final ExtensionType type, final byte[] data) {
    this.type = type;
    this.data = data;
  }

  @Override
  public ExtensionType getType() {
    return type;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public void write(final ByteBuf bb, final boolean isClient) {
    bb.writeBytes(data);
  }

  @Override
  public String toString() {
    return "RawExtension{" + "type=" + type + ", data=" + Hex.hex(data) + '}';
  }
}

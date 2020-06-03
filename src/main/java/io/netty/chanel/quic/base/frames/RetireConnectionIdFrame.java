package io.netty.chanel.quic.base.frames;

import io.netty.buffer.ByteBuf;
import io.netty.chanel.quic.utils.Varint;

public class RetireConnectionIdFrame extends Frame {

  private final long sequenceNumber;

  public static RetireConnectionIdFrame parse(final ByteBuf bb) {
    final byte type = bb.readByte();
    if (type != FrameType.RETIRE_CONNECTION_ID.getType()) {
      throw new IllegalArgumentException("Illegal frame type");
    }

    final long sequenceNumber = Varint.readAsLong(bb);

    return new RetireConnectionIdFrame(sequenceNumber);
  }

  public RetireConnectionIdFrame(final long sequenceNumber) {
    super(FrameType.RETIRE_CONNECTION_ID);
    this.sequenceNumber = sequenceNumber;
  }

  public long getSequenceNumber() {
    return sequenceNumber;
  }

  @Override
  public void write(final ByteBuf bb) {
    bb.writeByte(getType().getType());

    Varint.write(sequenceNumber, bb);
  }
}

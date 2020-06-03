package io.netty.chanel.quic.base.frames;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.chanel.quic.utils.Writeable;

public abstract class Frame implements Writeable {

  public static Frame parse(final ByteBuf bb) {
    final byte typeByte = bb.getByte(bb.readerIndex());
    final FrameType type = FrameType.fromByte(typeByte);

    if (type == FrameType.STREAM) {
      return StreamFrame.parse(bb);
    } else if (type == FrameType.PADDING) {
      return PaddingFrame.parse(bb);
    } else if (type == FrameType.CRYPTO) {
      return CryptoFrame.parse(bb);
    } else if (type == FrameType.ACK) {
      return AckFrame.parse(bb);
    } else if (type == FrameType.PING) {
      return PingFrame.parse(bb);
    } else if (type == FrameType.RETIRE_CONNECTION_ID) {
      return RetireConnectionIdFrame.parse(bb);
    } else if (type == FrameType.RESET_STREAM) {
      return ResetStreamFrame.parse(bb);
    } else if (type == FrameType.CONNECTION_CLOSE) {
      return ConnectionCloseFrame.parse(bb);
    } else if (type == FrameType.APPLICATION_CLOSE) {
      return ApplicationCloseFrame.parse(bb);
    } else if (type == FrameType.MAX_STREAM_DATA) {
      return MaxStreamDataFrame.parse(bb);
    } else if (type == FrameType.MAX_DATA) {
      return MaxDataFrame.parse(bb);
    } else if (type == FrameType.MAX_STREAMS) {
      return MaxStreamsFrame.parse(bb);
    } else if (type == FrameType.STREAM_DATA_BLOCKED) {
      return StreamDataBlockedFrame.parse(bb);
    } else if (type == FrameType.DATA_BLOCKED) {
      return DataBlockedFrame.parse(bb);
    } else if (type == FrameType.STREAMS_BLOCKED) {
      return StreamsBlockedFrame.parse(bb);
    } else if (type == FrameType.NEW_TOKEN) {
      return NewToken.parse(bb);
    } else {
      throw new RuntimeException("Unknown frame type " + type);
    }
  }

  private final FrameType type;

  public Frame(final FrameType type) {
    this.type = type;
  }

  public FrameType getType() {
    return type;
  }

  public int calculateLength() {
    // TODO implement in subclasses, this is slow
    final ByteBuf bb = Unpooled.buffer();
    try {
      write(bb);
      return bb.writerIndex();
    } finally {
      bb.release();
    }
  }

  public abstract void write(ByteBuf bb);
}

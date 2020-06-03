package io.netty.chanel.quic.base.frames;

import io.netty.buffer.ByteBuf;
import io.netty.chanel.quic.base.StreamId;
import io.netty.chanel.quic.utils.Varint;

import java.util.Arrays;

public class StreamFrame extends Frame {

  public static StreamFrame parse(final ByteBuf bb) {
    final byte firstByte = bb.readByte();

    final boolean off = (firstByte & 0x04) == 0x04;
    final boolean len = (firstByte & 0x02) == 0x02;
    final boolean fin = (firstByte & 0x01) == 0x01;

    final long streamId = StreamId.parse(bb);
    final long offset;
    if (off) {
      offset = Varint.readAsLong(bb);
    } else {
      offset = 0;
    }

    final int length;
    if (len) {
      length = Varint.readAsInt(bb);
    } else {
      length = bb.readableBytes();
    }

    final byte[] data = new byte[length];
    bb.readBytes(data);

    return new StreamFrame(streamId, offset, fin, data);
  }

  private final long streamId;
  private final long offset;
  private final boolean fin;
  private final byte[] data;

  public StreamFrame(final long streamId, final long offset, final boolean fin, final byte[] data) {
    super(FrameType.STREAM);
    this.streamId = StreamId.validate(streamId);
    this.offset = offset;
    this.fin = fin;
    this.data = data;
  }

  public long getStreamId() {
    return streamId;
  }

  public long getOffset() {
    return offset;
  }

  public boolean isFin() {
    return fin;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public void write(final ByteBuf bb) {
    byte type = getType().getType();
    if (offset > 0) {
      type = (byte) (type | 0x04);
    }
    if (isFin()) {
      type = (byte) (type | 0x01);
    }
    // TODO only set len when needed
    type = (byte) (type | 0x02);

    bb.writeByte(type);
    StreamId.write(bb, streamId);
    if (offset > 0) {
      Varint.write(offset, bb);
    }

    Varint.write(data.length, bb);

    bb.writeBytes(data);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final StreamFrame that = (StreamFrame) o;

    if (offset != that.offset) return false;
    if (fin != that.fin) return false;
    if (streamId != that.streamId) return false;
    return Arrays.equals(data, that.data);
  }

  @Override
  public int hashCode() {
    int result = Long.hashCode(streamId);
    result = 31 * result + (int) (offset ^ (offset >>> 32));
    result = 31 * result + (fin ? 1 : 0);
    result = 31 * result + Arrays.hashCode(data);
    return result;
  }

  @Override
  public String toString() {
    return "StreamFrame{"
        + "streamId="
        + streamId
        + ", offset="
        + offset
        + ", fin="
        + fin
        + ", data="
        + Arrays.toString(data)
        + '}';
  }
}

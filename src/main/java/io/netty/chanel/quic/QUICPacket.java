package io.netty.chanel.quic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import io.netty.buffer.Unpooled;
import io.netty.chanel.quic.base.ConnectionId;


public class QUICPacket extends DefaultByteBufHolder {

  public static QUICPacket of(
      final ConnectionId localConnectionId,
      final long streamId,
      final byte[] message) {
    final ByteBuf bb = Unpooled.wrappedBuffer(message);
    return new QUICPacket(localConnectionId, streamId, bb);
  }

  private final ConnectionId localConnectionId;
  private final long streamId;

  public QUICPacket(ConnectionId localConnectionId,
                    long streamId,
                    ByteBuf message) {
    super(message);
    this.localConnectionId = localConnectionId;
    this.streamId = streamId;
  }

  public ConnectionId getLocalConnectionId() {
    return localConnectionId;
  }

  public long getStreamId() {
    return streamId;
  }
}

package io.netty.chanel.quic.stream;

public interface Stream {

  long getId();

  StreamType getStreamType();

  void write(final byte[] b, boolean finish);

  void reset(int applicationErrorCode);

  boolean isFinished();
}

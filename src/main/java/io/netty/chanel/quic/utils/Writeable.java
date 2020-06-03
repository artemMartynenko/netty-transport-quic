package io.netty.chanel.quic.utils;

import io.netty.buffer.ByteBuf;

public interface Writeable {

  void write(ByteBuf bb);
}

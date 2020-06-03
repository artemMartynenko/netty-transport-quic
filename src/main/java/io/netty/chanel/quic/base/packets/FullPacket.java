package io.netty.chanel.quic.base.packets;


import io.netty.chanel.quic.base.Payload;
import io.netty.chanel.quic.base.frames.Frame;

public interface FullPacket extends Packet {

  PacketType getType();

  FullPacket addFrame(Frame frame);

  long getPacketNumber();

  Payload getPayload();
}

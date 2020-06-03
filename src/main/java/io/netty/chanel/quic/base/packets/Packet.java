package io.netty.chanel.quic.base.packets;


import io.netty.buffer.ByteBuf;
import io.netty.chanel.quic.base.ConnectionId;
import io.netty.chanel.quic.base.Version;
import io.netty.chanel.quic.tls.EncryptionLevel;
import io.netty.chanel.quic.tls.aead.AEAD;

import java.util.Optional;

import static io.netty.chanel.quic.tls.EncryptionLevel.*;


public interface Packet {

  int PACKET_TYPE_MASK = 0b10000000;

  static boolean isLongHeader(final int b) {
    return (PACKET_TYPE_MASK & b) == PACKET_TYPE_MASK;
  }

  static HalfParsedPacket parse(final ByteBuf bb, final int connidLength) {
    bb.markReaderIndex();
    final int firstByte = bb.readByte() & 0xFF;

    if (isLongHeader(firstByte)) {
      // Long header packet

      final int packetType = (firstByte & 0x30) >> 4;

      // might be a ver neg packet, so we must check the version
      final Version version = Version.read(bb);
      bb.resetReaderIndex();

      if (version == Version.VERSION_NEGOTIATION) {
        return VersionNegotiationPacket.parse(bb);
      } else if (packetType == PacketType.Initial.getType()) {
        return InitialPacket.parse(bb);
      } else if (packetType == PacketType.Handshake.getType()) {
        return HandshakePacket.parse(bb);
      } else if (packetType == PacketType.Retry.getType()) {
        return RetryPacket.parse(bb);
      } else {
        throw new RuntimeException("Unknown long header packet");
      }
    } else {
      // short header packet
      bb.resetReaderIndex();
      return ShortPacket.parse(bb, connidLength);
    }
  }

  static EncryptionLevel getEncryptionLevel(final Packet packet) {
    if (packet instanceof InitialPacket
        || packet instanceof RetryPacket
        || packet instanceof VersionNegotiationPacket) {
      return Initial;
    } else if (packet instanceof HandshakePacket) {
      return Handshake;
    } else {
      return OneRtt;
    }
  }

  void write(ByteBuf bb, AEAD aead);

  Optional<ConnectionId> getSourceConnectionId();

  Optional<ConnectionId> getDestinationConnectionId();
}

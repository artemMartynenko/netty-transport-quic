package io.netty.chanel.quic.base.packets;


import io.netty.chanel.quic.base.ConnectionId;
import io.netty.chanel.quic.base.Version;
import io.netty.chanel.quic.tls.aead.AEADProvider;

import java.util.Optional;

public interface HalfParsedPacket<P extends Packet> {

  Optional<Version> getVersion();

  Optional<ConnectionId> getConnectionId();

  P complete(AEADProvider aeadProvider);
}

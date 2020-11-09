package io.netty.chanel.quic.server;

import io.netty.chanel.quic.QuicChannelConnectionHolder;
import io.netty.transport.internal.quic.Connection;
import io.netty.util.internal.PlatformDependent;

import java.util.Arrays;
import java.util.Map;

public class QuicServerConnectionHolder implements QuicChannelConnectionHolder {


    private final Map<Integer, Connection> connections = PlatformDependent.newConcurrentHashMap();


    @Override
    public Long put(Connection connection) {
        Integer idHash =  Arrays.hashCode(connection.getConnectionId());
        connections.put(idHash, connection);
        return Long.valueOf(idHash);
    }

    @Override
    public void delete(Connection connection) {
        Integer idHash =  Arrays.hashCode(connection.getConnectionId());
        connections.remove(idHash);
    }

    @Override
    public void delete(Integer id) {
        connections.remove(id);
    }

    @Override
    public void clear() {
        connections.clear();
    }

    @Override
    public Connection get(Long id) {
        return connections.get(id);
    }

    @Override
    public Connection get() {
        return connections.entrySet().stream().findAny().map(Map.Entry::getValue).orElse(null);
    }


    @Override
    public Long getId(Connection connection) {
        return (long) Arrays.hashCode(connection.getConnectionId());
    }
}

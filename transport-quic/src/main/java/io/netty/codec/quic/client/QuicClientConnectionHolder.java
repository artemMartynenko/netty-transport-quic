package io.netty.codec.quic.client;

import io.netty.codec.quic.QuicChannelConnectionHolder;
import io.netty.transport.internal.quic.Connection;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class QuicClientConnectionHolder implements QuicChannelConnectionHolder {

    private AtomicReference<Connection> connectionAtomicReference;

    @Override
    public Long put(Connection connection) {
        Integer idHash =  Arrays.hashCode(connection.getConnectionId());
        connectionAtomicReference.set(connection);
        return Long.valueOf(idHash);
    }

    @Override
    public void delete(Connection connection) {
        connectionAtomicReference.set(null);
    }

    @Override
    public void delete(Integer id) {
        //TODO: throw unsupported operation
    }

    @Override
    public void clear() {
        connectionAtomicReference.set(null);
    }

    @Override
    public Connection get(Long id) {
        return connectionAtomicReference.get();
    }

    @Override
    public Connection get() {
        return connectionAtomicReference.get();
    }

    @Override
    public Long getId(Connection connection) {
        return (long) Arrays.hashCode(connection.getConnectionId());
    }
}

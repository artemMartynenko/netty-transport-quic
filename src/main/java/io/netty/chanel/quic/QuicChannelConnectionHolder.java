package io.netty.chanel.quic;

import io.netty.transport.internal.quic.Connection;

public interface QuicChannelConnectionHolder {

    public Long put(Connection connection);
    public void delete(Connection connection);
    public void delete(Integer id);
    public void clear();
    public Connection get(Long id);
    public Connection get();
    public Long getId(Connection connection);
}

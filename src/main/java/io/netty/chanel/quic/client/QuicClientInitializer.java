package io.netty.chanel.quic.client;

import io.netty.chanel.quic.QuicChannelConnectionHolder;
import io.netty.chanel.quic.QuicStreamHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.transport.internal.quic.Config;

public class QuicClientInitializer extends ChannelInitializer<DatagramChannel> {

    private Config config;

    public QuicClientInitializer(Config config) {
        this.config = config;
    }

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        QuicChannelConnectionHolder connectionHolder = new QuicClientConnectionHolder();
        pipeline.addLast(new QuicClientConnectionHandler(config, connectionHolder));
        pipeline.addLast(new QuicStreamHandler(connectionHolder));
    }
}

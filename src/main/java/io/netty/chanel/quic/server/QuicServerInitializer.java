package io.netty.chanel.quic.server;

import io.netty.chanel.quic.QuicChannelConnectionHolder;
import io.netty.chanel.quic.QuicStreamHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.transport.internal.quic.Config;

public class QuicServerInitializer extends ChannelInitializer<DatagramChannel> {

    private Config config;

    public QuicServerInitializer(Config config) {
        this.config = config;
    }

    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        QuicChannelConnectionHolder channelConnectionHolder = new QuicServerConnectionHolder();
        pipeline.addLast(new QuicServerConnectionHandler(config, channelConnectionHolder));
        pipeline.addLast(new QuicStreamHandler(channelConnectionHolder));
    }
}

package io.netty.examples.quic.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.codec.quic.QuicChannelConnectionHolder;
import io.netty.codec.quic.QuicStreamHandler;
import io.netty.codec.quic.client.QuicClientConnectionHandler;
import io.netty.codec.quic.client.QuicClientConnectionHolder;
import io.netty.codec.quic.server.QuicServerConnectionHandler;
import io.netty.codec.quic.server.QuicServerConnectionHolder;
import io.netty.examples.quic.server.EchoServerHandler;
import io.netty.transport.internal.quic.Config;
import io.netty.transport.internal.quic.QuicheNative;

import java.nio.ByteBuffer;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class EchoClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "4455"));

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                        Config config = configure();
                        ChannelPipeline pipeline = datagramChannel.pipeline();
                        QuicChannelConnectionHolder channelConnectionHolder = new QuicClientConnectionHolder();
                        pipeline.addLast(new QuicClientConnectionHandler(config, channelConnectionHolder));
                        pipeline.addLast(new QuicStreamHandler(channelConnectionHolder));
                        pipeline.addLast(new EchoServerHandler());
                    }
                });
        ChannelFuture channelFuture = clientBootstrap.connect(HOST, PORT).sync();
        System.out.println("Connected");



        channelFuture.channel().closeFuture().sync();

    }


    private static Config configure() {
        Config config = new Config(QuicheNative.quiche_config_new(0xff00001d));
        byte[] protos = new byte[27];
        ByteBuffer byteBuffer = ByteBuffer.wrap(protos);
        byteBuffer.put((byte) "hq-29".length())
                .put("hq-29".getBytes())
                .put((byte) "hq-28".length())
                .put("hq-28".getBytes())
                .put((byte) "hq-27".length())
                .put("hq-27".getBytes())
                .put((byte) "http/0.9".length())
                .put("http/0.9".getBytes());
        config.setApplication_protos(protos);
        config.getLocal_transport_params().setMax_idle_timeout(50000);
        config.getLocal_transport_params().setMax_recv_udp_payload_size(1350);
        config.getLocal_transport_params().setMax_send_udp_payload_size(1350);
        config.getLocal_transport_params().setInitial_max_data(800000000);
        config.getLocal_transport_params().setInitial_max_stream_data_bidi_local(800000000);
        config.getLocal_transport_params().setInitial_max_stream_data_uni(800000000);
        config.getLocal_transport_params().setInitial_max_streams_bidi(800000000);
        config.getLocal_transport_params().setInitial_max_streams_uni(800000000);
        config.getLocal_transport_params().setDisable_active_migration(true);
        config.setVerifyPeer(false);
        config.enableLoggingSecrets();
//        QuicheNative.enable_debug_logging();
        return config;
    }

}

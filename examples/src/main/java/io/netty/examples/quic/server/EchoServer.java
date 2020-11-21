package io.netty.examples.quic.server;

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
import io.netty.codec.quic.server.QuicServerConnectionHandler;
import io.netty.codec.quic.server.QuicServerConnectionHolder;
import io.netty.transport.internal.quic.Config;
import io.netty.transport.internal.quic.CongestionControllAlgorithm;
import io.netty.transport.internal.quic.QuicheNative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class EchoServer {

    public static final Logger LOGGER = LoggerFactory.getLogger(EchoServer.class);

    public static void main(String[] args) throws IOException, InterruptedException {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap serverBootstrap = new Bootstrap();
        serverBootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                        Config config = configure();
                        ChannelPipeline pipeline = datagramChannel.pipeline();
                        QuicChannelConnectionHolder channelConnectionHolder = new QuicServerConnectionHolder();
                        pipeline.addLast(new QuicServerConnectionHandler(config, channelConnectionHolder));
                        pipeline.addLast(new QuicStreamHandler(channelConnectionHolder));
                        pipeline.addLast(new EchoServerHandler());
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind(4455).sync();

        channelFuture.channel().closeFuture().sync();
    }






    private static Config configure() throws IOException {
        Config config = new Config(QuicheNative.quiche_config_new(0xff00001d));
        String certPath = EchoServer.class.getClassLoader().getResource("cert.crt").getPath();
        String keyPath = EchoServer.class.getClassLoader().getResource("cert.key").getPath();
        LOGGER.info("Loading cert path = " + certPath);
        LOGGER.info("Loading key path = " + keyPath);
        config.setCertChainPath(certPath);
        config.setPrivateKeyPath(keyPath);
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
        config.getLocal_transport_params().setInitial_max_data(40000000);
        config.getLocal_transport_params().setInitial_max_stream_data_bidi_local(40000000);
        config.getLocal_transport_params().setInitial_max_stream_data_bidi_remote(40000000);
        config.getLocal_transport_params().setInitial_max_streams_bidi(40000000);
        config.getLocal_transport_params().setInitial_max_streams_uni(40000000);
        config.enableLoggingSecrets();
        config.setCc_algorithm(CongestionControllAlgorithm.CUBIC);
//        QuicheNative.enable_debug_logging();
        return config;
    }

}

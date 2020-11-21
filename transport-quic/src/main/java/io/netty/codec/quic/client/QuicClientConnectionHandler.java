package io.netty.codec.quic.client;

import io.netty.buffer.ByteBuf;
import io.netty.codec.quic.DataMessage;
import io.netty.codec.quic.QuicChannelConnectionHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.transport.internal.quic.Config;
import io.netty.transport.internal.quic.Connection;
import io.netty.transport.internal.quic.ConnectionManager;
import io.netty.transport.internal.quic.exception.QUICErrorDone;
import io.netty.transport.internal.quic.exception.QUICException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class QuicClientConnectionHandler extends ByteToMessageDecoder implements ChannelOutboundHandler  {

    public static final long MAX_DATAGRAM_SIZE = 1350;
    public static final int QUICHE_MAX_CONN_ID_LEN = 20;
    public static final int LOCAL_CONN_ID_LEN = 16;

    private Config config;
    private Connection connection;
    private ChannelPromise connectionPromise;
    private QuicChannelConnectionHolder connectionHolder;

    public QuicClientConnectionHandler(Config config, QuicChannelConnectionHolder connectionHolder) {
        this.config = config;
        this.connectionHolder = connectionHolder;
    }


    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress);
        connection = connect((InetSocketAddress) remoteAddress, config);
        connectionPromise = promise;
        promise.addListener(future -> {
            if(future.isSuccess()){
               connectionHolder.put(connection);
            }
        });
        flushEgress(ctx, connection);
        //TODO: process connect promise
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
        //TODO: process disconnect promise
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof DataMessage){
            flushEgress(ctx, connection);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] raw = getAllReadableBytes(in);
        connection.processReceive(raw);


        if (!connectionPromise.isDone()){
            if (connection.isEstablished()){
                connectionPromise.setSuccess();
            }
        }

        //TODO: add connection closing processing
        DataMessage dataMessage = new DataMessage(connection);
        out.add(dataMessage);

    }


    //TODO: cleanup address type
    private Connection connect(InetSocketAddress address, Config config) throws SocketException {
        byte[] scid = generateConnectionId(LOCAL_CONN_ID_LEN);
        Connection connection = ConnectionManager.connect(address.getHostName(), scid, config);
//        connection.setAddress(address);
        connection.setConnectionId(scid);
        return connection;
    }

    private byte[] generateConnectionId(int length) {
        byte[] connectionId = new byte[length];
        byte[] rndom = new byte[length];
        new Random().nextBytes(rndom);
        for (byte i = 0; i < rndom.length; i++) {
            connectionId[i] = (byte) Math.abs(rndom[i]);
        }
        return connectionId;
    }

    private void flushEgress(ChannelHandlerContext ctx, Connection connection) throws IOException {
        byte[] byteBuffer = new byte[(int) MAX_DATAGRAM_SIZE];
        while (true) {
            try {
                int written = connection.processSend(byteBuffer);
                ByteBuf writeBytes = ctx.alloc().buffer().writeBytes(Arrays.copyOf(byteBuffer, written));
                ctx.write(writeBytes);
//                LOGGER.debug("Sent {} bytes", written);
            }catch (QUICErrorDone done){
//                LOGGER.debug("Done writing");
                return;
            }catch (QUICException e){
//                LOGGER.error("Failed to create packet");
                return;
            }
        }
    }

    private byte[] getAllReadableBytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        int readerIndex = byteBuf.readerIndex();
        byteBuf.getBytes(readerIndex, bytes);
        return bytes;
    }

}

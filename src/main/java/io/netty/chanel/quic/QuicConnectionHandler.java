package io.netty.chanel.quic;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.transport.internal.quic.*;
import io.netty.transport.internal.quic.exception.QUICErrorDone;
import io.netty.transport.internal.quic.exception.QUICException;
import io.netty.util.internal.PlatformDependent;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class QuicConnectionHandler extends ByteToMessageDecoder implements ChannelOutboundHandler {

    public static final long MAX_DATAGRAM_SIZE = 1350;
    public static final int QUICHE_MAX_CONN_ID_LEN = 20;
    private final Map<Integer, Connection> connections = PlatformDependent.newConcurrentHashMap();
    private final Config config;

    public QuicConnectionHandler(Config config) {
        this.config = config;
    }


    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
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
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof NegotiateVersionPacket) {
            NegotiateVersionPacket negotiateVersionPacket = (NegotiateVersionPacket) msg;
            byte[] out = new byte[(int) MAX_DATAGRAM_SIZE];
            ConnectionManager.quiche_negotiate_version(
                    negotiateVersionPacket.getHeaderInfo().getSourceConnId(),
                    negotiateVersionPacket.getHeaderInfo().getDestinationConnId(),
                    out);
            ByteBuf writeBytes = ctx.alloc().buffer().writeBytes(out);
        } else if (msg instanceof RetryPacket) {
            RetryPacket retryPacket = (RetryPacket) msg;
            byte[] token = token(retryPacket.getHeaderInfo().getDestinationConnId());
            byte[] newConnectionId = Arrays.copyOf(retryPacket.getHeaderInfo().getDestinationConnId(), retryPacket.getHeaderInfo().getDestinationConnId().length);
            byte[] out = new byte[(int) MAX_DATAGRAM_SIZE];
            int written = ConnectionManager.quiche_retry(retryPacket.getHeaderInfo().getSourceConnId(),
                    retryPacket.getHeaderInfo().getDestinationConnId(),
                    newConnectionId,
                    token,
                    retryPacket.getHeaderInfo().getVersion(),
                    out);
            ByteBuf writeBytes = ctx.alloc().buffer().writeBytes(Arrays.copyOf(out, written));
        } else if (msg instanceof DataPacket) {
            DataPacket dataPacket = (DataPacket) msg;
            byte[] byteBuffer = new byte[(int) MAX_DATAGRAM_SIZE];
            while (true) {
                try {
                    int written = dataPacket.getConnection().processSend(byteBuffer);
                    ByteBuf packet = ctx.alloc().buffer().writeBytes(Arrays.copyOf(byteBuffer, written));
                    ctx.write(packet);
                } catch (QUICErrorDone done) {
//                    LOGGER.debug("Done writing");
                    return;
                } catch (QUICException e) {
//                    LOGGER.error("Failed to create packet");
                    return;
                }
            }
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] raw = getAllReadableBytes(in);
        if (raw.length == 0) {
            //TODO: log error "Failed to read. Packet is empty"
        }

        //TODO: change values to configurable
        HeaderInfo headerInfo = ConnectionManager.headerInfo(raw, 16, 16, 30);
        int destConnIdHash = Arrays.hashCode(headerInfo.getDestinationConnId());
        Connection connection = connections.get(destConnIdHash);

        if (connection == null) {
            if (!ConnectionManager.quiche_version_is_supported(headerInfo.getVersion())) {
                ctx.write(new NegotiateVersionPacket(headerInfo));
                return;
            }


            if (headerInfo.getToken().length == 0) {
                ctx.write(new RetryPacket(headerInfo));
                return;
            }


            byte[] od_connection_id;
            try {
                od_connection_id = validateToken(headerInfo.getToken());
            } catch (InvalidConnectionTokenException e) {
                return;
            }

            connection = createConnection(headerInfo.getDestinationConnId(), od_connection_id, config);
        }

        int done = connection.processReceive(raw);

        if (done < 0) {
//            LOGGER.error("Failed to process packet: {}", done);
            return;
        }

        DataPacket dataPacket = new DataPacket(connection);
        out.add(dataPacket);
    }


    private byte[] getAllReadableBytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        int readerIndex = byteBuf.readerIndex();
        byteBuf.getBytes(readerIndex, bytes);
        return bytes;
    }

    //TODO: make token validation with crypto operations
    private byte[] validateToken(byte[] token) throws InvalidConnectionTokenException {
        String tokenS = new String(token);
        String firstPart = new String(Arrays.copyOfRange(token, 0, "quiche".length()));
        if (firstPart.equals("quiche")) {
            return Arrays.copyOfRange(token, "quiche".length(), token.length);
        } else {
            throw new InvalidConnectionTokenException();
        }
    }

    private byte[] token(byte[] dcid) {
        byte[] token = new byte["quiche".getBytes().length + dcid.length];
        ByteBuffer buffer = ByteBuffer.wrap(token);
        buffer.put("quiche".getBytes()).put(dcid);
        return token;
    }


    private Connection createConnection(byte[] destination_conn_id, byte[] od_cid, Config config) {
        ServerConnection connection = ConnectionManager.accept(destination_conn_id, od_cid, config);
        connection.setConnectionId(destination_conn_id);
        connections.put(Arrays.hashCode(destination_conn_id), connection);
        return connection;
    }


}

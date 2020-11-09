package io.netty.chanel.quic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.transport.internal.quic.Connection;
import io.netty.transport.internal.quic.Stream;
import io.netty.transport.internal.quic.exception.QUICException;

import java.util.Arrays;
import java.util.List;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class QuicStreamHandler extends MessageToMessageCodec<DataMessage, QuicStreamMessage> {

    private final QuicChannelConnectionHolder connectionStore;

    public QuicStreamHandler(QuicChannelConnectionHolder connectionStore) {
        this.connectionStore = connectionStore;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, QuicStreamMessage msg, List<Object> out) throws Exception {
        Connection connection = connectionStore.get(msg.getConnectionId());
        if(connection != null){
            //TODO; rename method to stream
            Stream stream = connection.getWritableStreams().newStream(msg.getStreamId());
            stream.processSend(msg.getData(), msg.isFin());
            out.add(new DataMessage(connection));
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DataMessage msg, List<Object> out) throws Exception {
        Connection connection = msg.getConnection();
        if (connection.isEstablished()) {

            Stream stream;
            while ((stream = connection.getReadableStreams().next()) != null) {
                try {
                    Boolean isFin = false;
                    byte[] received = new byte[65535];
                    int read = stream.processReceive(received, isFin);
                    long streamId= stream.getPointer();
                    QuicStreamMessage quicStreamMessage = new QuicStreamMessage(connectionStore.getId(connection), streamId, Arrays.copyOf(received, read) ,isFin);
                    out.add(quicStreamMessage);
                } catch (QUICException e) {
//                    LOGGER.error("Error during receiving data from stream");
                }
            }
            //FlushEgress
            ctx.write(new DataMessage(msg.getConnection()));
        }
    }
}

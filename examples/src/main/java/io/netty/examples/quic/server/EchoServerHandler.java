package io.netty.examples.quic.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.codec.quic.QuicStreamMessage;

import java.util.List;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class EchoServerHandler extends MessageToMessageDecoder<QuicStreamMessage> {

    protected void decode(ChannelHandlerContext channelHandlerContext, QuicStreamMessage quicStreamMessage,
                          List<Object> list) throws Exception {
        System.out.println("Received data from connection ID :"+quicStreamMessage.getConnectionId()+" stream "+quicStreamMessage.getStreamId()+" data: "+new String(quicStreamMessage.getData()));
        channelHandlerContext.write(quicStreamMessage);
    }
}

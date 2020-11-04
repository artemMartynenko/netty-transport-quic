package io.netty.chanel.quic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class QuicStreamHandler extends MessageToMessageCodec<DataPacket, QuicStreamMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, QuicStreamMessage msg, List<Object> out) throws Exception {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, DataPacket msg, List<Object> out) throws Exception {

    }
}

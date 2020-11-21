package io.netty.examples.quic.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.codec.quic.QuicStreamMessage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class EchoClientHandler extends MessageToMessageEncoder<String> {
    private AtomicInteger atomicInteger = new AtomicInteger(2);


    protected void encode(ChannelHandlerContext channelHandlerContext, String s, List<Object> list) throws Exception {
        System.out.println("Sending message : "+s);
        QuicStreamMessage quicStreamMessage = new QuicStreamMessage(0, atomicInteger.addAndGet(2), s.getBytes(), true);
        list.add(quicStreamMessage);
    }
}

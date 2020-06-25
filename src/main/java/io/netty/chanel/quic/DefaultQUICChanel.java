package io.netty.chanel.quic;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class DefaultQUICChanel implements QUICChannel{

    private final DatagramChannel datagramChannel;

    public DefaultQUICChanel(DatagramChannel datagramChannel) {
        this.datagramChannel = datagramChannel;
    }


    @Override
    public ChannelId id() {
        return datagramChannel.id();
    }

    @Override
    public EventLoop eventLoop() {
        return datagramChannel.eventLoop();
    }

    @Override
    public Channel parent() {
        return datagramChannel.parent();
    }

    //TODO: add quic specific configs
    @Override
    public ChannelConfig config() {
        return datagramChannel.config();
    }

    @Override
    public boolean isOpen() {
        return datagramChannel.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return datagramChannel.isRegistered();
    }

    @Override
    public boolean isActive() {
        return datagramChannel.isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return datagramChannel.metadata();
    }

    @Override
    public SocketAddress localAddress() {
        return datagramChannel.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return datagramChannel.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture() {
        return datagramChannel.closeFuture();
    }

    @Override
    public boolean isWritable() {
        return datagramChannel.isWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return datagramChannel.bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return datagramChannel.bytesBeforeWritable();
    }

    @Override
    public Unsafe unsafe() {
        return datagramChannel.unsafe();
    }

    //TODO: add quic handlers pipeline
    @Override
    public ChannelPipeline pipeline() {
        return datagramChannel.pipeline();
    }

    @Override
    public ByteBufAllocator alloc() {
        return datagramChannel.alloc();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return datagramChannel.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return datagramChannel.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return datagramChannel.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return datagramChannel.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return datagramChannel.close();
    }

    @Override
    public ChannelFuture deregister() {
        return datagramChannel.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return datagramChannel.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return datagramChannel.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return datagramChannel.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return datagramChannel.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return datagramChannel.close(promise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return datagramChannel.deregister(promise);
    }

    @Override
    public Channel read() {
        return datagramChannel.read();
    }

    @Override
    public ChannelFuture write(Object msg) {
        return datagramChannel.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return datagramChannel.write(msg, promise);
    }

    @Override
    public Channel flush() {
        return datagramChannel.flush();
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return datagramChannel.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return datagramChannel.writeAndFlush(msg);
    }

    @Override
    public ChannelPromise newPromise() {
        return datagramChannel.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return datagramChannel.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return datagramChannel.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return datagramChannel.newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise() {
        return datagramChannel.voidPromise();
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return datagramChannel.attr(key);
    }

    @Override
    public <T> boolean hasAttr(AttributeKey<T> key) {
        return datagramChannel.hasAttr(key);
    }

    //TODO: implemet for quic
    @Override
    public int compareTo(Channel channel) {
        return datagramChannel.compareTo(channel);
    }
}

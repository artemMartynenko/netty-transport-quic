package io.netty.chanel.quic;

import io.netty.transport.internal.quic.HeaderInfo;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class RetryPacket extends InitialPacket{
    public RetryPacket(HeaderInfo headerInfo) {
        super(headerInfo);
    }
}

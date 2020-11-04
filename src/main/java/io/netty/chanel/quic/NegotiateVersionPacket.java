package io.netty.chanel.quic;

import io.netty.transport.internal.quic.HeaderInfo;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class NegotiateVersionPacket extends InitialPacket{
    public NegotiateVersionPacket(HeaderInfo headerInfo) {
        super(headerInfo);
    }
}

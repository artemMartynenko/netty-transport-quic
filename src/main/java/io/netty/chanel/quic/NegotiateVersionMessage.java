package io.netty.chanel.quic;

import io.netty.transport.internal.quic.HeaderInfo;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class NegotiateVersionMessage extends InitialMessage {
    public NegotiateVersionMessage(HeaderInfo headerInfo) {
        super(headerInfo);
    }
}

package io.netty.codec.quic;

import io.netty.transport.internal.quic.HeaderInfo;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class RetryMessage extends InitialMessage {
    public RetryMessage(HeaderInfo headerInfo) {
        super(headerInfo);
    }
}

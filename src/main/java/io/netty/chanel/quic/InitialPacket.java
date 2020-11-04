package io.netty.chanel.quic;

import io.netty.transport.internal.quic.HeaderInfo;

import java.util.Objects;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public abstract class InitialPacket {
    protected final HeaderInfo headerInfo;

    public InitialPacket(HeaderInfo headerInfo) {
        this.headerInfo = headerInfo;
    }


    public HeaderInfo getHeaderInfo() {
        return headerInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InitialPacket that = (InitialPacket) o;
        return Objects.equals(headerInfo, that.headerInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerInfo);
    }

    @Override
    public String toString() {
        return "InitialPacket{" +
                "headerInfo=" + headerInfo +
                '}';
    }
}

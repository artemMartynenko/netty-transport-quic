package io.netty.chanel.quic;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class QuicStreamMessage {
    private long streamId;
    private long connectionId;
    private byte[] data;
    private boolean isFin;

    public QuicStreamMessage(long connectionId, long streamId, byte[] data, boolean isFin) {
        this.streamId = streamId;
        this.connectionId = connectionId;
        this.data = data;
        this.isFin = isFin;
    }

    public long getStreamId() {
        return streamId;
    }

    public void setStreamId(long streamId) {
        this.streamId = streamId;
    }

    public long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(long connectionId) {
        this.connectionId = connectionId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isFin() {
        return isFin;
    }

    public void setFin(boolean fin) {
        isFin = fin;
    }
}

package io.netty.chanel.quic;

import io.netty.transport.internal.quic.Connection;

import java.util.Objects;

/**
 * @author Artem Martynenko artem7mag@gmail.com
 **/
public class DataMessage {
    private final Connection connection;

    public DataMessage(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataMessage that = (DataMessage) o;
        return Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection);
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "connection=" + connection +
                '}';
    }

}

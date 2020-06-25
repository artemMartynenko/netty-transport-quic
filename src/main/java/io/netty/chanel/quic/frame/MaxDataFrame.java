/*
 * Copyright © 2019, 2020 Peter Doornbosch
 *
 * This file is part of Kwik, a QUIC client Java library
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.netty.chanel.quic.frame;

import net.luminis.quic.InvalidIntegerEncodingException;
import net.luminis.quic.VariableLengthInteger;
import net.luminis.quic.log.Logger;

import java.nio.ByteBuffer;

// https://tools.ietf.org/html/draft-ietf-quic-transport-20#section-19.9
public class MaxDataFrame extends QuicFrame {

    private long maxData;

    public MaxDataFrame() {
    }

    public MaxDataFrame(long flowControlMax) {
        maxData = flowControlMax;
    }

    public MaxDataFrame parse(ByteBuffer buffer, Logger log) throws InvalidIntegerEncodingException {
        buffer.get();
        maxData = VariableLengthInteger.parse(buffer);
        return this;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(9);
        // https://tools.ietf.org/html/draft-ietf-quic-transport-20#section-19.9
        // "The MAX_DATA frame (type=0x10)..."
        buffer.put((byte) 0x10);
        VariableLengthInteger.encode(maxData, buffer);

        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);
        return bytes;
    }

    public long getMaxData() {
        return maxData;
    }

    @Override
    public String toString() {
        return "MaxDataFrame[" + maxData + "]";
    }

}

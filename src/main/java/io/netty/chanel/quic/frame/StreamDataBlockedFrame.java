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

// https://tools.ietf.org/html/draft-ietf-quic-transport-20#section-19.13
public class StreamDataBlockedFrame extends QuicFrame {

    private int streamId;
    private int streamDataLimit;

    public StreamDataBlockedFrame parse(ByteBuffer buffer, Logger log) throws InvalidIntegerEncodingException {
        byte frameType = buffer.get();
        streamId = VariableLengthInteger.parse(buffer);
        streamDataLimit = VariableLengthInteger.parse(buffer);

        return this;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    @Override
    public String toString() {
        return "StreamDataBlockedFrame[" + streamId + "|" + streamDataLimit + "]";
    }

}

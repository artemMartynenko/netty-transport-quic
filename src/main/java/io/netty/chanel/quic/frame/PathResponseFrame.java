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

import net.luminis.quic.Version;
import net.luminis.quic.log.Logger;
import net.luminis.tls.ByteUtils;

import java.nio.ByteBuffer;

// https://tools.ietf.org/html/draft-ietf-quic-transport-24#section-19.18
// "The PATH_RESPONSE frame (type=0x1b) is sent in response to a
//   PATH_CHALLENGE frame."
public class PathResponseFrame extends QuicFrame {

    private byte[] data;

    public PathResponseFrame(Version quicVersion, byte[] data) {
        this.data = data;
    }

    public PathResponseFrame(Version quicVersion) {
    }

    @Override
    public byte[] getBytes() {
        byte[] frameBytes = new byte[1 + 8];
        frameBytes[0] = 0x1b;
        System.arraycopy(data, 0, frameBytes, 1, 8);
        return frameBytes;
    }

    public PathResponseFrame parse(ByteBuffer buffer, Logger log) {
        buffer.get();
        data = new byte[8];
        buffer.get(data);
        return this;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "PathResponseFrame[" + ByteUtils.bytesToHex(data) + "]";
    }

}


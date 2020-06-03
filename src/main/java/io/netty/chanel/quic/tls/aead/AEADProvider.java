package io.netty.chanel.quic.tls.aead;

import io.netty.chanel.quic.tls.EncryptionLevel;

public interface AEADProvider {

  AEAD get(EncryptionLevel level);
}

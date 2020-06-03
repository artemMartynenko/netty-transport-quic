package io.netty.chanel.quic.tls;

import io.netty.chanel.quic.utils.Bytes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

  private static final ThreadLocal<MessageDigest> digests =
      ThreadLocal.withInitial(
          () -> {
            try {
              return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
              throw new RuntimeException(e);
            }
          });

  public static byte[] sha256(final byte[]... data) {
    return digests.get().digest(Bytes.concat(data));
  }
}

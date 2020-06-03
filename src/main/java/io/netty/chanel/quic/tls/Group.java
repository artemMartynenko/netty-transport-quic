package io.netty.chanel.quic.tls;

import java.util.EnumSet;
import java.util.Optional;

public enum Group {
  X25519(0x001d),
  X448(0x001e);

  public static Optional<Group> fromValue(final int value) {
    if (value == X25519.value) {
      return Optional.of(X25519);
    } else if (value == X448.value) {
      return Optional.of(X448);
    } else {
      return Optional.empty();
    }
  }

  public static final EnumSet<Group> ALL = EnumSet.allOf(Group.class);

  private final int value;

  Group(final int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}

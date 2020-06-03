package io.netty.chanel.quic.tls.extensions;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedMap.Builder;
import io.netty.chanel.quic.tls.Group;
import io.netty.chanel.quic.utils.Hex;
import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;

public class KeyShare implements Extension {

  public static KeyShare parse(final ByteBuf bb, final boolean isClient) {
    if (!isClient) {
      bb.readShort();
    }

    final Builder<Group, byte[]> builder = ImmutableSortedMap.naturalOrder();

    while (bb.isReadable()) {
      final Optional<Group> group = Group.fromValue(bb.readShort());
      final int keyLen = bb.readShort();
      final byte[] key = new byte[keyLen];
      bb.readBytes(key);

      if (group.isPresent()) {
        builder.put(group.get(), key);
      }
    }

    return new KeyShare(builder.build());
  }

  public static KeyShare of(final Group group, final byte[] publicKey) {
    return new KeyShare(ImmutableSortedMap.of(group, publicKey));
  }

  public static KeyShare of(
      final Group group1, final byte[] key1, final Group group2, final byte[] key2) {
    return new KeyShare(ImmutableSortedMap.of(group1, key1, group2, key2));
  }

  private final SortedMap<Group, byte[]> keys;

  private KeyShare(final SortedMap<Group, byte[]> keys) {
    this.keys = ImmutableSortedMap.copyOf(keys);
  }

  @Override
  public ExtensionType getType() {
    return ExtensionType.KEY_SHARE;
  }

  public SortedMap<Group, byte[]> getKeys() {
    return keys;
  }

  public Optional<byte[]> getKey(final Group group) {
    return Optional.ofNullable(keys.get(group));
  }

  @Override
  public void write(final ByteBuf bb, final boolean isClient) {
    final int lenPos = bb.writerIndex();
    if (isClient) {
      bb.writeShort(0);
    }

    for (final Map.Entry<Group, byte[]> entry : keys.entrySet()) {
      bb.writeShort(entry.getKey().getValue());
      bb.writeShort(entry.getValue().length);
      bb.writeBytes(entry.getValue());
    }

    if (isClient) {
      bb.setShort(lenPos, bb.writerIndex() - lenPos - 2);
    }
  }

  @Override
  public String toString() {

    final StringBuilder sb = new StringBuilder();
    for (final Map.Entry<Group, byte[]> key : keys.entrySet()) {
      sb.append('{')
          .append(key.getKey().name())
          .append('=')
          .append(Hex.hex(key.getValue()))
          .append('}');
    }

    return "KeyShare{" + sb + '}';
  }
}

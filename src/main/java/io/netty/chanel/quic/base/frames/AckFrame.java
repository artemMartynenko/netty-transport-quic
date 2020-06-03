package io.netty.chanel.quic.base.frames;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.chanel.quic.utils.Varint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class AckFrame extends Frame {

  public static AckFrame parse(final ByteBuf bb) {
    final byte type = bb.readByte();
    if (type != FrameType.ACK.getType()) {
      throw new IllegalArgumentException("Illegal frame type");
    }

    final long largestAcknowledged = Varint.readAsLong(bb);
    final long ackDelay = Varint.readAsLong(bb);
    final long blockCount = Varint.readAsLong(bb);

    final List<AckBlock> blocks = new ArrayList<>();

    final long firstBlock = Varint.readAsLong(bb);
    long smallest = largestAcknowledged - firstBlock;

    blocks.add(new AckBlock(smallest, largestAcknowledged));

    long largest = largestAcknowledged;
    for (int i = 0; i < blockCount; i++) {
      if (i % 2 == 0) {
        // reading gap
        final long gap = Varint.readAsLong(bb);
        largest = smallest - gap - 1;
      } else {
        final long ackBlock = Varint.readAsLong(bb);
        smallest = largest - ackBlock;
        blocks.add(new AckBlock(smallest, largest));
      }
    }

    return new AckFrame(ackDelay, blocks);
  }

  private final long ackDelay;
  private final List<AckBlock> blocks;

  public AckFrame(final long ackDelay, final AckBlock... blocks) {
    this(ackDelay, Arrays.asList(blocks));
  }

  public AckFrame(final long ackDelay, final List<AckBlock> blocks) {
    super(FrameType.ACK);

    checkArgument(ackDelay >= 0);
    requireNonNull(blocks);
    checkArgument(blocks.size() > 0);

    this.ackDelay = ackDelay;
    this.blocks = orderBlocks(blocks);
  }

  private List<AckBlock> orderBlocks(final List<AckBlock> blocks) {
    if (blocks.size() < 1) {
      throw new IllegalArgumentException("Must contain at least one block");
    }

    final List<AckBlock> sorted = new ArrayList<>(blocks);
    sorted.sort((b1, b2) -> Long.compare(b2.getLargest(), b1.getLargest()));

    // TODO check overlaps

    return Lists.newArrayList(sorted);
  }

  public long getAckDelay() {
    return ackDelay;
  }

  public List<AckBlock> getBlocks() {
    return blocks;
  }

  @Override
  public void write(final ByteBuf bb) {
    bb.writeByte(getType().getType());

    final AckBlock firstBlock = blocks.get(0);

    Varint.write(firstBlock.getLargest(), bb);
    Varint.write(ackDelay, bb);
    Varint.write((blocks.size() - 1) * 2, bb);

    final long largest = firstBlock.getLargest();
    long smallest = firstBlock.getSmallest();
    Varint.write(largest - smallest, bb);

    for (int i = 1; i < blocks.size(); i++) {
      final AckBlock block = blocks.get(i);

      final long gap = smallest - block.getLargest() - 1;
      Varint.write(gap, bb);

      final long nextBlock = block.getLargest() - block.getSmallest();
      smallest = block.getSmallest();
      Varint.write(nextBlock, bb);
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AckFrame ackFrame = (AckFrame) o;

    if (ackDelay != ackFrame.ackDelay) return false;
    return blocks.equals(ackFrame.blocks);
  }

  @Override
  public int hashCode() {
    int result = (int) (ackDelay ^ (ackDelay >>> 32));
    result = 31 * result + blocks.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "AckFrame{" + "ackDelay=" + ackDelay + ", blocks=" + blocks + '}';
  }
}

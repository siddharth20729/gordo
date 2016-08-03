package com.xjeffrose.gordo;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Random;

public class PetitionBuilder {

  private static final Random dice = new Random();

  private PetitionBuilder() {}

  public static ByteBuf RequestAVote() {
    ByteBuf bb = Unpooled.buffer();

    bb.writeBytes(Ints.toByteArray(0));

    return bb;
  }

  public static ByteBuf VoteForMe(int sessionID) {
    ByteBuf bb = Unpooled.buffer();

    bb.writeBytes(Ints.toByteArray(1));
    bb.writeBytes(Ints.toByteArray(sessionID));
    bb.writeBytes(Ints.toByteArray(dice.nextInt(100)));

    return bb;
  }
}

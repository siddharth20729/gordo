package com.xjeffrose.gordo;

import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.server.Gordo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;

public class PetitionBuilder {

  private static final Random dice = new Random();
  private static final int BOUND = 1000000;
  private static final String localhost;

  static {
    String localhost1;
    try {
      localhost1 = InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException e) {
      localhost1 = "Unknown";
    }
    localhost = localhost1;
  }

  private PetitionBuilder() {}

  public static GordoMessage RequestAVote() {

    return new DefaultGordoMessage(UUID.randomUUID(), Op.START_LEADER_ELECTION, localhost.getBytes(), null, true);
  }

  public static GordoMessage VoteForMe(GordoMessage sessionID) {

    return new DefaultGordoMessage(UUID.randomUUID(), Op.CAST_BALLOT, localhost.getBytes(), Ints.toByteArray(dice.nextInt(BOUND)), true);
  }
}

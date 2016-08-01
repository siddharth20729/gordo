package com.xjeffrose.gordo.server;

import com.xjeffrose.gordo.GordoConfig;
import com.xjeffrose.gordo.GordoMaster;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class GordoMasterHandler extends ChannelDuplexHandler {
  private final List<String> cellMembers;
  private final GordoMaster master;
  private final Random markov;
  private final String whoAmI;

  private int electionBallot;

  public GordoMasterHandler(GordoConfig config, String cellMembers, GordoMaster master) {
    this.cellMembers = Arrays.asList(cellMembers.split(","));
    this.master = master;
    this.markov = new Random();
    this.whoAmI = config.getGordoBindIP();
    this.electionBallot = Integer.MAX_VALUE;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {

  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (electionBallot == Integer.MAX_VALUE) {
      electionBallot = markov.nextInt(cellMembers.size());
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    //TODO(JR): I think this is the correct course of action here so that
    // the election ballot is reset for the next master election
    electionBallot = Integer.MAX_VALUE;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {

  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object o) throws Exception {

  }
}

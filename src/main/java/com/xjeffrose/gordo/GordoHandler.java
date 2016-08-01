package com.xjeffrose.gordo;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class GordoHandler extends ChannelDuplexHandler {
  private final List<String> cellMembers;
  private final GordoMaster master;
  private final String whoAmI;

  public GordoMasterHandler(GordoConfig config, String cellMembers, GordoMaster master) {
    this.cellMembers = Arrays.asList(cellMembers.split(","));
    this.master = master;
    this.whoAmI = config.getGordoBindIP();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {

  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {

  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {

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

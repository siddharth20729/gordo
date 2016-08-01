package com.xjeffrose.gordo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoCodec extends CombinedChannelDuplexHandler<GordoObjectDecoder, GordoObjectEncoder> implements HttpServerUpgradeHandler.SourceCodec {
  private static final Logger log = LoggerFactory.getLogger(GordoCodec.class.getName());

  public GordoCodec() {
    super(new GordoObjectDecoder(), new GordoObjectEncoder());
  }

  @Override
  public void upgradeFrom(ChannelHandlerContext channelHandlerContext) {

  }
}
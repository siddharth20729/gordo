package com.xjeffrose.gordo;

import com.google.common.util.concurrent.ListenableFuture;
import com.xjeffrose.xio.processor.XioProcessor;
import com.xjeffrose.xio.server.RequestContext;
import io.netty.channel.ChannelHandlerContext;

public class GordoAdminProcessor implements XioProcessor {
  @Override
  public void disconnect(ChannelHandlerContext channelHandlerContext) {

  }

  @Override
  public ListenableFuture<Boolean> process(ChannelHandlerContext channelHandlerContext, Object o, RequestContext requestContext) {
    return null;
  }
}

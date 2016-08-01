package com.xjeffrose.gordo.server;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.xjeffrose.xio.processor.XioProcessor;
import com.xjeffrose.xio.server.RequestContext;
import io.netty.channel.ChannelHandlerContext;

public class GordoProcessor implements XioProcessor {
  @Override
  public void disconnect(ChannelHandlerContext channelHandlerContext) {

  }

  @Override
  public ListenableFuture<Boolean> process(ChannelHandlerContext ctx, Object o, RequestContext requestContext) {
    final ListeningExecutorService service = MoreExecutors.listeningDecorator(ctx.executor());

    return service.submit(() -> {

      // Stats and whatnot

      return true;
    });
  }
}

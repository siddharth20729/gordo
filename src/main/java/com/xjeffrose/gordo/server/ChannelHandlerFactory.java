package com.xjeffrose.gordo.server;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerFactory {
  ChannelHandler get();
}

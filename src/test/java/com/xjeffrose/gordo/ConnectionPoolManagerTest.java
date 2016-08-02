package com.xjeffrose.gordo;

import com.google.common.collect.ImmutableList;
import com.xjeffrose.gordo.server.GordoServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.InetSocketAddress;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectionPoolManagerTest {
  @Test
  public void getNode() throws Exception {

    final InetSocketAddress addr1 = UnitHelp.localSocketAddress();
    final InetSocketAddress addr2 = UnitHelp.localSocketAddress();

    GordoServerBootstrap server1 = new GordoServerBootstrap(addr1, new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

      }
    }, 5);

    GordoServerBootstrap server2 = new GordoServerBootstrap(addr2, new SimpleChannelInboundHandler<ByteBuf>() {
      @Override
      protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

      }
    }, 5);

    server1.start();
    server2.start();

    ConnectionPoolManager cpx = new ConnectionPoolManager(ImmutableList.of(addr1, addr2));

    cpx.start();

    ChannelFuture cf1 = cpx.getNode(addr1.getAddress().getHostAddress() + ":" + addr1.getPort());
    ChannelFuture cf2 = cpx.getNode(addr2.getAddress().getHostAddress() + ":" + addr1.getPort());

    assertTrue(cf1.channel().isActive());
    assertTrue(cf2.channel().isActive());

    server1.stop();
    server2.stop();
  }

  @Test
  public void addNode() throws Exception {

  }

}
package com.xjeffrose.gordo;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GordoObjectEncoder extends MessageToByteEncoder<GordoMessage> {
  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, GordoMessage gm, ByteBuf bb) throws Exception {

    bb.writeBytes(gm.getId().toString().getBytes());
    bb.writeBytes(Ints.toByteArray(gm.getOp().getOp()));
    bb.writeBytes(Ints.toByteArray(gm.getKey().length));
    bb.writeBytes(gm.getKey());
    bb.writeBytes(Ints.toByteArray(gm.getVal().length));
    bb.writeBytes(gm.getVal());
    bb.writeBytes(Ints.toByteArray(gm.getSuccess() ? 1 : 0));

  }
}

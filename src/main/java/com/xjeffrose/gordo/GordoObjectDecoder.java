package com.xjeffrose.gordo;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import java.util.UUID;

public class GordoObjectDecoder extends ByteToMessageDecoder {
  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

    final byte[] id = new byte[36];
    final byte[] op = new byte[4];
    final byte[] keySize = new byte[4];
    final byte[] valSize = new byte[4];
    final byte[] success = new byte[4];


    byteBuf.readBytes(id);
    byteBuf.readBytes(op);
    byteBuf.readBytes(keySize);

    final byte[] key;
    if (Ints.fromByteArray(keySize) > 0 ) {
      key = new byte[Ints.fromByteArray(keySize)];
      byteBuf.readBytes(key);
      byteBuf.readBytes(valSize);
    } else {
      key = null;
    }

    final byte[] val;
    if (Ints.fromByteArray(valSize) > 0 ) {
      val = new byte[Ints.fromByteArray(valSize)];
      byteBuf.readBytes(val);
    } else {
      val = null;
    }

    byteBuf.readBytes(success);

    GordoMessage gm = new DefaultGordoMessage(
        UUID.fromString(new String(id)),
            Op.fromInt(Ints.fromByteArray(op)),
            key,
            val,
            Ints.fromByteArray(success) == 0 ? false : true);

    list.add(gm);
  }
}

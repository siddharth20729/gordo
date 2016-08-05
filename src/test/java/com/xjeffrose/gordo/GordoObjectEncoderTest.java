package com.xjeffrose.gordo;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.fixtures.TestCTX;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import org.junit.Test;

import static org.junit.Assert.*;

public class GordoObjectEncoderTest {
  @Test
  public void encode() throws Exception {

    GordoObjectEncoder encoder = new GordoObjectEncoder();
    ByteBuf byteBuf = Unpooled.buffer();
    UUID Eid = UUID.randomUUID();
    GordoMessage gm = new DefaultGordoMessage(Eid, Op.GENERAL_RESPONSE, "foo".getBytes(), "bar".getBytes(), true);

    encoder.encode(new TestCTX(), gm, byteBuf);

    final byte[] id = new byte[36];
    final byte[] op = new byte[4];
    final byte[] keySize = new byte[4];
    final byte[] valSize = new byte[4];

    byteBuf.readBytes(id);
    byteBuf.readBytes(op);
    byteBuf.readBytes(keySize);

    final byte[] key;
    if (Ints.fromByteArray(keySize) > 0) {
      key = new byte[Ints.fromByteArray(keySize)];
      byteBuf.readBytes(key);
      byteBuf.readBytes(valSize);
    } else {
      key = null;
    }

    final byte[] val;
    if (Ints.fromByteArray(valSize) > 0) {
      val = new byte[Ints.fromByteArray(valSize)];
      byteBuf.readBytes(val);
    } else {
      val = null;
    }

    assertEquals(Eid.toString(), new String(id));
    assertEquals(Op.GENERAL_RESPONSE, Op.fromInt(Ints.fromByteArray(op)));
    assertEquals("foo", new String(key));
    assertEquals("bar", new String(val));

  }
}
package com.xjeffrose.gordo;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.fixtures.TestCTX;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

import static org.junit.Assert.*;

public class GordoObjectDecoderTest {
  @Test
  public void decode() throws Exception {
    GordoObjectDecoder decoder = new GordoObjectDecoder();

    ByteBuf bb = Unpooled.buffer();
    List<Object> objList = new ArrayList<>();

    String uuidString = UUID.randomUUID().toString();
    bb.writeBytes(uuidString.getBytes());
    bb.writeBytes(Ints.toByteArray(0));
    bb.writeBytes(Ints.toByteArray(3));
    bb.writeBytes("foo".getBytes());
    bb.writeBytes(Ints.toByteArray(3));
    bb.writeBytes("bar".getBytes());

    decoder.decode(new TestCTX(), bb, objList);

    GordoMessage gm = (GordoMessage) objList.get(0);

    assertEquals(UUID.fromString(uuidString), gm.getId());
    assertEquals(Op.fromInt(0), gm.getOp());
    assertEquals("foo", new String(gm.getKey()));
    assertEquals("bar", new String(gm.getVal()));

  }

}
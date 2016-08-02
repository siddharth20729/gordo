package com.xjeffrose.gordo.server;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.server.handlers.GordoLeaderElectionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LeaderElectionServerTest {

  @Test
  public void castGoodBallot() throws Exception {
    CampaignManager campaignManager = new CampaignManager();

    ChannelHandler testHandle1 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler2 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler3 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler4 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler5 = new GordoLeaderElectionHandler(5, campaignManager);


    EmbeddedChannel ch1 = new EmbeddedChannel(testHandle1);
    EmbeddedChannel ch2 = new EmbeddedChannel(testHandler2);
    EmbeddedChannel ch3 = new EmbeddedChannel(testHandler3);
    EmbeddedChannel ch4 = new EmbeddedChannel(testHandler4);
    EmbeddedChannel ch5 = new EmbeddedChannel(testHandler5);


    ByteBuf req1 = Unpooled.buffer();

    req1.writeBytes(Ints.toByteArray(0));
    req1.writeBytes(Ints.toByteArray(-1));

    ch1.writeInbound(req1.duplicate());
    ch2.writeInbound(req1.duplicate());
    ch3.writeInbound(req1.duplicate());
    ch4.writeInbound(req1.duplicate());
    ch5.writeInbound(req1.duplicate());

    ByteBuf resp1 = ch1.readOutbound();
    ByteBuf resp2 = ch2.readOutbound();
    ByteBuf resp3 = ch3.readOutbound();
    ByteBuf resp4 = ch4.readOutbound();
    ByteBuf resp5 = ch5.readOutbound();

    byte[] _op1 = new byte[4];
    resp1.readBytes(_op1);
    int op1 = Ints.fromByteArray(_op1);

    byte[] _cycle1 = new byte[4];
    resp1.readBytes(_cycle1);
    int cycle1 = Ints.fromByteArray(_cycle1);

    byte[] _ballot1 = new byte[4];
    resp1.readBytes(_ballot1);
    int ballot1 = Ints.fromByteArray(_ballot1);

    byte[] _op2 = new byte[4];
    resp2.readBytes(_op2);
    int op2 = Ints.fromByteArray(_op2);

    byte[] _cycle2 = new byte[4];
    resp2.readBytes(_cycle2);
    int cycle2 = Ints.fromByteArray(_cycle2);

    byte[] _ballot2 = new byte[4];
    resp2.readBytes(_ballot2);
    int ballot2 = Ints.fromByteArray(_ballot2);

    byte[] _op3 = new byte[4];
    resp3.readBytes(_op3);
    int op3 = Ints.fromByteArray(_op3);

    byte[] _cycle3 = new byte[4];
    resp3.readBytes(_cycle3);
    int cycle3 = Ints.fromByteArray(_cycle3);

    byte[] _ballot3 = new byte[4];
    resp3.readBytes(_ballot3);
    int ballot3 = Ints.fromByteArray(_ballot3);

    byte[] _op4 = new byte[4];
    resp4.readBytes(_op4);
    int op4 = Ints.fromByteArray(_op4);

    byte[] _cycle4 = new byte[4];
    resp4.readBytes(_cycle4);
    int cycle4 = Ints.fromByteArray(_cycle4);

    byte[] _ballot4 = new byte[4];
    resp4.readBytes(_ballot4);
    int ballot4 = Ints.fromByteArray(_ballot4);

    byte[] _op5 = new byte[4];
    resp5.readBytes(_op5);
    int op5 = Ints.fromByteArray(_op5);

    byte[] _cycle5 = new byte[4];
    resp5.readBytes(_cycle5);
    int cycle5 = Ints.fromByteArray(_cycle5);

    byte[] _ballot5 = new byte[4];
    resp5.readBytes(_ballot5);
    int ballot5 = Ints.fromByteArray(_ballot5);

    assertEquals(0, op1);
    assertEquals(1, cycle1);
    assertEquals(1, ballot1);

    assertEquals(0, op2);
    assertEquals(1, cycle2);
    assertEquals(1, ballot2);

    assertEquals(0, op3);
    assertEquals(1, cycle3);
    assertEquals(1, ballot3);

    assertEquals(0, op4);
    assertEquals(1, cycle4);
    assertEquals(1, ballot4);

    assertEquals(0, op5);
    assertEquals(1, cycle5);
    assertEquals(1, ballot5);

    ByteBuf req2 = Unpooled.buffer();

    req2.writeBytes(Ints.toByteArray(1));
    req2.writeBytes(Ints.toByteArray(1));
    req2.writeBytes(Ints.toByteArray(4));

    ch1.writeInbound(req2.duplicate());
    ch2.writeInbound(req2.duplicate());
    ch3.writeInbound(req2.duplicate());
    ch4.writeInbound(req2.duplicate());
    ch5.writeInbound(req2.duplicate());


    resp1 = ch1.readOutbound();
    resp2 = ch2.readOutbound();
    resp3 = ch3.readOutbound();
    resp4 = ch4.readOutbound();
    resp5 = ch5.readOutbound();

    resp1.readBytes(_op1);
    op1 = Ints.fromByteArray(_op1);

    resp1.readBytes(_cycle1);
    cycle1 = Ints.fromByteArray(_cycle1);

    resp1.readBytes(_ballot1);
    ballot1 = Ints.fromByteArray(_ballot1);

    resp2.readBytes(_op2);
    op2 = Ints.fromByteArray(_op2);

    resp2.readBytes(_cycle2);
    cycle2 = Ints.fromByteArray(_cycle2);

    resp2.readBytes(_ballot2);
    ballot2 = Ints.fromByteArray(_ballot2);

    assertEquals(2, op1);
    assertEquals(1, cycle1);
    assertEquals(4, ballot1);

    assertEquals(2, op2);
    assertEquals(1, cycle2);
    assertEquals(4, ballot2);

  }

  @Test
  public void castBadBallot() throws Exception {
    CampaignManager campaignManager = new CampaignManager();

    ChannelHandler testHandle1 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler2 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler3 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler4 = new GordoLeaderElectionHandler(5, campaignManager);
    ChannelHandler testHandler5 = new GordoLeaderElectionHandler(5, campaignManager);


    EmbeddedChannel ch1 = new EmbeddedChannel(testHandle1);
    EmbeddedChannel ch2 = new EmbeddedChannel(testHandler2);
    EmbeddedChannel ch3 = new EmbeddedChannel(testHandler3);
    EmbeddedChannel ch4 = new EmbeddedChannel(testHandler4);
    EmbeddedChannel ch5 = new EmbeddedChannel(testHandler5);


    ByteBuf req1 = Unpooled.buffer();

    req1.writeBytes(Ints.toByteArray(0));
    req1.writeBytes(Ints.toByteArray(-1));

    ch1.writeInbound(req1.duplicate());
    ch2.writeInbound(req1.duplicate());
    ch3.writeInbound(req1.duplicate());
    ch4.writeInbound(req1.duplicate());
    ch5.writeInbound(req1.duplicate());

    ByteBuf resp1 = ch1.readOutbound();
    ByteBuf resp2 = ch2.readOutbound();
    ByteBuf resp3 = ch3.readOutbound();
    ByteBuf resp4 = ch4.readOutbound();
    ByteBuf resp5 = ch5.readOutbound();

    byte[] _op1 = new byte[4];
    resp1.readBytes(_op1);
    int op1 = Ints.fromByteArray(_op1);

    byte[] _cycle1 = new byte[4];
    resp1.readBytes(_cycle1);
    int cycle1 = Ints.fromByteArray(_cycle1);

    byte[] _ballot1 = new byte[4];
    resp1.readBytes(_ballot1);
    int ballot1 = Ints.fromByteArray(_ballot1);

    byte[] _op2 = new byte[4];
    resp2.readBytes(_op2);
    int op2 = Ints.fromByteArray(_op2);

    byte[] _cycle2 = new byte[4];
    resp2.readBytes(_cycle2);
    int cycle2 = Ints.fromByteArray(_cycle2);

    byte[] _ballot2 = new byte[4];
    resp2.readBytes(_ballot2);
    int ballot2 = Ints.fromByteArray(_ballot2);

    byte[] _op3 = new byte[4];
    resp3.readBytes(_op3);
    int op3 = Ints.fromByteArray(_op3);

    byte[] _cycle3 = new byte[4];
    resp3.readBytes(_cycle3);
    int cycle3 = Ints.fromByteArray(_cycle3);

    byte[] _ballot3 = new byte[4];
    resp3.readBytes(_ballot3);
    int ballot3 = Ints.fromByteArray(_ballot3);

    byte[] _op4 = new byte[4];
    resp4.readBytes(_op4);
    int op4 = Ints.fromByteArray(_op4);

    byte[] _cycle4 = new byte[4];
    resp4.readBytes(_cycle4);
    int cycle4 = Ints.fromByteArray(_cycle4);

    byte[] _ballot4 = new byte[4];
    resp4.readBytes(_ballot4);
    int ballot4 = Ints.fromByteArray(_ballot4);

    byte[] _op5 = new byte[4];
    resp5.readBytes(_op5);
    int op5 = Ints.fromByteArray(_op5);

    byte[] _cycle5 = new byte[4];
    resp5.readBytes(_cycle5);
    int cycle5 = Ints.fromByteArray(_cycle5);

    byte[] _ballot5 = new byte[4];
    resp5.readBytes(_ballot5);
    int ballot5 = Ints.fromByteArray(_ballot5);

    assertEquals(0, op1);
    assertEquals(1, cycle1);
    assertEquals(1, ballot1);

    assertEquals(0, op2);
    assertEquals(1, cycle2);
    assertEquals(1, ballot2);

    assertEquals(0, op3);
    assertEquals(1, cycle3);
    assertEquals(1, ballot3);

    assertEquals(0, op4);
    assertEquals(1, cycle4);
    assertEquals(1, ballot4);

    assertEquals(0, op5);
    assertEquals(1, cycle5);
    assertEquals(1, ballot5);

    ByteBuf req2 = Unpooled.buffer();
    ByteBuf req3 = Unpooled.buffer();


    req2.writeBytes(Ints.toByteArray(1));
    req2.writeBytes(Ints.toByteArray(1));
    req2.writeBytes(Ints.toByteArray(4));

    req3.writeBytes(Ints.toByteArray(1));
    req3.writeBytes(Ints.toByteArray(1));
    req3.writeBytes(Ints.toByteArray(5));

    ch1.writeInbound(req2.duplicate());
    ch2.writeInbound(req2.duplicate());
    ch3.writeInbound(req3.duplicate());
    ch4.writeInbound(req3.duplicate());
    ch5.writeInbound(req3.duplicate());

    resp1 = ch1.readOutbound();
    resp2 = ch2.readOutbound();
    resp3 = ch3.readOutbound();
    resp4 = ch4.readOutbound();
    resp5 = ch5.readOutbound();

    resp1.readBytes(_op1);
    op1 = Ints.fromByteArray(_op1);

    resp1.readBytes(_cycle1);
    cycle1 = Ints.fromByteArray(_cycle1);

    resp1.readBytes(_ballot1);
    ballot1 = Ints.fromByteArray(_ballot1);

    resp2.readBytes(_op2);
    op2 = Ints.fromByteArray(_op2);

    resp2.readBytes(_cycle2);
    cycle2 = Ints.fromByteArray(_cycle2);

    resp2.readBytes(_ballot2);
    ballot2 = Ints.fromByteArray(_ballot2);

    assertEquals(0, op1);
    assertEquals(1, cycle1);
    assertEquals(-1, ballot1);

    assertEquals(0, op2);
    assertEquals(1, cycle2);
    assertEquals(-1, ballot2);
  }


}
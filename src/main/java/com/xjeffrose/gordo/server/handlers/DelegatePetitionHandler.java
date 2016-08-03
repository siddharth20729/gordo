package com.xjeffrose.gordo.server.handlers;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

@ChannelHandler.Sharable
public class DelegatePetitionHandler extends ChannelDuplexHandler {

  private final Map<Channel, Integer> sessionIDs;
  private final Map<Channel, Integer> leadersQuorum;


  public DelegatePetitionHandler(Map<Channel, Integer> sessionIDs, Map<Channel, Integer> leadersQuorum) {

    this.sessionIDs = sessionIDs;
    this.leadersQuorum = leadersQuorum;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof ByteBuf) {
      ByteBuf byteBuf = (ByteBuf) msg;

      byte[] _op = new byte[4];
      byteBuf.readBytes(_op);
      int op = Ints.fromByteArray(_op);

      switch (op) {
        // Request to Start Leader Election
        case 0:
          byte[] _electionCycle = new byte[4];
          byteBuf.readBytes(_electionCycle);
          int electionCycle = Ints.fromByteArray(_electionCycle);

          sessionIDs.put(ctx.channel(), electionCycle);

          break;

        case 1:
          // Cast ballot

          // Verify election Cycle
          byte[] __electionCycle = new byte[4];
          byteBuf.readBytes(__electionCycle);
          int electionCycle_ = Ints.fromByteArray(__electionCycle);

//          if (electionCycle != campaignManager.getCurrentElectionCycle()) {
//            // TODO(JR): Wait for next cycle?
//          }

          // Get the vote
          byte[] _ballot = new byte[4];
          byteBuf.readBytes(_ballot);
          int ballot = Ints.fromByteArray(_ballot);

          leadersQuorum.put(ctx.channel(), ballot);


          break;

        case 2:
          // Confirm Leader
          break;

        case 3:
          // Who is Leader
          break;

        default:
          break;
      }
    }
  }

}

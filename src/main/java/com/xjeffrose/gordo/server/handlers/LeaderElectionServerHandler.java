package com.xjeffrose.gordo.server.handlers;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.server.CampaignManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderElectionServerHandler extends ChannelDuplexHandler {
  private static final Logger log = LoggerFactory.getLogger(LeaderElectionServerHandler.class);

  private final int quorum;
  private final CampaignManager campaignManager;
  private final ByteBuf bb = UnpooledByteBufAllocator.DEFAULT.buffer();


  public LeaderElectionServerHandler(int quorum, CampaignManager campaignManager) {
    this.quorum = quorum;
    this.campaignManager = campaignManager;
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
          if (campaignManager.okToStartCampaign()) {
            if (!campaignManager.isThereAnOngoingElection()) {
              campaignManager.startNewElectionCampaign(ctx);
            }
            bb.writeBytes(Ints.toByteArray(0));
            bb.writeBytes(Ints.toByteArray(campaignManager.getCurrentElectionCycle()));
            bb.writeBytes(Ints.toByteArray(1));
            ctx.writeAndFlush(bb);
          } else {
            bb.writeBytes(Ints.toByteArray(0));
            bb.writeBytes(Ints.toByteArray(campaignManager.getCurrentElectionCycle()));
            bb.writeBytes(Ints.toByteArray(-1));
            ctx.writeAndFlush(bb);
          }

          break;

        case 1:
          // Cast ballot

          // Verify election Cycle
          byte[] _electionCycle = new byte[4];
          byteBuf.readBytes(_electionCycle);
          int electionCycle = Ints.fromByteArray(_electionCycle);

          if (electionCycle != campaignManager.getCurrentElectionCycle()) {
            // TODO(JR): Wait for next cycle?
          }

          // Get the vote
          byte[] _ballot = new byte[4];
          byteBuf.readBytes(_ballot);
          int ballot = Ints.fromByteArray(_ballot);
          campaignManager.castVote(ctx.channel(), ballot);

          if (campaignManager.votesCast() == quorum) {
//            if (campaignManager.isConsensusVote(ballot)) {
              bb.writeBytes(Ints.toByteArray(2));
              bb.writeBytes(Ints.toByteArray(campaignManager.getCurrentElectionCycle()));
              bb.writeBytes(Ints.toByteArray(campaignManager.getConsensusVote()));

              campaignManager.getDelegateList().stream().forEach(xs -> {
                xs.writeAndFlush(bb.duplicate());
              });
              System.out.println(ctx.channel().toString() + "Elected : " + ballot);
              campaignManager.swearIn(ballot);
              campaignManager.clear();
//            } else {
//              bb.writeBytes(Ints.toByteArray(0));
//              bb.writeBytes(Ints.toByteArray(campaignManager.getCurrentElectionCycle()));
//              bb.writeBytes(Ints.toByteArray(-1));
//
//              campaignManager.getDelegateList().stream().forEach(xs -> {
//                xs.writeAndFlush(bb.duplicate());
//              });
//              campaignManager.swearIn(ballot);
//            }
//            campaignManager.clear();
          }

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

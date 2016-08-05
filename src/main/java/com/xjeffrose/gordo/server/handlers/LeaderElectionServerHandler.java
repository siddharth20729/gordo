package com.xjeffrose.gordo.server.handlers;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.DefaultGordoMessage;
import com.xjeffrose.gordo.GordoMessage;
import com.xjeffrose.gordo.Op;
import com.xjeffrose.gordo.server.CampaignManager;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class LeaderElectionServerHandler extends ChannelDuplexHandler {
  private static final Logger log = LoggerFactory.getLogger(LeaderElectionServerHandler.class);

  private final int quorum;
  private final CampaignManager campaignManager;

  public LeaderElectionServerHandler(int quorum, CampaignManager campaignManager) {
    this.quorum = quorum;
    this.campaignManager = campaignManager;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof GordoMessage) {
      switch (((GordoMessage) msg).getOp()) {
        case START_LEADER_ELECTION:
          // Request to Start Leader Election
          if (campaignManager.isThereAnOngoingElection()) {
            ctx.writeAndFlush(
                new DefaultGordoMessage(((GordoMessage) msg).getId(),
                    Op.START_LEADER_ELECTION,
                    ((GordoMessage) msg).getKey(),
                    Ints.toByteArray(campaignManager.getCurrentElectionCycle()),
                    true));
          } else {

            if (campaignManager.okToStartCampaign()) {
              campaignManager.startNewElectionCampaign();
              ctx.writeAndFlush(
                  new DefaultGordoMessage(((GordoMessage) msg).getId(),
                      Op.START_LEADER_ELECTION,
                      ((GordoMessage) msg).getKey(),
                      Ints.toByteArray(campaignManager.getCurrentElectionCycle()),
                      true));
            } else {
              ctx.writeAndFlush(
                  new DefaultGordoMessage(((GordoMessage) msg).getId(),
                      Op.START_LEADER_ELECTION,
                      ((GordoMessage) msg).getKey(),
                      Ints.toByteArray(campaignManager.getCurrentElectionCycle()),
                      false));
            }
          }

          break;

        case CAST_BALLOT:
          // Cast ballot
          if (campaignManager.isThereAnOngoingElection()) {
            campaignManager.castVote(ctx, ((GordoMessage) msg));
            if (campaignManager.votesCast() > quorum) {
              if (campaignManager.consensusReached()) {
                ctx.writeAndFlush(
                    new DefaultGordoMessage(((GordoMessage) msg).getId(),
                        Op.CAST_BALLOT,
                        ((GordoMessage) msg).getKey(),
                        campaignManager.getLeader().getBytes(),
                        true));
              }  else {
                String v = campaignManager.getConsensusVote();
                campaignManager.swearIn(v);
                ctx.writeAndFlush(
                    new DefaultGordoMessage(((GordoMessage) msg).getId(),
                        Op.CAST_BALLOT,
                        ((GordoMessage) msg).getKey(),
                        v.getBytes(),
                        true));
                campaignManager.clear();
              }
            }
          } else {
            ctx.writeAndFlush(
                new DefaultGordoMessage(((GordoMessage) msg).getId(),
                    Op.CAST_BALLOT,
                    ((GordoMessage) msg).getKey(),
                    null,
                    false));
          }
          break;

        default:
          break;

      }
    }  else {
      log.error("Recieved a non standard request from: " + ctx);
    }
  }
}

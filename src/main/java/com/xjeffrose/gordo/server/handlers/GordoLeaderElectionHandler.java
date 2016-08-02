package com.xjeffrose.gordo.server.handlers;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.server.CampaignManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoLeaderElectionHandler extends ChannelDuplexHandler {
  private static final Logger log = LoggerFactory.getLogger(GordoLeaderElectionHandler.class);

  private final int quorum;
  private final CampaignManager campaignManager;
  private final ByteBuf bb = UnpooledByteBufAllocator.DEFAULT.buffer();


  public GordoLeaderElectionHandler(int quorum, CampaignManager campaignManager) {
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
        case 0:
          if (campaignManager.okToStartCampaign()) {
            bb.writeBytes(Ints.toByteArray(0));
            bb.writeBytes(Ints.toByteArray(1));
            ctx.writeAndFlush(bb);
          } else {
            bb.writeBytes(Ints.toByteArray(0));
            bb.writeBytes(Ints.toByteArray(0));
            ctx.writeAndFlush(bb);
          }

          break;

        case 1:
          // Cast ballot
          byte[] _ballot = new byte[4];
          byteBuf.readBytes(_ballot);
          int ballot = Ints.fromByteArray(_ballot);
          campaignManager.castVote(ctx.channel(), ballot);

          log.info("---------++++++++++++++++++++------------" + ctx.channel().remoteAddress()
              + " Voted for " + ballot);

          if (campaignManager.votesCast() == quorum) {
            if (campaignManager.isConsensusVote(ballot)) {
              bb.writeBytes(Ints.toByteArray(2));
              bb.writeBytes(Ints.toByteArray(ballot));

              campaignManager.getDelegateList().stream().forEach(xs -> {
                xs.writeAndFlush(bb.duplicate());
              });
              campaignManager.swearIn(ballot);
              campaignManager.clear();
            } else {
              bb.writeBytes(Ints.toByteArray(0));
              bb.writeBytes(Ints.toByteArray(1));

              campaignManager.getDelegateList().stream().forEach(xs -> {
                xs.writeAndFlush(bb.duplicate());
              });
              campaignManager.swearIn(ballot);
            }
            campaignManager.clear();
          }

          break;

        case 2:
          // Confirm Leader
//        confirmLeader(ctx, masterManager.getMaster());
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

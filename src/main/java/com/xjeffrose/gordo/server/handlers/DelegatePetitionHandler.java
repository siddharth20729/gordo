package com.xjeffrose.gordo.server.handlers;

import com.xjeffrose.gordo.GordoMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class DelegatePetitionHandler extends ChannelDuplexHandler {
  private static final Logger log = LoggerFactory.getLogger(LeaderElectionServerHandler.class);

  private final Map<Channel, GordoMessage> sessionIDs;
  private final Map<Channel, GordoMessage> leadersQuorum;

  public DelegatePetitionHandler(Map<Channel, GordoMessage> sessionIDs, Map<Channel, GordoMessage> leadersQuorum) {

    this.sessionIDs = sessionIDs;
    this.leadersQuorum = leadersQuorum;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof GordoMessage) {

      switch (((GordoMessage) msg).getOp()) {
        case START_LEADER_ELECTION:
          if (((GordoMessage) msg).getSuccess()) {
            sessionIDs.put(ctx.channel(), (GordoMessage) msg);
          }
          break;

        case CAST_BALLOT:
          if (((GordoMessage) msg).getSuccess()) {
            leadersQuorum.put(ctx.channel(), (GordoMessage) msg);
          }
          break;

        default:
          break;

      }
    } else {
      log.error("Recieved a non standard request from: " + ctx);
    }
  }

}

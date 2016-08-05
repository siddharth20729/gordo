package com.xjeffrose.gordo.server;

import com.google.common.primitives.Ints;
import com.xjeffrose.gordo.ConnectionPoolManager;
import com.xjeffrose.gordo.GordoMessage;
import com.xjeffrose.gordo.PetitionBuilder;
import com.xjeffrose.gordo.server.handlers.DelegatePetitionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampaignManager {
  private static final Logger log = LoggerFactory.getLogger(CampaignManager.class);

  private final Map<ChannelHandlerContext, GordoMessage> ballotBox = PlatformDependent.newConcurrentHashMap();
  private final Map<Channel, GordoMessage> sessionIDs = PlatformDependent.newConcurrentHashMap();
  private final Map<Channel, GordoMessage> leadersQuorum = PlatformDependent.newConcurrentHashMap();

  private final long leaderLease = 100000; // Lease Time in Milliseconds
  private final AtomicInteger electionCycle = new AtomicInteger();
  private final AtomicBoolean electionCycleOngoing = new AtomicBoolean();
  private final ConnectionPoolManager cpx;
  private final int q;

  private long electionTimeStamp;
  private String leader = null;

  public CampaignManager(List<InetSocketAddress> delegateList, int q) {
    this.cpx = new ConnectionPoolManager(delegateList, () -> new DelegatePetitionHandler(sessionIDs, leadersQuorum));
    this.q = q;
  }

  public CampaignManager(ConnectionPoolManager cpx, int q) {
    this.cpx = cpx;
    this.q = q;
  }

  public void start() {
    cpx.start();
    if (leader == null) {
      electLeader();
    }
  }

  public void stop() {
    cpx.stop();
  }

  public void castVote(ChannelHandlerContext ctx, GordoMessage gm) {
    log.info(ctx + " Voted with a Gxid of " + Ints.fromByteArray(gm.getVal()));
    ballotBox.put(ctx, gm);
  }

  public int votesCast() {
    return ballotBox.size();
  }

  public void clear() {
    ballotBox.clear();
  }

  public void swearIn(String leader) {
    this.leader = leader;
    electionTimeStamp = System.currentTimeMillis();
    electionCycleOngoing.set(false);
  }

  public void petitionDelegates(GordoMessage petition) {
    cpx.getConncetionMap().values().stream().forEach(xs -> {
      xs.channel().writeAndFlush(petition);
    });
  }

  public void electLeader() {

    petitionDelegates(PetitionBuilder.RequestAVote());

    while (sessionIDs.size() < q) {
      try {
        Thread.sleep(0, 500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    sessionIDs.values().stream().forEach(xs -> log.info(Thread.currentThread().getName() + " " + Integer.toString(Ints.fromByteArray(xs.getVal())) + " -> " + sessionIDs.size()));

    petitionDelegates(PetitionBuilder.VoteForMe(sessionIDs.values().iterator().next()));
  }

  public void impeach() {
    leader = null;
  }

  public Set<ChannelHandlerContext> getDelegateList() {
    return ballotBox.keySet();
  }

  public boolean okToStartCampaign() {
    if (leader == null) { // There is currently no leader
      return true;
    } else if ((electionTimeStamp + leaderLease) < System.currentTimeMillis()) { // Dear Leader Lease time is up
      return true;
    } else {
      return false;
    }
  }

  public void startNewElectionCampaign() {
    if (electionCycle.get() == Integer.MAX_VALUE) {
      electionCycle.set(0);
    }

    electionCycle.incrementAndGet();
    electionCycleOngoing.set(true);
    impeach();
  }

  public int getCurrentElectionCycle() {
    return electionCycle.get();
  }

  public boolean isThereAnOngoingElection() {
    return electionCycleOngoing.get();
  }

  public String getConsensusVote() {
    final int[] vote = {0};

    ballotBox.entrySet().stream().forEach(xs -> {
      int proposed = Ints.fromByteArray(xs.getValue().getVal());
      if (proposed > vote[0]) {
        vote[0] = proposed;
        leader = new String(xs.getValue().getKey());
      }
    });

    log.info("Elected -> " + leader + " at "
        + ZonedDateTime
        .now(ZoneId.of("UTC"))
        .format(DateTimeFormatter.RFC_1123_DATE_TIME)
        + " "
        + "With a Gxid value of "
        + Integer.toString(vote[0]));
    return leader;
  }

  public String getLeader() {
    return leader;
  }

  public boolean consensusReached() {
    return leader != null;
  }
}

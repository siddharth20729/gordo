package com.xjeffrose.gordo.server;

import com.xjeffrose.gordo.ConnectionPoolManager;
import com.xjeffrose.gordo.PetitionBuilder;
import com.xjeffrose.gordo.server.handlers.DelegatePetitionHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampaignManager {
  private static final Logger log = LoggerFactory.getLogger(CampaignManager.class);

  private final Map<Channel, Integer> ballotBox = PlatformDependent.newConcurrentHashMap();
  private final Map<Channel, Integer> sessionIDs = PlatformDependent.newConcurrentHashMap();
  private final Map<Channel, Integer> leadersQuorum = PlatformDependent.newConcurrentHashMap();

  private final long leaderLease = 100000; // Lease Time in Milliseconds
  private final AtomicInteger electionCycle = new AtomicInteger();
  private final AtomicBoolean electionCycleOngoing = new AtomicBoolean();
  private final ConnectionPoolManager cpx;
  private final List<InetSocketAddress> delegateList;
  private final int q;

  private long electionTimeStamp;
  private int leader = -1;

  public CampaignManager(List<InetSocketAddress> delegateList, int q) {
    this.cpx = new ConnectionPoolManager(delegateList, new DelegatePetitionHandler(sessionIDs, leadersQuorum));
    this.delegateList = delegateList;
    this.q = q;
  }

  public CampaignManager(ConnectionPoolManager cpx, int q) {
    this.cpx = cpx;
    this.delegateList = null;
    this.q = q;
  }

  public void start() {
    cpx.start();
  }

  public void castVote(Channel delegate, int ballot) {
    ballotBox.put(delegate, ballot);
  }

  public int votesCast() {
    return ballotBox.size();
  }

  public boolean isConsensusVote(int ballot) {
    return ballotBox.values().stream().allMatch(xs -> xs == ballot);
  }

  public void clear() {
    ballotBox.clear();
  }

  public void swearIn(int ballot) {
    leader = ballot;
    electionTimeStamp = System.currentTimeMillis();
    electionCycleOngoing.set(false);
  }

  public void petitionDelegates(ByteBuf petition) {
    petition.retain(cpx.getConncetionMap().size());
    cpx.getConncetionMap().values().stream().forEach(xs -> {
      xs.channel().writeAndFlush(petition.duplicate());
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

    petitionDelegates(PetitionBuilder.VoteForMe(sessionIDs.values().iterator().next()));
  }

  public void impeach() {
    leader = -1;
  }

  public Set<Channel> getDelegateList() {
    return ballotBox.keySet();
  }

  public boolean okToStartCampaign() {
    if (leader == -1) { // There is currently no leader
      return true;
    } else if ((electionTimeStamp + leaderLease) < System.currentTimeMillis()) { // Dear Leader Lease time is up
      return true;
    } else {
      return false;
    }
  }

  public void startNewElectionCampaign(ChannelHandlerContext ctx) {
    if (electionCycle.get() == Integer.MAX_VALUE) {
      electionCycle.set(0);
    }

    electionCycle.incrementAndGet();
    electionCycleOngoing.set(true);
  }

  public int getCurrentElectionCycle() {
    return electionCycle.get();
  }

  public boolean isThereAnOngoingElection() {
    return electionCycleOngoing.get();
  }

  public int getConsensusVote() {
    int vote =  ballotBox.values().stream().max(Integer::compare).orElse(null);
    log.info(Integer.toString(vote));
    return vote;
  }

  public int getLeader() {
    return leader;
  }
}

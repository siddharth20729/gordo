package com.xjeffrose.gordo.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.PlatformDependent;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampaignManager {
  private static final Logger log = LoggerFactory.getLogger(CampaignManager.class);

  private final Map<Channel, Integer> ballotBox = PlatformDependent.newConcurrentHashMap();
  private final long leaderLease = 100000; // Lease Time in Milliseconds
  private final AtomicInteger electionCycle = new AtomicInteger();
  private final AtomicBoolean electionCycleOngoing = new AtomicBoolean();

  private long electionTimeStamp;
  private int leader = -1;

  public CampaignManager() {

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

  public void electLeader() {
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
}

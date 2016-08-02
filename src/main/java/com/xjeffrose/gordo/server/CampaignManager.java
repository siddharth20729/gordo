package com.xjeffrose.gordo.server;

import io.netty.channel.Channel;
import io.netty.util.internal.PlatformDependent;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampaignManager {
  private static final Logger log = LoggerFactory.getLogger(CampaignManager.class);

  private final Map<Channel, Integer> ballotBox = PlatformDependent.newConcurrentHashMap();
  private final long masterLease = 100000; // Lease Time in Milliseconds

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
    } else if ((electionTimeStamp + masterLease) < System.currentTimeMillis()) { // Master Least time is up
      return true;
    } else {
      return false;
    }
  }
}

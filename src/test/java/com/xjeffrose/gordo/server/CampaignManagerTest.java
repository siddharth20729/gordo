package com.xjeffrose.gordo.server;

import com.google.common.collect.ImmutableList;
import com.xjeffrose.gordo.UnitHelp;
import java.net.InetSocketAddress;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CampaignManagerTest {
  @Test
  public void electLeader() throws Exception {
    final InetSocketAddress addr1 = UnitHelp.localSocketAddress();
    final InetSocketAddress addr2 = UnitHelp.localSocketAddress();
    final InetSocketAddress addr3 = UnitHelp.localSocketAddress();

    CampaignManager cm1 = new CampaignManager(ImmutableList.of(addr1, addr2, addr3), 3);
    CampaignManager cm2 = new CampaignManager(ImmutableList.of(addr1, addr2, addr3), 3);
    CampaignManager cm3 = new CampaignManager(ImmutableList.of(addr1, addr2, addr3), 3);

    LeaderElectionServer server1 = new LeaderElectionServer(addr1, 3, cm1);
    LeaderElectionServer server2 = new LeaderElectionServer(addr2, 3, cm2);
    LeaderElectionServer server3 = new LeaderElectionServer(addr3, 3, cm3);

    server1.start();
    server2.start();
    server3.start();

    cm1.start();
    cm2.start();
    cm3.start();

    Thread.sleep(2500);

    assertTrue(cm1.getLeader() != null);
    assertTrue(cm2.getLeader() != null);
    assertTrue(cm3.getLeader() != null);

    assertEquals(cm1.getLeader(), cm2.getLeader());
    assertEquals(cm2.getLeader(), cm3.getLeader());

    server1.stop();
    server2.stop();
    server3.stop();

  }

}

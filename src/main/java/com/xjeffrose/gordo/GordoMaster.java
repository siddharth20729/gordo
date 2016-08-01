package com.xjeffrose.gordo;

import com.xjeffrose.gordo.client.GordoClient;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoMaster {
  private static final Logger log = LoggerFactory.getLogger(GordoClient.class);

  private final AtomicBoolean amIMaster;
  private final AtomicBoolean isThereAMaster;
  private InetSocketAddress masterAddress;

  public GordoMaster() {
    this.amIMaster = new AtomicBoolean();
    this.isThereAMaster = new AtomicBoolean();
  }

  boolean amIMaster() {
    return amIMaster.get();
  }

  boolean isThereAMaster() {
    return isThereAMaster.get();
  }

  InetSocketAddress whoIsMaster() {
    return masterAddress;
  }

  void setMaster(InetSocketAddress masterAddress) {
    this.masterAddress = masterAddress;
    isThereAMaster.set(true);
  }

  void unSetMaster() {
    this.masterAddress = null;
    isThereAMaster.set(false);
  }

}

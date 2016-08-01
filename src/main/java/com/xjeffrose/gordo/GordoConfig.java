package com.xjeffrose.gordo;

import com.typesafe.config.Config;
import com.xjeffrose.xio.core.XioMetrics;
import com.xjeffrose.xio.server.XioServerDef;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoConfig {
  private static final Logger log = LoggerFactory.getLogger(GordoConfig.class.getName());

  private String X509_CERT;
  private String PRIVATE_KEY;
  private Config conf;
  private int workers;
  private Map<XioServerDef, XioMetrics> channelStats;
  private int bossCount;
  private String adminBindIP;
  private int adminPort;
  private int statsPort;
  private String statsBindIP;
  private int gordoPort;
  private String gordoBindIP;
  private int quorum;
  private String masterBindIP;
  private int masterPort;
  private String cellMembers;

  public GordoConfig(Config conf) {

    this.conf = conf;
    try {
      this.X509_CERT = new String(Files.readAllBytes(Paths.get(conf.getString("X509_CERT")).toAbsolutePath()));
      this.PRIVATE_KEY = new String(Files.readAllBytes(Paths.get(conf.getString("PRIVATE_KEY")).toAbsolutePath()));
    } catch (IOException e) {
      this.X509_CERT = null;
      this.PRIVATE_KEY = null;
      e.printStackTrace();
    }

    this.workers = conf.getInt("workers");
    this.bossCount = conf.getInt("boss_count");
    this.adminBindIP = conf.getString("admin_bind_ip");
    this.adminPort = conf.getInt("admin_port");
    this.statsBindIP = conf.getString("stats_bind_ip");
    this.statsPort = conf.getInt("stats_port");
    this.masterBindIP = conf.getString("master_bind_ip");
    this.masterPort = conf.getInt("master_port");
    this.gordoBindIP = conf.getString("gordo_bind_ip");
    this.gordoPort = conf.getInt("gordo_port");
    this.quorum = conf.getInt("quorum");
    this.cellMembers = conf.getString("cell_members");

  }

  public int getWorkers() {
    return workers;
  }

  public void setChannelStats(Map<XioServerDef, XioMetrics> channelStats) {
    this.channelStats = channelStats;
  }

  public int getBossCount() {
    return bossCount;
  }

  public String getAdminBindIP() {
    return adminBindIP;
  }

  public int getAdminPort() {
    return adminPort;
  }

  public int getStatsPort() {
    return statsPort;
  }

  public String getStatsBindIP() {
    return statsBindIP;
  }

  public int getGordoPort() {
    return gordoPort;
  }

  public String getGordoBindIP() {
    return gordoBindIP;
  }

  public String getCert() {
    return X509_CERT;
  }

  public String getKey() {
    return PRIVATE_KEY;
  }

  public String getMasterBindIP() {
    return masterBindIP;
  }

  public int getMasterPort() {
    return masterPort;
  }

  public String getCellMembers() {
    return cellMembers;
  }

  public void setCellMembers(String cellMembers) {
    this.cellMembers = cellMembers;
  }
}

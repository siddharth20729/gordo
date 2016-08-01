package com.xjeffrose.gordo.client;

import com.google.common.hash.Funnels;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class BaseGordoClient {

  public final static String REPLICATION_LOCK_PATH = "/chicago/replication-lock";
  protected final static String NODE_LIST_PATH = "/chicago/node-list";
  protected static final long TIMEOUT = 1000;
  private static final Logger log = LoggerFactory.getLogger(BaseGordoClient.class);
  protected static boolean TIMEOUT_ENABLED = true;
  protected static int MAX_RETRY = 3;
  protected final AtomicInteger nodesAvailable = new AtomicInteger(0);

  protected final ExecutorService exe = Executors.newFixedThreadPool(20);

  protected final boolean single_server;
  protected final RendezvousHash rendezvousHash;
  protected final ClientNodeWatcher clientNodeWatcher;
  protected final ZkClient zkClient;
  protected final ConnectionPoolManager connectionPoolMgr;
  protected int quorum;
  private CountDownLatch latch;
  private final ClientNodeWatcher.Listener listener = new ClientNodeWatcher.Listener() {
    public void nodeAdded() {
      int avail = nodesAvailable.incrementAndGet();
      if (latch != null) {
        latch.countDown();
      }
    }

    public void nodeRemoved() {
      nodesAvailable.decrementAndGet();
    }
  };

  public BaseGordoClient(String address) {
    this.single_server = true;
    this.zkClient = null;
    this.quorum = 1;
    ArrayList<String> nodeList = new ArrayList<>();
    nodeList.add(address);
    this.rendezvousHash = new RendezvousHash(Funnels.stringFunnel(Charset.defaultCharset()), nodeList, quorum);
    clientNodeWatcher = null;
    this.connectionPoolMgr = new ConnectionPoolManager(address);
  }

  public BaseGordoClient(String zkConnectionString, int quorum) throws InterruptedException {

    this.single_server = false;
    this.zkClient = new ZkClient(zkConnectionString);
    this.quorum = quorum;

    ArrayList<String> nodeList = new ArrayList<>();
    this.rendezvousHash = new RendezvousHash(Funnels.stringFunnel(Charset.defaultCharset()), nodeList, quorum);
    clientNodeWatcher = new ClientNodeWatcher(zkClient, rendezvousHash, listener);
    this.connectionPoolMgr = new ConnectionPoolManager(zkClient);
  }

  public void start() {
    try {
      if (!single_server) {
        zkClient.start();
        connectionPoolMgr.start();
        clientNodeWatcher.start();
        clientNodeWatcher.registerConnectionPoolManager(connectionPoolMgr);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void startAndWaitForNodes(int count) {
    startAndWaitForNodes(count, 5000);
  }

  public void startAndWaitForNodes(int count, long timeout) {
    if (!single_server) {
      try {
        latch = new CountDownLatch(count);
        start();
        latch.await(timeout, TimeUnit.MILLISECONDS);
        for (String node : buildNodeList()) {
          rendezvousHash.add(node);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void stop() throws Exception {
    log.info("ChicagoClient stopping");
    if (!single_server) {
      clientNodeWatcher.stop();
      connectionPoolMgr.stop();
      zkClient.stop();
    }
  }

  protected List<String> buildNodeList() {
    return zkClient.list(NODE_LIST_PATH);
  }

  public List<String> getNodeList(byte[] key) {
    return rendezvousHash.get(key);
  }

  public List<String> getEffectiveNodes(byte[] key) {
    List<String> hashList = rendezvousHash.get(key);
    if (!single_server) {
      List<String> replicationList = zkClient.list(REPLICATION_LOCK_PATH + "/" + new String(key));
      hashList.removeAll(replicationList);
    }
    return hashList;
  }
}

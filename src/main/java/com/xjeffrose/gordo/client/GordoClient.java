package com.xjeffrose.gordo.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoClient extends BaseGordoClient {
  private static final Logger log = LoggerFactory.getLogger(GordoClient.class);

  /*
   * Happy Path:
   *
   * Fail Path:
   *
   */

  public GordoClient(String zkConnectionString, int quorum) throws InterruptedException {
    super(zkConnectionString, quorum);
  }

  public GordoClient(String address) throws InterruptedException {
    super(address);
  }

  public boolean write(byte[] key, byte[] value) throws GordoClientTimeoutException, GordoClientException {
    return write("chicago".getBytes(), key, value);
  }

  public boolean write(byte[] colFam, byte[] key, byte[] value) throws GordoClientTimeoutException, GordoClientException {
    long ts = System.currentTimeMillis();
    try {
      if (TIMEOUT_ENABLED) {
        return _write(colFam, key, value, 0).get(TIMEOUT, TimeUnit.MILLISECONDS);
      } else {
        return _write(colFam, key, value, 0).get();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (TimeoutException e) {
      long now = System.currentTimeMillis();
      log.error("TIMEOUT! " + (now - ts));
      throw new GordoClientTimeoutException(e);
    }

    return false;
  }


  private ListenableFuture<Boolean> _write(byte[] colFam, byte[] key, byte[] value, int _retries) throws GordoClientTimeoutException, GordoClientException {
    final int retries = _retries;
    ListeningExecutorService executor = MoreExecutors.listeningDecorator(exe);
    return executor.submit(() -> {

      ConcurrentLinkedDeque<Boolean> responseList = new ConcurrentLinkedDeque<>();
      final long startTime = System.currentTimeMillis();
      try {

        List<String> hashList = rendezvousHash.get(key);

        for (String node : hashList) {
          if (node == null) {

          } else {
            log.debug(" +++++++++++++++++++++++++++++++++++++++++++++ Getting Node");
            ChannelFuture cf = connectionPoolMgr.getNode(node);
            log.debug(" +++++++++++++++++++++++++++++++++++++++++++++ Got Node");
            if (cf.channel().isWritable()) {
              writeState.nodeState(node, "dispatch");
              exe.execute(() -> {
                UUID id = UUID.randomUUID();
                log.debug(" +++++++++++++++++++++++++++++++++++++++++++++ Getting Listener");
                Listener listener = connectionPoolMgr.getListener(node); // Blocking
                cf.channel().writeAndFlush(new DefaultGordoMessage(id, Op.WRITE, colFam, key, value));
                log.debug("++++++++++++++++++++++++++++++++++++++++ Write to node: " + node + " " + new String(key));
                listener.addID(id);
                exe.execute(() -> {
                  try {
                    log.debug(" ++++++++++++++++++++++++++++++++++++++ Getting Response for: " + new String(key) + " " + id);
                    responseList.add(listener.getStatus(id)); //Blocking
                    log.debug(" ======================================= Got Response for: " + new String(key) + " " + id);
                  } catch (GordoClientTimeoutException e) {
//                          Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                  }
                });
              });
            } else {
              log.error("Channel was not writable");
            }
          }
        }

      } catch (GordoClientTimeoutException e) {
        log.error("Client Timeout During Write Operation: ", e);
        return false;
      }

      log.debug(" +++++++++++++++++++++++++++++++++++++++++++++++++++ Attempting to achive Quorum for key: " + new String(key));

      while (responseList.size() < quorum) {
        if (TIMEOUT_ENABLED && (System.currentTimeMillis() - startTime) > TIMEOUT) {
          log.error("Quorum timeout");
//            Thread.currentThread().interrupt();
          throw new GordoTimeoutException();
        }
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      log.debug(" +++++++++++++++++++++++++++++++++++++++++++++++++++ Returning response");
      if (responseList.stream().allMatch(b -> b)) {
        log.debug(" ========================================== Returned true ==================================================");
        return true;
      } else {
        if (MAX_RETRY < retries) {
          log.error("write failed, retrying(" + retries + ")");
          if (TIMEOUT_ENABLED) {
            return _write(colFam, key, value, retries + 1).get(TIMEOUT, TimeUnit.MILLISECONDS);
          } else {
            return _write(colFam, key, value, retries + 1).get();
          }
        } else {
          _delete(colFam, key, 0);
          throw new GordoClientException("Could not successfully complete a replicated write. Please retry the operation");
        }
      }
    });
  }
}

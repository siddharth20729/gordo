package com.xjeffrose.gordo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.PlatformDependent;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPoolManager {
  private static final Logger log = LoggerFactory.getLogger(ConnectionPoolManager.class);

  private final Map<String, ChannelFuture> connectionMap = PlatformDependent.newConcurrentHashMap();
  private final NioEventLoopGroup workerLoop = new NioEventLoopGroup(5,
      new ThreadFactoryBuilder()
          .setNameFormat("gordo-nioEventLoopGroup-%d")
          .build()
  );

  private final AtomicBoolean running = new AtomicBoolean(false);
  private final List<InetSocketAddress> delegateList;
  private final ChannelHandler handler;

  public ConnectionPoolManager(List<InetSocketAddress> delegateList, ChannelHandler handler) {
    this.delegateList = delegateList;
    this.handler = handler;
  }

  public void start() {
    running.set(true);
    refreshPool();
  }

  public void stop() {
    log.info("ConnectionPoolManager stopping");
    running.set(false);
    ChannelGroup channelGroup = new DefaultChannelGroup(workerLoop.next());
    for (ChannelFuture cf : connectionMap.values()) {
      channelGroup.add(cf.channel());
    }
    log.info("Closing channels");
    channelGroup.close().awaitUninterruptibly();
    log.info("Stopping workerLoop");
    workerLoop.shutdownGracefully();
  }

  private InetSocketAddress address(String node) {
    String chunks[] = node.split(":");
    return new InetSocketAddress(chunks[0], Integer.parseInt(chunks[1]));
  }

  public Map<String, ChannelFuture> getConncetionMap() {
    return connectionMap;
  }

  private void refreshPool() {
    delegateList.stream().forEach(xs -> {
      connect(xs);
    });
  }

  public ChannelFuture getNode(String node) {
    log.debug("Trying to get node:" + node);
    return _getNode(node, System.currentTimeMillis());
  }

  private ChannelFuture _getNode(String node, long startTime) {
    if (connectionMap.containsKey(node)) {
      ChannelFuture cf = connectionMap.get(node);
      if (cf.channel().isWritable()) {
        return cf;
      } else {
        return _getNode(node, startTime);
      }
    } else {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return _getNode(node, startTime);
    }
  }

  public void addNode(String hostname, ChannelFuture future) {
    connectionMap.put(hostname, future);

  }

  private void connect(InetSocketAddress server) {
    // Start the connection attempt.
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
        .option(ChannelOption.SO_REUSEADDR, true)
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
        .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
        .option(ChannelOption.TCP_NODELAY, true);
    bootstrap.group(workerLoop)
        .channel(NioSocketChannel.class)
        .handler(handler);


    connect2(server, bootstrap);
  }

  private void connect2(InetSocketAddress server, Bootstrap bootstrap) {
    ChannelFutureListener listener = new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) {
        if (!future.isSuccess()) {
          if (!running.get()) {
            return;
          }
          try {
//            retryLoop.takeException((Exception) future.cause());
            log.error("==== Service connect failure (will retry)", future.cause());
            connect2(server, bootstrap);
          } catch (Exception e) {
            log.error("==== Service connect failure ", future.cause());
            // Close the connection if the connection attempt has failed.
            future.channel().close();
          }
        } else {
          log.debug("Gordo connected to: " + server);
          String hostname = server.getAddress().getHostAddress();
          if (hostname.equals("localhost")) {
            hostname = "127.0.0.1";
          }
          log.debug("Adding hostname: " + hostname + ":" + ((InetSocketAddress) future.channel().remoteAddress()).getPort());
          addNode(hostname + ":" + ((InetSocketAddress) future.channel().remoteAddress()).getPort(), future);
        }
      }
    };

    bootstrap.connect(server).addListener(listener);
  }

  public void releaseChannel(String node, ChannelFuture cf1) {

  }
}

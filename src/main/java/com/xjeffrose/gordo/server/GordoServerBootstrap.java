package com.xjeffrose.gordo.server;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GordoServerBootstrap {
  private static final Logger log = LoggerFactory.getLogger(GordoServerBootstrap.class);

  private static final int NO_WRITER_IDLE_TIMEOUT = 120000;
  private static final int NO_ALL_IDLE_TIMEOUT = 120000;
  private final int requestedPort;
  private final InetSocketAddress hostAddr;
  //  private final ChannelGroup allChannels;
//  private final XioServerDef def;
//  private final XioServerConfig xioServerConfig;
//  private final ChannelStatistics channelStatistics;
  private final ChannelInitializer<SocketChannel> pipelineFactory;
  private int actualPort;
  private ServerBootstrap bootstrap;
  //  private ExecutorService bossExecutor;
//  private ExecutorService ioWorkerExecutor;
  private Channel serverChannel;


  public GordoServerBootstrap(InetSocketAddress addr, ChannelHandlerFactory handlerFactory, int maxConnections) {
    this.requestedPort = addr.getPort();
    this.hostAddr = addr;
//    this.bossExecutor = bossExecutor;
//    this.ioWorkerExecutor = ioWorkerExecutor;
//    this.allChannels = allChannels;
    // connectionLimiter must be instantiated exactly once (and thus outside the pipeline factory)
    final ConnectionLimiter connectionLimiter = new ConnectionLimiter(maxConnections);
//    this.channelStatistics = new ChannelStatistics(allChannels);

    //TODO(JR): This is an ugly mess, clean this up
    this.pipelineFactory = new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
//        XioSecurityHandlers securityHandlers = def.getSecurityFactory().getSecurityHandlers(def, xioServerConfig);
//        if (def.getClientIdleTimeout() != null) {
//          cp.addFirst("idleDisconnectHandler", new XioIdleDisconnectHandler(
//              (int) def.getClientIdleTimeout().toMillis(),
//              NO_WRITER_IDLE_TIMEOUT,
//              NO_ALL_IDLE_TIMEOUT,
//              TimeUnit.MILLISECONDS));
//        }
//        cp.addLast("connectionContext", new ConnectionContextHandler());
        cp.addLast("globalConnectionLimiter", connectionLimiter);
        cp.addLast("serviceConnectionLimiter", new ConnectionLimiter(1000));
//        cp.addLast(ChannelStatistics.NAME, channelStatistics);
//        cp.addLast("encryptionHandler", securityHandlers.getEncryptionHandler());
//        cp.addLast("messageLogger", new XioMessageLogger());
//        cp.addLast("codec", def.getCodecFactory().getCodec());
//        cp.addLast("aggregator", def.getAggregatorFactory().getAggregator());
//        cp.addLast("routingFilter", def.getRoutingFilterFactory().getRoutingFilter());
//        cp.addLast("authHandler", securityHandlers.getAuthenticationHandler());
//        cp.addLast("dispatcher", new XioDispatcher(def, xioServerConfig));
//        cp.addLast("exceptionLogger", new XioExceptionLogger());
        cp.addLast("GordoHandler", handlerFactory.get());
      }
    };

  }

  public void start() {
//    bossExecutor = xioServerConfig.getBossExecutor();
//    int bossThreadCount = xioServerConfig.getBossThreadCount();
//    ioWorkerExecutor = xioServerConfig.getWorkerExecutor();
//    int ioWorkerThreadCount = xioServerConfig.getWorkerThreadCount();

    //TODO(JR) Make these values configurable

    if (Epoll.isAvailable()) {
      start(new EpollEventLoopGroup(4), new EpollEventLoopGroup(20));
    } else {
      start(new NioEventLoopGroup(4), new NioEventLoopGroup(20));
    }
  }

  public void start(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup) {
    bootstrap = new ServerBootstrap();
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(pipelineFactory);

    //Set some sane defaults
    bootstrap
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
        .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
        .option(ChannelOption.SO_BACKLOG, 128)
        .option(ChannelOption.TCP_NODELAY, true);

    try {
      serverChannel = bootstrap.bind(hostAddr).sync().channel();
    } catch (Throwable e) {
      //TODO(JR): Do somefin here
//      e.printStackTrace();
      String msg = e.getMessage() + " (" + hostAddr + ")";
      log.error(msg, e);
      throw new RuntimeException(msg, e);
    }
    InetSocketAddress actualSocket = (InetSocketAddress) serverChannel.localAddress();
    actualPort = actualSocket.getPort();
    Preconditions.checkState(actualPort != 0 && (actualPort == requestedPort || requestedPort == 0));
    log.info("started transport " + ":" + actualPort);
  }

  public void start(EpollEventLoopGroup bossGroup, EpollEventLoopGroup workerGroup) {
    bootstrap = new ServerBootstrap();
    bootstrap
        .group(bossGroup, workerGroup)
        .channel(EpollServerSocketChannel.class)
        .childHandler(pipelineFactory);

//    xioServerConfig.getBootstrapOptions().entrySet().forEach(xs -> {
//      bootstrap.option(xs.getKey(), xs.getValue());
//    });

    //Set some sane defaults
    bootstrap
        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
        .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024)
        .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024)
        .option(ChannelOption.TCP_NODELAY, true)
        .option(ChannelOption.SO_REUSEADDR, true);

    try {
      serverChannel = bootstrap.bind(hostAddr).sync().channel();
    } catch (Throwable e) {
      //TODO(JR): Do somefin here
//      e.printStackTrace();
      String msg = e.getMessage() + " (" + hostAddr + ")";
      log.error(msg, e);
      throw new RuntimeException(msg, e);
    }
    InetSocketAddress actualSocket = (InetSocketAddress) serverChannel.localAddress();
    actualPort = actualSocket.getPort();
    Preconditions.checkState(actualPort != 0 && (actualPort == requestedPort || requestedPort == 0));
    log.info("started transport " + ":" + actualPort);
  }

  public void stop()
      throws InterruptedException {
//    if (serverChannel != null) {
////      log.info("stopping transport %s:%s", def.getName(), actualPort);
////       first stop accepting
//      final CountDownLatch latch = new CountDownLatch(1);
//      serverChannel.close().addListener(new ChannelFutureListener() {
//        @Override
//        public void operationComplete(ChannelFuture future)
//            throws Exception {
//          // stop and process remaining in-flight invocations
//          if (def.getExecutor() instanceof ExecutorService) {
//            ExecutorService exe = (ExecutorService) def.getExecutor();
//            ShutdownUtil.shutdownExecutor(exe, "dispatcher");
//          }
//          latch.countDown();
//        }
//      });
//      latch.await();
//      serverChannel = null;
//      log.info("stopped transport " + def.getName() + ":" + actualPort);
//    }
  }

  public Channel getServerChannel() {
    return serverChannel;
  }

  public int getPort() {
    if (actualPort != 0) {
      return actualPort;
    } else {
      return requestedPort; // may be 0 if server not yet started
    }
  }

//  public XioMetrics getMetrics() {
//    return channelStatistics;
//  }

  @ChannelHandler.Sharable
  private static class ConnectionLimiter extends ChannelDuplexHandler {
    private final AtomicInteger numConnections;
    private final int maxConnections;

    public ConnectionLimiter(int maxConnections) {
      this.maxConnections = maxConnections;
      this.numConnections = new AtomicInteger(0);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      if (maxConnections > 0) {
        if (numConnections.incrementAndGet() > maxConnections) {
          ctx.channel().close();
          // numConnections will be decremented in channelClosed
          log.info("Accepted connection above limit (" + maxConnections + "). Dropping.");
        }
      }
      ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      if (maxConnections > 0) {
        if (numConnections.decrementAndGet() < 0) {
          log.error("BUG in ConnectionLimiter");
        }
      }
      ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      ctx.fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      ctx.fireChannelReadComplete();
    }
  }
}

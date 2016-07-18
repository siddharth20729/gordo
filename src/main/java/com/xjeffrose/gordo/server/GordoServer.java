package com.xjeffrose.gordo.server;

import com.xjeffrose.xio.SSL.XioSecurityHandlerImpl;
import com.xjeffrose.xio.core.XioAggregatorFactory;
import com.xjeffrose.xio.core.XioCodecFactory;
import com.xjeffrose.xio.core.XioNoOpHandler;
import com.xjeffrose.xio.core.XioRoutingFilterFactory;
import com.xjeffrose.xio.core.XioSecurityFactory;
import com.xjeffrose.xio.core.XioSecurityHandlers;
import com.xjeffrose.xio.processor.XioProcessor;
import com.xjeffrose.xio.processor.XioProcessorFactory;
import com.xjeffrose.xio.server.XioBootstrap;
import com.xjeffrose.xio.server.XioServerConfig;
import com.xjeffrose.xio.server.XioServerDef;
import com.xjeffrose.xio.server.XioServerDefBuilder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpServerCodec;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoServer implements Closeable {
  private static final Logger log = LoggerFactory.getLogger(GordoServer.class);

  //TODO(JR): Make this concurrent to applow for parallel streams
  private final Set<XioServerDef> serverDefSet = new HashSet<>();
  private final GordoConfig config;
  private XioBootstrap x;
//  private final DBLog dbLog;

  public GordoServer(GordoConfig config) {
    this.config = config;


  }

  public void start() {
    configureAdminServer();
    configureStatsServer();
    configureGServer();
  }


  private void configureAdminServer() {
    XioServerDef adminServer = new XioServerDefBuilder()
        .name("Chicago Admin Server")
        .listen(new InetSocketAddress(config.getAdminBindIP(), config.getAdminPort()))
        .withSecurityFactory(new XioSecurityFactory() {
          @Override
          public XioSecurityHandlers getSecurityHandlers(XioServerDef xioServerDef, XioServerConfig xioServerConfig) {
            return new XioSecurityHandlerImpl(config.getCert(), config.getKey());
          }

          @Override
          public XioSecurityHandlers getSecurityHandlers() {
            return new XioSecurityHandlerImpl(config.getCert(), config.getKey());
          }
        })
        .withProcessorFactory(new XioProcessorFactory() {
          @Override
          public XioProcessor getProcessor() {
            return new GordoAdminProcessor();
          }
        })
        .withCodecFactory(new XioCodecFactory() {
          @Override
          public ChannelHandler getCodec() {
            return new HttpServerCodec();
          }
        })
        .withAggregator(new XioAggregatorFactory() {
          @Override
          public ChannelHandler getAggregator() {
            return null;
          }
        })
        .withRoutingFilter(new XioRoutingFilterFactory() {
          @Override
          public ChannelInboundHandler getRoutingFilter() {
            return null;
          }
        })
        .build();

    serverDefSet.add(adminServer);
  }

  private void configureStatsServer() {
    XioServerDef statsServer = new XioServerDefBuilder()
        .name("Chicago Stats Server")
        .listen(new InetSocketAddress(config.getStatsBindIP(), config.getStatsPort()))
        .withSecurityFactory(new XioSecurityFactory() {
          @Override
          public XioSecurityHandlers getSecurityHandlers(XioServerDef xioServerDef, XioServerConfig xioServerConfig) {
            return new XioSecurityHandlerImpl(config.getCert(), config.getKey());
          }

          @Override
          public XioSecurityHandlers getSecurityHandlers() {
            return new XioSecurityHandlerImpl(config.getCert(), config.getKey());
          }
        })
        .withProcessorFactory(new XioProcessorFactory() {
          @Override
          public XioProcessor getProcessor() {
            return new GordoStatsProcessor();
          }
        })
        .withCodecFactory(new XioCodecFactory() {
          @Override
          public ChannelHandler getCodec() {
            return new HttpServerCodec();
          }
        })
        .withAggregator(new XioAggregatorFactory() {
          @Override
          public ChannelHandler getAggregator() {
            return null;
          }
        })
        .withRoutingFilter(new XioRoutingFilterFactory() {
          @Override
          public ChannelInboundHandler getRoutingFilter() {
            return null;
          }
        })
        .build();

    serverDefSet.add(statsServer);
  }

  private void configureGServer() {
    XioServerDef dbServer = new XioServerDefBuilder()
        .name("Chicago DB Server")
        .listen(new InetSocketAddress(config.getDBBindIP(), config.getDBPort()))
//        .withSecurityFactory(new XioNoOpSecurityFactory())
        .withSecurityFactory(new XioSecurityFactory() {
          @Override
          public XioSecurityHandlers getSecurityHandlers(XioServerDef xioServerDef, XioServerConfig xioServerConfig) {
            return new XioSecurityHandlerImpl(config.getCert(), config.getKey());
          }

          @Override
          public XioSecurityHandlers getSecurityHandlers() {
            return new XioSecurityHandlerImpl(config.getCert(), config.getKey());
          }
        })
        .withProcessorFactory(new XioProcessorFactory() {
          @Override
          public XioProcessor getProcessor() {
            return new GordoProcessor();
          }
        })
        .withCodecFactory(new XioCodecFactory() {
          @Override
          public ChannelHandler getCodec() {
            return new GordoCodec();
          }
        })
        .withAggregator(new XioAggregatorFactory() {
          @Override
          public ChannelHandler getAggregator() {
            return new XioNoOpHandler();
          }
        })
        .withRoutingFilter(new XioRoutingFilterFactory() {
          @Override
          public ChannelInboundHandler getRoutingFilter() {
            return new GordoHandler();
          }
        })
        .build();

    serverDefSet.add(dbServer);
  }

  public void run() {

    configureAdminServer();
    configureStatsServer();
    configureGServer();

    XioServerConfig serverConfig = XioServerConfig.newBuilder()
        .setBossThreadCount(config.getBossCount())
        .setBossThreadExecutor(Executors.newCachedThreadPool())
        .setWorkerThreadCount(config.getWorkers())
        .setWorkerThreadExecutor(Executors.newCachedThreadPool())
//        .setBootstrapOptions((Map<ChannelOption<Object>, Object>) new HashMap<>().put(ChannelOption.SO_KEEPALIVE, true))
        .build();

    ChannelGroup channels = new DefaultChannelGroup(new NioEventLoopGroup(config.getWorkers()).next());
    x = new XioBootstrap(serverDefSet, serverConfig, channels);

    try {
      x.start();
      config.setChannelStats(x.getXioMetrics());
      // For debug, leave commented out (or not, your choice if you like it)
      String msg = "--------------- Chicago Server Started!!! ----------------------";
      //System.out.println(msg);
      log.info(msg);
    } catch (Exception e) {
      e.printStackTrace();
      log.error("There was an error starting Chicago: ", e);
      x.stop();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() throws IOException {
    x.stop();
    serverDefSet.clear();
  }

  public void stop() {
    try {
      close();
    } catch (IOException e) {
      //TODO(JR): Should we just force close here?
      log.error("Error while attempting to close", e);
      System.exit(-1);
    }
  }

  public InetSocketAddress getDBBoundInetAddress() {
    for (Map.Entry<XioServerDef, Integer> entry : x.getBoundPorts().entrySet()) {
      if (entry.getKey().getName().equals("Chicago DB Server")) {
        return new InetSocketAddress(
            entry.getKey().getHostAddress().getAddress(),
            entry.getValue()
        );
      }
    }
    return null;
  }
}

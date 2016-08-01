package com.xjeffrose.gordo;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.xjeffrose.gordo.server.GordoServer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gordo {
  private static final Logger log = LoggerFactory.getLogger(Gordo.class.getName());

  public static void main(String[] args) {
    log.info("Starting Gordo, have a nice day");

    Config _conf;

    if (args.length > 0) {
      try {
        _conf = ConfigFactory.parseFile(new File(args[1]));
      } catch (Exception e) {
        _conf = ConfigFactory.parseFile(new File("application.conf"));
      }
    } else {
      _conf = ConfigFactory.parseFile(new File("test.conf"));
    }

    GordoConfig config = new GordoConfig(_conf);

    try {
      GordoServer server = new GordoServer(config);
      server.start();
    } catch (Exception e) {
      log.error("Error Starting Gordo", e);
      throw new RuntimeException(e);
    }
  }
}
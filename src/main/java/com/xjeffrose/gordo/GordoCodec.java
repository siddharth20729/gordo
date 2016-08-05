package com.xjeffrose.gordo;


import io.netty.channel.CombinedChannelDuplexHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoCodec extends CombinedChannelDuplexHandler<GordoObjectDecoder, GordoObjectEncoder> {
  private static final Logger log = LoggerFactory.getLogger(GordoCodec.class.getName());

  public GordoCodec() {
    super(new GordoObjectDecoder(), new GordoObjectEncoder());
  }

}

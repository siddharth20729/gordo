package com.xjeffrose.gordo;


import io.netty.channel.CombinedChannelDuplexHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GordoClientCodec extends CombinedChannelDuplexHandler<GordoObjectDecoder, GordoObjectEncoder> {
  private static final Logger log = LoggerFactory.getLogger(GordoClientCodec.class.getName());

  public GordoClientCodec() {
    super(new GordoObjectDecoder(), new GordoObjectEncoder());
  }

}

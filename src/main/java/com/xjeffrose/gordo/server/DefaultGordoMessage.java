package com.xjeffrose.gordo.server;

import io.netty.handler.codec.DecoderResult;
import java.util.UUID;

public class DefaultGordoMessage implements GordoMessage {
  private DecoderResult decoderResult;

  public DefaultGordoMessage(UUID uuid, Object fromInt, byte[] colFam, byte[] key, byte[] val) {
  }


  public void setDecoderResult(DecoderResult decoderResult) {
    this.decoderResult = decoderResult;
  }

  public DecoderResult getDecoderResult() {
    return decoderResult;
  }
}

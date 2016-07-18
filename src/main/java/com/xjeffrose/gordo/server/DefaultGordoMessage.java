package com.xjeffrose.gordo.server;

import com.xjeffrose.gordo.GordoMessage;
import com.xjeffrose.gordo.Op;
import io.netty.handler.codec.DecoderResult;
import java.util.UUID;

public class DefaultGordoMessage implements GordoMessage {
  private DecoderResult decoderResult;

  public DefaultGordoMessage(UUID uuid, Object fromInt, byte[] colFam, byte[] key, byte[] val) {
  }


  @Override
  public DecoderResult decoderResult() {
    return null;
  }

  public void setDecoderResult(DecoderResult decoderResult) {
    this.decoderResult = decoderResult;
  }

  public DecoderResult getDecoderResult() {
    return decoderResult;
  }

  @Override
  public UUID getId() {
    return null;
  }

  @Override
  public Op getOp() {
    return null;
  }

  @Override
  public byte[] getKey() {
    return new byte[0];
  }

  @Override
  public byte[] getVal() {
    return new byte[0];
  }

  @Override
  public boolean getSuccess() {
    return false;
  }

  @Override
  public byte[] getColFam() {
    return new byte[0];
  }
}

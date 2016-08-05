package com.xjeffrose.gordo;

import java.util.UUID;

public class DefaultGordoMessage implements GordoMessage {

  private final UUID id;
  private final Op op;
  private final byte[] key;
  private final byte[] val;
  private final boolean success;

  public DefaultGordoMessage(UUID id, Op op, byte[] key, byte[] val, boolean success) {
    this.id = id;
    this.op = op;
    this.key = key;
    this.val = val;
    this.success = success;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Op getOp() {
    return op;
  }

  @Override
  public byte[] getKey() {
    if (key == null) {
      return new byte[0];
    }
    return key;
  }

  @Override
  public byte[] getVal() {
    if (val == null) {
      return new byte[0];
    }
    return val;
  }

  @Override
  public boolean getSuccess() {
    return success;
  }
}

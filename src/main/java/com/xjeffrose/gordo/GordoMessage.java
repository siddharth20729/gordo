package com.xjeffrose.gordo;

import java.util.UUID;

public interface GordoMessage {

  UUID getId();

  Op getOp();

  byte[] getKey();

  byte[] getVal();

  boolean getSuccess();

}

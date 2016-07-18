package com.xjeffrose.gordo.server;

import java.util.UUID;

public interface GordoMessage extends GordoObject {

  UUID getId();

  Op getOp();

  byte[] getKey();

  byte[] getVal();

  boolean getSuccess();

  byte[] getColFam();
}

package com.xjeffrose.gordo;

public enum Op {

  CHILD_NODE_ADDED(0),
  CHILD_NODE_MODIFIED(1),
  CHILD_NODE_REMOVED(2),
  FILE_MODIFIED(3),
  LOCK_ACQUIRED(4),
  LOCK_CONFLICT(5),
  MASTER_FAILED(6),
  INVALIDATE_CACHE(7),
  MARK_NODE_DIRTY(8),
  REPLICATION_EVENT(9);


  private int i;

  Op(int i) {

    this.i = i;
  }

  public int getOp() {
    return i;
  }

  public static Op fromInt(int x) {
    switch(x) {
      case 0:
        return CHILD_NODE_ADDED;
      case 1:
        return CHILD_NODE_MODIFIED;
      case 2:
        return CHILD_NODE_REMOVED;
      case 3:
        return FILE_MODIFIED;
      case 4:
        return LOCK_ACQUIRED;
      case 5:
        return LOCK_CONFLICT;
      case 6:
        return MASTER_FAILED;
      case 7:
        return INVALIDATE_CACHE;
      case 8:
        return MARK_NODE_DIRTY;
      case 9:
        return REPLICATION_EVENT;
    }

    return null;
  }
}

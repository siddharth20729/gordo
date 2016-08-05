package com.xjeffrose.gordo;
public enum Op {
  START_LEADER_ELECTION(0),
  CAST_BALLOT(1),
  READ(2),
  WRITE(3),
  UPDATE(4),
  DELETE(5),
  CHILD_NODE_ADDED(6),
  CHILD_NODE_MODIFIED(7),
  CHILD_NODE_REMOVED(8),
  FILE_MODIFIED(9),
  LOCK_ACQUIRED(10),
  LOCK_CONFLICT(11),
  MASTER_FAILED(12),
  MASTER_BALLOT(13),
  INVALIDATE_CACHE(14),
  MARK_NODE_DIRTY(15),
  REPLICATION_EVENT(16),
  GENERAL_RESPONSE(17);

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
        return START_LEADER_ELECTION;
      case 1:
        return CAST_BALLOT;
      case 2:
        return READ;
      case 3:
        return WRITE;
      case 4:
        return UPDATE;
      case 5:
        return DELETE;
      case 6:
        return CHILD_NODE_ADDED;
      case 7:
        return CHILD_NODE_MODIFIED;
      case 8:
        return CHILD_NODE_REMOVED;
      case 9:
        return FILE_MODIFIED;
      case 10:
        return LOCK_ACQUIRED;
      case 11:
        return LOCK_CONFLICT;
      case 12:
        return MASTER_FAILED;
      case 13:
        return MASTER_BALLOT;
      case 14:
        return INVALIDATE_CACHE;
      case 15:
        return MARK_NODE_DIRTY;
      case 16:
        return REPLICATION_EVENT;
      case 17:
        return GENERAL_RESPONSE;
    }
    return null;
  }
}

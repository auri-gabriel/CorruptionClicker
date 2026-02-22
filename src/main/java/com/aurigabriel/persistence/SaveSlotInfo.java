package com.aurigabriel.persistence;

public class SaveSlotInfo {
  private final int slot;
  private final boolean exists;
  private final double cleanMoney;
  private final double dirtyMoney;
  private final long lastModifiedMillis;

  public SaveSlotInfo(int slot, boolean exists, double cleanMoney, double dirtyMoney, long lastModifiedMillis) {
    this.slot = slot;
    this.exists = exists;
    this.cleanMoney = cleanMoney;
    this.dirtyMoney = dirtyMoney;
    this.lastModifiedMillis = lastModifiedMillis;
  }

  public int getSlot() {
    return slot;
  }

  public boolean exists() {
    return exists;
  }

  public double getCleanMoney() {
    return cleanMoney;
  }

  public double getDirtyMoney() {
    return dirtyMoney;
  }

  public long getLastModifiedMillis() {
    return lastModifiedMillis;
  }
}

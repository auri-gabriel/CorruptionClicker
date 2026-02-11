package com.aurigabriel;

public class Game {

  private double cleanMoney = 0;
  private double dirtyMoney = 0;
  private double dirtyMoneyPerSecond = 0;
  private double cleanFromDirtyPerSecond = 0;

  public void click() {
    dirtyMoney += 1;
  }

  public boolean manualClean() {
    if (dirtyMoney >= 1) {
      dirtyMoney -= 1;
      cleanMoney += 1;
      return true;
    }
    return false;
  }

  public void addCleanMoney(double amount) {
    cleanMoney += amount;
  }

  public void addDirtyMoney(double amount) {
    dirtyMoney += amount;
  }

  public void update() {
    dirtyMoney += dirtyMoneyPerSecond / 10.0; // 10 ticks per second
    double conversion = cleanFromDirtyPerSecond / 10.0;
    if (conversion > dirtyMoney) {
      conversion = dirtyMoney;
    }
    dirtyMoney -= conversion;
    cleanMoney += conversion;
  }

  public double getCleanMoney() {
    return cleanMoney;
  }

  public double getDirtyMoney() {
    return dirtyMoney;
  }

  public double getDirtyMoneyPerSecond() {
    return dirtyMoneyPerSecond;
  }

  public double getCleanFromDirtyPerSecond() {
    return cleanFromDirtyPerSecond;
  }

  public void increaseDirtyMoneyPerSecond(double amount) {
    dirtyMoneyPerSecond += amount;
  }

  public void increaseCleanFromDirtyPerSecond(double amount) {
    cleanFromDirtyPerSecond += amount;
  }
}

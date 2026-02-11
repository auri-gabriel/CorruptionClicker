package com.aurigabriel.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SaveData {
  private final double cleanMoney;
  private final double dirtyMoney;
  private final Map<String, Integer> upgradeQuantities;

  public SaveData(double cleanMoney, double dirtyMoney, Map<String, Integer> upgradeQuantities) {
    this.cleanMoney = cleanMoney;
    this.dirtyMoney = dirtyMoney;
    this.upgradeQuantities = new HashMap<>(upgradeQuantities);
  }

  public double getCleanMoney() {
    return cleanMoney;
  }

  public double getDirtyMoney() {
    return dirtyMoney;
  }

  public Map<String, Integer> getUpgradeQuantities() {
    return Collections.unmodifiableMap(upgradeQuantities);
  }
}

package com.aurigabriel.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameState {
  private double cleanMoney;
  private double dirtyMoney;
  private final Map<String, UpgradeInstance> upgrades;

  public GameState(List<UpgradeDefinition> definitions) {
    upgrades = new LinkedHashMap<>();
    for (UpgradeDefinition definition : definitions) {
      upgrades.put(definition.getId(), new UpgradeInstance(definition));
    }
  }

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

  public void update(double deltaSeconds) {
    double dirtyRate = getDirtyMoneyPerSecond();
    double cleanRate = getCleanFromDirtyPerSecond();

    dirtyMoney += dirtyRate * deltaSeconds;

    double conversion = cleanRate * deltaSeconds;
    if (conversion > dirtyMoney) {
      conversion = dirtyMoney;
    }
    dirtyMoney -= conversion;
    cleanMoney += conversion;
  }

  public boolean buyUpgrade(String upgradeId) {
    UpgradeInstance instance = upgrades.get(upgradeId);
    if (instance == null) {
      return false;
    }
    double cost = instance.getCost();
    if (cleanMoney >= cost) {
      cleanMoney -= cost;
      instance.increaseQuantity();
      return true;
    }
    return false;
  }

  public double getCleanMoney() {
    return cleanMoney;
  }

  public double getDirtyMoney() {
    return dirtyMoney;
  }

  public void setCleanMoney(double cleanMoney) {
    this.cleanMoney = Math.max(0, cleanMoney);
  }

  public void setDirtyMoney(double dirtyMoney) {
    this.dirtyMoney = Math.max(0, dirtyMoney);
  }

  public double getDirtyMoneyPerSecond() {
    return calculateRate(UpgradeType.POLITICIAN);
  }

  public double getCleanFromDirtyPerSecond() {
    return calculateRate(UpgradeType.BUSINESS);
  }

  public Collection<UpgradeInstance> getUpgrades() {
    return upgrades.values();
  }

  public UpgradeInstance getUpgrade(String id) {
    return upgrades.get(id);
  }

  public void setUpgradeQuantity(String id, int quantity) {
    UpgradeInstance instance = upgrades.get(id);
    if (instance != null) {
      instance.setQuantity(quantity);
    }
  }

  private double calculateRate(UpgradeType type) {
    double total = 0;
    for (UpgradeInstance instance : upgrades.values()) {
      if (instance.getDefinition().getType() == type) {
        total += instance.getTotalProduction();
      }
    }
    return total;
  }
}

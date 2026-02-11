package com.aurigabriel;

public class Upgrade {

  public enum UpgradeType {
    POLITICIAN,
    BUSINESS
  }

  private String name;
  private double baseCost;
  private double cost;
  private double production;
  private UpgradeType type;
  private int quantity = 0;

  public Upgrade(String name, double baseCost, double production, UpgradeType type) {
    this.name = name;
    this.baseCost = baseCost;
    this.cost = baseCost;
    this.production = production;
    this.type = type;
  }

  public boolean buy(Game game) {
    if (game.getCleanMoney() >= cost) {
      game.addCleanMoney(-cost);
      quantity++;
      if (type == UpgradeType.POLITICIAN) {
        game.increaseDirtyMoneyPerSecond(production);
      } else {
        game.increaseCleanFromDirtyPerSecond(production);
      }
      increaseCost();
      return true;
    }
    return false;
  }

  private void increaseCost() {
    cost = baseCost * Math.pow(1.15, quantity);
  }

  public String getDisplayText() {
    return name + " | Custo: " + (int) cost + " | Qtde: " + quantity;
  }

  public double getCost() {
    return cost;
  }
}

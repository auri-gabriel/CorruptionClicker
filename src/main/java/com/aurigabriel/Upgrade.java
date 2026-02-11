package com.aurigabriel;

public class Upgrade {

  private String name;
  private double baseCost;
  private double cost;
  private double production;
  private int quantity = 0;

  public Upgrade(String name, double baseCost, double production) {
    this.name = name;
    this.baseCost = baseCost;
    this.cost = baseCost;
    this.production = production;
  }

  public boolean buy(Game game) {
    if (game.getInfluence() >= cost) {
      game.addInfluence(-cost);
      quantity++;
      game.increaseInfluencePerSecond(production);
      increaseCost();
      return true;
    }
    return false;
  }

  private void increaseCost() {
    cost = baseCost * Math.pow(1.15, quantity);
  }

  public String getDisplayText() {
    return name + " | Cost: " + (int) cost + " | Owned: " + quantity;
  }
}

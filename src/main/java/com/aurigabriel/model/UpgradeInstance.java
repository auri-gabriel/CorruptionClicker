package com.aurigabriel.model;

public class UpgradeInstance {
  private static final double COST_GROWTH = 1.15;

  private final UpgradeDefinition definition;
  private int quantity;

  public UpgradeInstance(UpgradeDefinition definition) {
    this.definition = definition;
    this.quantity = 0;
  }

  public UpgradeDefinition getDefinition() {
    return definition;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = Math.max(0, quantity);
  }

  public void increaseQuantity() {
    quantity += 1;
  }

  public double getCost() {
    return definition.getBaseCost() * Math.pow(COST_GROWTH, quantity);
  }

  public double getTotalProduction() {
    return definition.getProduction() * quantity;
  }
}

package com.aurigabriel.model;

public class UpgradeDefinition {
  private final String id;
  private final String name;
  private final double baseCost;
  private final double production;
  private final UpgradeType type;

  public UpgradeDefinition(String id, String name, double baseCost, double production, UpgradeType type) {
    this.id = id;
    this.name = name;
    this.baseCost = baseCost;
    this.production = production;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public double getBaseCost() {
    return baseCost;
  }

  public double getProduction() {
    return production;
  }

  public UpgradeType getType() {
    return type;
  }
}

package com.aurigabriel;

public class Game {

  private double influence = 0;
  private double influencePerSecond = 0;

  public void click() {
    influence += 1;
  }

  public void addInfluence(double amount) {
    influence += amount;
  }

  public void update() {
    influence += influencePerSecond / 10.0; // 10 ticks per second
  }

  public double getInfluence() {
    return influence;
  }

  public double getInfluencePerSecond() {
    return influencePerSecond;
  }

  public void increaseInfluencePerSecond(double amount) {
    influencePerSecond += amount;
  }
}

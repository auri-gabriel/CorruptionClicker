package com.aurigabriel.core;

import java.util.function.DoubleConsumer;

import javax.swing.Timer;

public class GameLoop {
  private final Timer timer;
  private final DoubleConsumer onTick;
  private long lastNanos;

  public GameLoop(int tickMillis, DoubleConsumer onTick) {
    this.timer = new Timer(tickMillis, event -> tick());
    this.onTick = onTick;
  }

  public void start() {
    lastNanos = System.nanoTime();
    timer.start();
  }

  public void stop() {
    timer.stop();
  }

  private void tick() {
    long now = System.nanoTime();
    double deltaSeconds = (now - lastNanos) / 1_000_000_000.0;
    lastNanos = now;
    onTick.accept(deltaSeconds);
  }
}

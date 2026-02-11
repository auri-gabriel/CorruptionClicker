package com.aurigabriel.core;

import java.util.function.DoubleConsumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class GameLoop {
  private final Timeline timeline;
  private final DoubleConsumer onTick;
  private long lastNanos;

  public GameLoop(int tickMillis, DoubleConsumer onTick) {
    this.onTick = onTick;
    this.timeline = new Timeline(new KeyFrame(Duration.millis(tickMillis), event -> tick()));
    this.timeline.setCycleCount(Timeline.INDEFINITE);
  }

  public void start() {
    lastNanos = System.nanoTime();
    timeline.play();
  }

  public void stop() {
    timeline.stop();
  }

  private void tick() {
    long now = System.nanoTime();
    double deltaSeconds = (now - lastNanos) / 1_000_000_000.0;
    lastNanos = now;
    onTick.accept(deltaSeconds);
  }
}

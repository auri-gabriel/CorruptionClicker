package com.aurigabriel.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.aurigabriel.model.GameState;
import com.aurigabriel.model.UpgradeInstance;

public class SaveManager {
  private static final String VERSION_KEY = "version";
  private static final String CLEAN_MONEY_KEY = "cleanMoney";
  private static final String DIRTY_MONEY_KEY = "dirtyMoney";
  private static final String UPGRADE_PREFIX = "upgrade.";
  private static final String QUANTITY_SUFFIX = ".quantity";

  private final Path saveFile;

  public SaveManager(Path saveFile) {
    this.saveFile = saveFile;
  }

  public void save(GameState state) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(VERSION_KEY, "1");
    properties.setProperty(CLEAN_MONEY_KEY, Double.toString(state.getCleanMoney()));
    properties.setProperty(DIRTY_MONEY_KEY, Double.toString(state.getDirtyMoney()));

    for (UpgradeInstance instance : state.getUpgrades()) {
      String key = UPGRADE_PREFIX + instance.getDefinition().getId() + QUANTITY_SUFFIX;
      properties.setProperty(key, Integer.toString(instance.getQuantity()));
    }

    Files.createDirectories(saveFile.getParent());
    try (OutputStream stream = Files.newOutputStream(
        saveFile,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING)) {
      properties.store(stream, "Corruption Clicker Save");
    }
  }

  public boolean load(GameState state) throws IOException {
    if (!Files.exists(saveFile)) {
      return false;
    }

    Properties properties = new Properties();
    try (InputStream stream = Files.newInputStream(saveFile)) {
      properties.load(stream);
    }

    double cleanMoney = parseDouble(properties.getProperty(CLEAN_MONEY_KEY), 0);
    double dirtyMoney = parseDouble(properties.getProperty(DIRTY_MONEY_KEY), 0);

    Map<String, Integer> quantities = new HashMap<>();
    for (String key : properties.stringPropertyNames()) {
      if (key.startsWith(UPGRADE_PREFIX) && key.endsWith(QUANTITY_SUFFIX)) {
        String id = key.substring(UPGRADE_PREFIX.length(), key.length() - QUANTITY_SUFFIX.length());
        quantities.put(id, parseInt(properties.getProperty(key), 0));
      }
    }

    SaveData data = new SaveData(cleanMoney, dirtyMoney, quantities);
    apply(state, data);
    return true;
  }

  private void apply(GameState state, SaveData data) {
    state.setCleanMoney(data.getCleanMoney());
    state.setDirtyMoney(data.getDirtyMoney());
    for (Map.Entry<String, Integer> entry : data.getUpgradeQuantities().entrySet()) {
      state.setUpgradeQuantity(entry.getKey(), entry.getValue());
    }
  }

  private static double parseDouble(String value, double fallback) {
    if (value == null) {
      return fallback;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }

  private static int parseInt(String value, int fallback) {
    if (value == null) {
      return fallback;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }
}

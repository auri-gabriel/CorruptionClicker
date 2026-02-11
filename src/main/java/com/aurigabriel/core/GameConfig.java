package com.aurigabriel.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aurigabriel.model.UpgradeDefinition;
import com.aurigabriel.model.UpgradeType;

public final class GameConfig {
  private GameConfig() {
  }

  public static List<UpgradeDefinition> defaultUpgrades() {
    List<UpgradeDefinition> definitions = new ArrayList<>();

    definitions.add(new UpgradeDefinition("pol_ver", "Vereador", 10, 0.5, UpgradeType.POLITICIAN));
    definitions.add(new UpgradeDefinition("pol_dep_est", "Deputado Estadual", 50, 2, UpgradeType.POLITICIAN));
    definitions.add(new UpgradeDefinition("pol_dep_fed", "Deputado Federal", 200, 6, UpgradeType.POLITICIAN));
    definitions.add(new UpgradeDefinition("pol_sen", "Senador", 800, 18, UpgradeType.POLITICIAN));
    definitions.add(new UpgradeDefinition("pol_gov", "Governador", 2500, 45, UpgradeType.POLITICIAN));
    definitions.add(new UpgradeDefinition("pol_pres", "Presidente", 10000, 120, UpgradeType.POLITICIAN));

    definitions.add(new UpgradeDefinition("bus_lav", "Lavanderia", 25, 0.3, UpgradeType.BUSINESS));
    definitions.add(new UpgradeDefinition("bus_con", "Construtora", 150, 1.5, UpgradeType.BUSINESS));
    definitions.add(new UpgradeDefinition("bus_ban", "Banco", 600, 5, UpgradeType.BUSINESS));
    definitions.add(new UpgradeDefinition("bus_hol", "Holding", 2500, 16, UpgradeType.BUSINESS));
    definitions.add(new UpgradeDefinition("bus_off", "Offshore", 12000, 60, UpgradeType.BUSINESS));

    return Collections.unmodifiableList(definitions);
  }
}

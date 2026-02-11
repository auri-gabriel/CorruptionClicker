package com.aurigabriel.ui;

import javax.swing.*;

import com.aurigabriel.Game;
import com.aurigabriel.Upgrade;
import com.aurigabriel.Upgrade.UpgradeType;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameUI {

  private Game game;
  private List<Upgrade> politicians;
  private List<Upgrade> businesses;
  private Map<Upgrade, JButton> upgradeButtons;

  private JLabel cleanMoneyLabel;
  private JLabel dirtyMoneyLabel;
  private JLabel dirtyPerSecondLabel;
  private JLabel cleanFromDirtyLabel;
  private JButton manualCleanButton;

  public GameUI() {

    game = new Game();
    politicians = new ArrayList<>();
    businesses = new ArrayList<>();
    upgradeButtons = new LinkedHashMap<>();

    politicians.add(new Upgrade("Vereador", 10, 0.5, UpgradeType.POLITICIAN));
    politicians.add(new Upgrade("Deputado Estadual", 50, 2, UpgradeType.POLITICIAN));
    politicians.add(new Upgrade("Deputado Federal", 200, 6, UpgradeType.POLITICIAN));
    politicians.add(new Upgrade("Senador", 800, 18, UpgradeType.POLITICIAN));
    politicians.add(new Upgrade("Governador", 2500, 45, UpgradeType.POLITICIAN));
    politicians.add(new Upgrade("Presidente", 10000, 120, UpgradeType.POLITICIAN));

    businesses.add(new Upgrade("Lavanderia", 25, 0.3, UpgradeType.BUSINESS));
    businesses.add(new Upgrade("Construtora", 150, 1.5, UpgradeType.BUSINESS));
    businesses.add(new Upgrade("Banco", 600, 5, UpgradeType.BUSINESS));
    businesses.add(new Upgrade("Holding", 2500, 16, UpgradeType.BUSINESS));
    businesses.add(new Upgrade("Offshore", 12000, 60, UpgradeType.BUSINESS));

    JFrame frame = new JFrame("Corruption Clicker");
    frame.setSize(520, 520);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());

    // Top Panel (Stats)
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new GridLayout(4, 1));

    cleanMoneyLabel = new JLabel("Dinheiro limpo: 0");
    dirtyMoneyLabel = new JLabel("Dinheiro sujo: 0");
    dirtyPerSecondLabel = new JLabel("Sujo/seg: 0");
    cleanFromDirtyLabel = new JLabel("Lavagem/seg: 0");

    topPanel.add(cleanMoneyLabel);
    topPanel.add(dirtyMoneyLabel);
    topPanel.add(dirtyPerSecondLabel);
    topPanel.add(cleanFromDirtyLabel);

    // Center Buttons (Click + Manual Clean)
    JPanel actionsPanel = new JPanel();
    actionsPanel.setLayout(new GridLayout(2, 1));

    JButton clickButton = new JButton("Receber propina");
    clickButton.addActionListener(e -> {
      game.click();
      updateLabels();
    });

    manualCleanButton = new JButton("Lavar manualmente");
    manualCleanButton.addActionListener(e -> {
      if (game.manualClean()) {
        updateLabels();
      }
    });

    actionsPanel.add(clickButton);
    actionsPanel.add(manualCleanButton);

    // Bottom Panel (Upgrades)
    JPanel upgradesPanel = new JPanel();
    upgradesPanel.setLayout(new GridLayout(1, 2));

    JPanel politiciansPanel = new JPanel();
    politiciansPanel.setLayout(new BoxLayout(politiciansPanel, BoxLayout.Y_AXIS));
    politiciansPanel.setBorder(BorderFactory.createTitledBorder("Politicos"));

    JPanel businessesPanel = new JPanel();
    businessesPanel.setLayout(new BoxLayout(businessesPanel, BoxLayout.Y_AXIS));
    businessesPanel.setBorder(BorderFactory.createTitledBorder("Negocios"));

    for (Upgrade upgrade : politicians) {
      JButton button = new JButton(upgrade.getDisplayText());
      button.addActionListener(e -> {
        if (upgrade.buy(game)) {
          updateLabels();
        }
      });
      upgradeButtons.put(upgrade, button);
      politiciansPanel.add(button);
    }

    for (Upgrade upgrade : businesses) {
      JButton button = new JButton(upgrade.getDisplayText());
      button.addActionListener(e -> {
        if (upgrade.buy(game)) {
          updateLabels();
        }
      });
      upgradeButtons.put(upgrade, button);
      businessesPanel.add(button);
    }

    upgradesPanel.add(politiciansPanel);
    upgradesPanel.add(businessesPanel);

    frame.add(topPanel, BorderLayout.NORTH);
    frame.add(actionsPanel, BorderLayout.CENTER);
    frame.add(upgradesPanel, BorderLayout.SOUTH);

    // Game loop (10 ticks per second)
    Timer timer = new Timer(100, e -> {
      game.update();
      updateLabels();
    });
    timer.start();

    updateLabels();
    frame.setVisible(true);
  }

  private void updateLabels() {
    cleanMoneyLabel.setText("Dinheiro limpo: " + (int) game.getCleanMoney());
    dirtyMoneyLabel.setText("Dinheiro sujo: " + (int) game.getDirtyMoney());
    dirtyPerSecondLabel.setText("Sujo/seg: " + String.format("%.1f", game.getDirtyMoneyPerSecond()));
    cleanFromDirtyLabel.setText("Lavagem/seg: " + String.format("%.1f", game.getCleanFromDirtyPerSecond()));
    manualCleanButton.setEnabled(game.getDirtyMoney() >= 1);
    updateUpgradeButtons();
  }

  private void updateUpgradeButtons() {
    for (Map.Entry<Upgrade, JButton> entry : upgradeButtons.entrySet()) {
      Upgrade upgrade = entry.getKey();
      JButton button = entry.getValue();
      button.setText(upgrade.getDisplayText());
      button.setEnabled(game.getCleanMoney() >= upgrade.getCost());
    }
  }
}

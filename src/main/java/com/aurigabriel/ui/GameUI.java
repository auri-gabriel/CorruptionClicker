package com.aurigabriel.ui;

import javax.swing.*;

import com.aurigabriel.Game;
import com.aurigabriel.Upgrade;

import java.awt.*;

public class GameUI {

  private Game game;
  private Upgrade juniorOfficial;

  private JLabel influenceLabel;
  private JLabel ipsLabel;

  public GameUI() {

    game = new Game();
    juniorOfficial = new Upgrade("Junior Official", 10, 0.5);

    JFrame frame = new JFrame("CorruptioClicker");
    frame.setSize(400, 300);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout());

    // Top Panel (Stats)
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new GridLayout(2, 1));

    influenceLabel = new JLabel("Influence: 0");
    ipsLabel = new JLabel("Influence/sec: 0");

    topPanel.add(influenceLabel);
    topPanel.add(ipsLabel);

    // Center Button (Click)
    JButton clickButton = new JButton("Generate Influence");
    clickButton.addActionListener(e -> {
      game.click();
      updateLabels();
    });

    // Bottom Panel (Upgrade)
    JButton upgradeButton = new JButton(juniorOfficial.getDisplayText());
    upgradeButton.addActionListener(e -> {
      if (juniorOfficial.buy(game)) {
        upgradeButton.setText(juniorOfficial.getDisplayText());
        updateLabels();
      }
    });

    frame.add(topPanel, BorderLayout.NORTH);
    frame.add(clickButton, BorderLayout.CENTER);
    frame.add(upgradeButton, BorderLayout.SOUTH);

    // Game loop (10 ticks per second)
    Timer timer = new Timer(100, e -> {
      game.update();
      updateLabels();
    });
    timer.start();

    frame.setVisible(true);
  }

  private void updateLabels() {
    influenceLabel.setText("Influence: " + (int) game.getInfluence());
    ipsLabel.setText("Influence/sec: " + (int) game.getInfluencePerSecond());
  }
}

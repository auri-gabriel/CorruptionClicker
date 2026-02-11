package com.aurigabriel.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

import com.aurigabriel.core.GameConfig;
import com.aurigabriel.core.GameLoop;
import com.aurigabriel.model.GameState;
import com.aurigabriel.model.UpgradeInstance;
import com.aurigabriel.model.UpgradeType;
import com.aurigabriel.persistence.SaveManager;

public class GameUI {
  private static final int TICK_MILLIS = 100;
  private static final Path SAVE_PATH = Path.of(
      System.getProperty("user.home"),
      ".corruptionclicker",
      "save.properties");

  private static final DecimalFormat WHOLE_FORMAT = new DecimalFormat("0", symbols());
  private static final DecimalFormat ONE_DECIMAL_FORMAT = new DecimalFormat("0.0", symbols());
  private static final double COST_GROWTH = 1.15;
  private static final Integer[] MULTIPLIER_OPTIONS = { 1, 5, 10, 50, 100 };

  private final GameState game;
  private final SaveManager saveManager;
  private final GameLoop gameLoop;
  private final Map<UpgradeInstance, JButton> upgradeButtons;

  private final JLabel cleanMoneyLabel;
  private final JLabel dirtyMoneyLabel;
  private final JLabel dirtyPerSecondLabel;
  private final JLabel cleanFromDirtyLabel;
  private final JLabel statusLabel;
  private final JButton manualCleanButton;
  private final JComboBox<Integer> multiplierBox;

  public GameUI() {
    this.game = new GameState(GameConfig.defaultUpgrades());
    this.saveManager = new SaveManager(SAVE_PATH);
    this.upgradeButtons = new LinkedHashMap<>();

    this.cleanMoneyLabel = new JLabel();
    this.dirtyMoneyLabel = new JLabel();
    this.dirtyPerSecondLabel = new JLabel();
    this.cleanFromDirtyLabel = new JLabel();
    this.statusLabel = new JLabel(" ");
    this.manualCleanButton = new JButton("Lavar manualmente");
    this.multiplierBox = new JComboBox<>(MULTIPLIER_OPTIONS);

    JFrame frame = buildFrame();
    loadOnStartup();
    updateLabels();

    this.gameLoop = new GameLoop(TICK_MILLIS, this::onTick);
    this.gameLoop.start();

    frame.setVisible(true);
  }

  private JFrame buildFrame() {
    JFrame frame = new JFrame("Corruption Clicker");
    frame.setSize(760, 620);
    frame.setMinimumSize(new Dimension(640, 560));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout(12, 12));

    JPanel north = new JPanel(new BorderLayout(8, 8));
    north.add(buildToolbar(), BorderLayout.NORTH);
    north.add(buildStatsPanel(), BorderLayout.CENTER);
    frame.add(north, BorderLayout.NORTH);

    JPanel center = new JPanel(new BorderLayout(8, 8));
    center.add(buildActionsPanel(), BorderLayout.NORTH);
    center.add(new JScrollPane(buildUpgradesPanel()), BorderLayout.CENTER);
    frame.add(center, BorderLayout.CENTER);

    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        saveSilently("Autosave no fechamento.");
        gameLoop.stop();
      }
    });

    return frame;
  }

  private JToolBar buildToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    JButton saveButton = new JButton("Salvar");
    saveButton.addActionListener(event -> saveSilently("Jogo salvo."));

    JButton loadButton = new JButton("Carregar");
    loadButton.addActionListener(event -> {
      loadSilently();
      updateLabels();
    });

    toolbar.add(saveButton);
    toolbar.add(loadButton);

    return toolbar;
  }

  private JPanel buildStatsPanel() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
    panel.setBorder(BorderFactory.createTitledBorder("Status"));

    panel.add(cleanMoneyLabel);
    panel.add(dirtyMoneyLabel);
    panel.add(dirtyPerSecondLabel);
    panel.add(cleanFromDirtyLabel);

    return panel;
  }

  private JPanel buildActionsPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createTitledBorder("Acoes"));

    JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 8));

    JButton clickButton = new JButton("Receber propina");
    clickButton.addActionListener(event -> {
      game.click();
      updateLabels();
    });

    manualCleanButton.addActionListener(event -> {
      if (game.manualClean()) {
        updateLabels();
      }
    });

    buttons.add(clickButton);
    buttons.add(manualCleanButton);

    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

    panel.add(buttons, BorderLayout.CENTER);
    panel.add(statusLabel, BorderLayout.SOUTH);
    return panel;
  }

  private JPanel buildUpgradesPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(BorderFactory.createTitledBorder("Upgrades"));

    JPanel controls = new JPanel(new BorderLayout(8, 8));
    JLabel multiplierLabel = new JLabel("Comprar:");
    multiplierBox.addActionListener(event -> updateLabels());
    controls.add(multiplierLabel, BorderLayout.WEST);
    controls.add(multiplierBox, BorderLayout.CENTER);

    JPanel lists = new JPanel(new GridLayout(1, 2, 12, 12));
    lists.add(buildUpgradeList("Politicos", UpgradeType.POLITICIAN));
    lists.add(buildUpgradeList("Negocios", UpgradeType.BUSINESS));

    panel.add(controls, BorderLayout.NORTH);
    panel.add(lists, BorderLayout.CENTER);

    return panel;
  }

  private JScrollPane buildUpgradeList(String title, UpgradeType type) {
    JPanel list = new JPanel(new GridLayout(0, 1, 6, 6));
    list.setBorder(BorderFactory.createTitledBorder(title));

    for (UpgradeInstance instance : game.getUpgrades()) {
      if (instance.getDefinition().getType() != type) {
        continue;
      }

      JButton button = new JButton();
      button.addActionListener(event -> {
        buyUpgradeMultiple(instance.getDefinition().getId());
      });
      upgradeButtons.put(instance, button);
      list.add(button);
    }

    JScrollPane scroll = new JScrollPane(list);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    return scroll;
  }

  private void onTick(double deltaSeconds) {
    game.update(deltaSeconds);
    updateLabels();
  }

  private void updateLabels() {
    cleanMoneyLabel.setText("Dinheiro limpo: " + formatMoney(game.getCleanMoney()));
    dirtyMoneyLabel.setText("Dinheiro sujo: " + formatMoney(game.getDirtyMoney()));
    dirtyPerSecondLabel.setText("Sujo/seg: " + formatRate(game.getDirtyMoneyPerSecond()));
    cleanFromDirtyLabel.setText("Lavagem/seg: " + formatRate(game.getCleanFromDirtyPerSecond()));
    manualCleanButton.setEnabled(game.getDirtyMoney() >= 1);
    updateUpgradeButtons();
  }

  private void updateUpgradeButtons() {
    int multiplier = getSelectedMultiplier();
    for (Map.Entry<UpgradeInstance, JButton> entry : upgradeButtons.entrySet()) {
      UpgradeInstance instance = entry.getKey();
      JButton button = entry.getValue();
      String name = instance.getDefinition().getName();
      String cost = formatMoney(calculateTotalCost(instance, multiplier));
      int quantity = instance.getQuantity();
      String costLabel = multiplier == 1 ? "Custo: " : "Custo x" + multiplier + ": ";
      button.setText(name + " | " + costLabel + cost + " | Qtde: " + quantity);
      button.setEnabled(game.getCleanMoney() >= calculateTotalCost(instance, multiplier));
    }
  }

  private int getSelectedMultiplier() {
    Integer selected = (Integer) multiplierBox.getSelectedItem();
    return selected == null ? 1 : selected;
  }

  private void buyUpgradeMultiple(String upgradeId) {
    int multiplier = getSelectedMultiplier();
    boolean purchased = false;
    for (int i = 0; i < multiplier; i++) {
      if (!game.buyUpgrade(upgradeId)) {
        break;
      }
      purchased = true;
    }
    if (purchased) {
      updateLabels();
    }
  }

  private double calculateTotalCost(UpgradeInstance instance, int count) {
    if (count <= 0) {
      return 0;
    }
    double baseCost = instance.getDefinition().getBaseCost();
    int quantity = instance.getQuantity();
    double total = 0;
    for (int i = 0; i < count; i++) {
      total += baseCost * Math.pow(COST_GROWTH, quantity + i);
    }
    return total;
  }

  private void loadOnStartup() {
    loadSilently();
  }

  private void saveSilently(String message) {
    try {
      saveManager.save(game);
      statusLabel.setText(message);
    } catch (Exception ex) {
      statusLabel.setText("Falha ao salvar.");
    }
  }

  private void loadSilently() {
    try {
      if (saveManager.load(game)) {
        statusLabel.setText("Jogo carregado.");
      } else {
        statusLabel.setText("Nenhum save encontrado.");
      }
    } catch (Exception ex) {
      statusLabel.setText("Falha ao carregar.");
    }
  }

  private static String formatMoney(double value) {
    return formatCompact(value, 0, 1);
  }

  private static String formatRate(double value) {
    return formatCompact(value, 1, 1);
  }

  private static String formatCompact(double value, int smallDecimals, int largeDecimals) {
    double abs = Math.abs(value);
    double display = value;
    String suffix = "";
    int decimals = smallDecimals;

    if (abs >= 1_000_000_000) {
      display = value / 1_000_000_000.0;
      suffix = "B";
      decimals = largeDecimals;
    } else if (abs >= 1_000_000) {
      display = value / 1_000_000.0;
      suffix = "M";
      decimals = largeDecimals;
    } else if (abs >= 1_000) {
      display = value / 1_000.0;
      suffix = "K";
      decimals = largeDecimals;
    }

    DecimalFormat format = decimals == 0 ? WHOLE_FORMAT : ONE_DECIMAL_FORMAT;
    return format.format(display) + suffix;
  }

  private static DecimalFormatSymbols symbols() {
    return DecimalFormatSymbols.getInstance(Locale.US);
  }
}

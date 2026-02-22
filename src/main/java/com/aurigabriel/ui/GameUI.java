package com.aurigabriel.ui;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.aurigabriel.core.GameConfig;
import com.aurigabriel.core.GameLoop;
import com.aurigabriel.model.GameState;
import com.aurigabriel.model.UpgradeInstance;
import com.aurigabriel.model.UpgradeType;
import com.aurigabriel.persistence.SaveManager;
import com.aurigabriel.persistence.SaveSlotInfo;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameUI extends Application {
  private static final int TICK_MILLIS = 100;
  private static final Path SAVE_DIR = Path.of(
      System.getProperty("user.home"),
      ".corruptionclicker");

  private static final DecimalFormat WHOLE_FORMAT = new DecimalFormat("0", symbols());
  private static final DecimalFormat ONE_DECIMAL_FORMAT = new DecimalFormat("0.0", symbols());
  private static final double COST_GROWTH = 1.15;
  private static final Integer[] MULTIPLIER_OPTIONS = { 1, 5, 10, 50, 100 };
  private static final Integer[] SAVE_SLOTS = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final DateTimeFormatter SAVE_TIME_FORMAT = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm")
      .withZone(ZoneId.systemDefault());

  private GameState game;
  private SaveManager saveManager;
  private GameLoop gameLoop;
  private Map<UpgradeInstance, Button> upgradeButtons;
  private Map<Integer, SaveSlotRow> saveSlotRows;
  private int activeSlot;
  private Stage savesStage;

  private Label cleanMoneyLabel;
  private Label dirtyMoneyLabel;
  private Label dirtyPerSecondLabel;
  private Label cleanFromDirtyLabel;
  private Label statusLabel;
  private Button manualCleanButton;
  private ComboBox<Integer> multiplierBox;

  @Override
  public void start(Stage stage) {
    initState();

    Parent root = buildContent();
    Scene scene = new Scene(root, 760, 620);
    scene.getStylesheets().add(GameUI.class.getResource("/styles/pixel.css").toExternalForm());

    stage.setTitle("Corruption Clicker");
    stage.setMinWidth(640);
    stage.setMinHeight(560);
    stage.setScene(scene);

    stage.setOnCloseRequest(event -> {
      saveSilently(activeSlot, "Autosave no fechamento.");
      if (gameLoop != null) {
        gameLoop.stop();
      }
    });

    loadOnStartup();
    updateLabels();

    gameLoop = new GameLoop(TICK_MILLIS, this::onTick);
    gameLoop.start();

    stage.show();
  }

  private void initState() {
    this.game = new GameState(GameConfig.defaultUpgrades());
    this.saveManager = new SaveManager(SAVE_DIR);
    this.upgradeButtons = new LinkedHashMap<>();
    this.saveSlotRows = new LinkedHashMap<>();
    this.activeSlot = SAVE_SLOTS[0];

    this.cleanMoneyLabel = new Label();
    this.cleanMoneyLabel.getStyleClass().add("stat-label");
    this.dirtyMoneyLabel = new Label();
    this.dirtyMoneyLabel.getStyleClass().add("stat-label");
    this.dirtyPerSecondLabel = new Label();
    this.dirtyPerSecondLabel.getStyleClass().add("stat-label");
    this.cleanFromDirtyLabel = new Label();
    this.cleanFromDirtyLabel.getStyleClass().add("stat-label");
    this.statusLabel = new Label(" ");
    this.statusLabel.getStyleClass().add("status-line");
    this.manualCleanButton = new Button("Lavar manualmente");
    this.multiplierBox = new ComboBox<>();
    this.multiplierBox.getItems().addAll(MULTIPLIER_OPTIONS);
    this.multiplierBox.setValue(MULTIPLIER_OPTIONS[0]);
  }

  private Parent buildContent() {
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(12));
    root.getStyleClass().add("game-root");

    Label title = new Label("CORRUPTION CLICKER");
    title.getStyleClass().add("game-title");

    VBox north = new VBox(8, title, buildStatsPanel());
    VBox center = new VBox(8, buildActionsPanel(), buildUpgradesPanel());

    root.setTop(north);
    root.setCenter(center);

    return root;
  }

  private TitledPane buildStatsPanel() {
    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(8);

    grid.add(cleanMoneyLabel, 0, 0);
    grid.add(dirtyMoneyLabel, 1, 0);
    grid.add(dirtyPerSecondLabel, 0, 1);
    grid.add(cleanFromDirtyLabel, 1, 1);

    return wrapPane("Status", grid);
  }

  private TitledPane buildActionsPanel() {
    Button clickButton = new Button("Receber propina");
    clickButton.setOnAction(event -> {
      playClickPulse(clickButton);
      game.click();
      updateLabels();
    });
    clickButton.getStyleClass().add("primary-action");

    manualCleanButton.setOnAction(event -> {
      if (game.manualClean()) {
        playClickPulse(manualCleanButton);
        updateLabels();
      }
    });
    manualCleanButton.getStyleClass().add("secondary-action");

    Button savesButton = new Button("Saves");
    savesButton.setOnAction(event -> openSavesWindow());

    HBox buttons = new HBox(8, clickButton, manualCleanButton, savesButton);

    statusLabel.setAlignment(Pos.CENTER);
    statusLabel.setMaxWidth(Double.MAX_VALUE);

    VBox content = new VBox(8, buttons, statusLabel);
    return wrapPane("Acoes", content);
  }

  private TitledPane buildUpgradesPanel() {
    Label multiplierLabel = new Label("Comprar:");
    multiplierBox.setOnAction(event -> updateLabels());

    HBox controls = new HBox(8, multiplierLabel, multiplierBox);
    controls.setAlignment(Pos.CENTER_LEFT);

    TitledPane politicians = buildUpgradeList("Politicos", UpgradeType.POLITICIAN);
    TitledPane businesses = buildUpgradeList("Negocios", UpgradeType.BUSINESS);

    HBox lists = new HBox(12, politicians, businesses);
    HBox.setHgrow(politicians, Priority.ALWAYS);
    HBox.setHgrow(businesses, Priority.ALWAYS);

    VBox content = new VBox(8, controls, lists);
    return wrapPane("Upgrades", content);
  }

  private TitledPane buildSavesPanel() {
    VBox list = new VBox(6);
    for (Integer slot : SAVE_SLOTS) {
      SaveSlotRow row = buildSaveSlotRow(slot);
      saveSlotRows.put(slot, row);
      list.getChildren().add(row.root);
    }

    ScrollPane scroll = new ScrollPane(list);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    TitledPane pane = wrapPane("Saves", scroll);
    return pane;
  }

  private void openSavesWindow() {
    if (savesStage == null) {
      Parent content = buildSavesPanel();
      Scene scene = new Scene(content, 520, 560);
      scene.getStylesheets().add(GameUI.class.getResource("/styles/pixel.css").toExternalForm());

      savesStage = new Stage();
      savesStage.setTitle("Saves");
      savesStage.setMinWidth(420);
      savesStage.setMinHeight(420);
      savesStage.setScene(scene);
      savesStage.setOnCloseRequest(event -> {
        event.consume();
        savesStage.hide();
      });
    }

    refreshSaveSlots();
    savesStage.show();
    savesStage.toFront();
  }

  private SaveSlotRow buildSaveSlotRow(int slot) {
    Label title = new Label();
    title.getStyleClass().add("stat-label");

    Label summary = new Label();
    summary.getStyleClass().add("status-line");
    summary.setMaxWidth(Double.MAX_VALUE);

    Button loadButton = new Button("Carregar");
    loadButton.setOnAction(event -> {
      loadSilently(slot);
      updateLabels();
    });

    Button saveButton = new Button("Salvar");
    saveButton.setOnAction(event -> saveSilently(slot, "Jogo salvo."));

    HBox row = new HBox(8, title, summary, loadButton, saveButton);
    row.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(summary, Priority.ALWAYS);
    row.getStyleClass().add("hud-bar");

    return new SaveSlotRow(slot, row, title, summary, loadButton, saveButton);
  }

  private TitledPane buildUpgradeList(String title, UpgradeType type) {
    VBox list = new VBox(6);

    for (UpgradeInstance instance : game.getUpgrades()) {
      if (instance.getDefinition().getType() != type) {
        continue;
      }

      Button button = new Button();
      button.setMaxWidth(Double.MAX_VALUE);
      button.setOnAction(event -> {
        playClickPulse(button);
        buyUpgradeMultiple(instance.getDefinition().getId());
      });
      button.getStyleClass().add("shop-button");
      upgradeButtons.put(instance, button);
      list.getChildren().add(button);
    }

    ScrollPane scroll = new ScrollPane(list);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    TitledPane titled = new TitledPane(title, scroll);
    titled.setCollapsible(false);
    titled.setMaxWidth(Double.MAX_VALUE);
    titled.getStyleClass().add("pixel-pane");
    return titled;
  }

  private TitledPane wrapPane(String title, Parent content) {
    TitledPane pane = new TitledPane(title, content);
    pane.setCollapsible(false);
    pane.getStyleClass().add("pixel-pane");
    return pane;
  }

  private void playClickPulse(Node node) {
    ScaleTransition pulse = new ScaleTransition(Duration.millis(120), node);
    pulse.setFromX(1.0);
    pulse.setFromY(1.0);
    pulse.setToX(1.06);
    pulse.setToY(1.06);
    pulse.setAutoReverse(true);
    pulse.setCycleCount(2);
    pulse.play();
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
    manualCleanButton.setDisable(game.getDirtyMoney() < 1);
    updateUpgradeButtons();
  }

  private void updateUpgradeButtons() {
    int multiplier = getSelectedMultiplier();
    for (Map.Entry<UpgradeInstance, Button> entry : upgradeButtons.entrySet()) {
      UpgradeInstance instance = entry.getKey();
      Button button = entry.getValue();
      String name = instance.getDefinition().getName();
      String cost = formatMoney(calculateTotalCost(instance, multiplier));
      int quantity = instance.getQuantity();
      String costLabel = multiplier == 1 ? "Custo: " : "Custo x" + multiplier + ": ";
      button.setText(name + " | " + costLabel + cost + " | Qtde: " + quantity);
      button.setDisable(game.getCleanMoney() < calculateTotalCost(instance, multiplier));
    }
  }

  private int getSelectedMultiplier() {
    Integer selected = multiplierBox.getValue();
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
    loadSilently(activeSlot);
  }

  private void saveSilently(int slot, String message) {
    try {
      saveManager.save(game, slot);
      activeSlot = slot;
      refreshSaveSlots();
      statusLabel.setText(message);
    } catch (Exception ex) {
      statusLabel.setText("Falha ao salvar.");
    }
  }

  private void loadSilently(int slot) {
    try {
      if (saveManager.load(game, slot)) {
        activeSlot = slot;
        statusLabel.setText("Jogo carregado.");
      } else {
        statusLabel.setText("Nenhum save encontrado.");
      }
      refreshSaveSlots();
    } catch (Exception ex) {
      statusLabel.setText("Falha ao carregar.");
    }
  }

  private void refreshSaveSlots() {
    if (saveSlotRows.isEmpty()) {
      return;
    }
    for (SaveSlotRow row : saveSlotRows.values()) {
      try {
        SaveSlotInfo info = saveManager.readSlotInfo(row.slot);
        row.titleLabel.setText(formatSlotTitle(info.getSlot()));
        row.summaryLabel.setText(formatSlotSummary(info));
      } catch (Exception ex) {
        row.titleLabel.setText(formatSlotTitle(row.slot));
        row.summaryLabel.setText("Erro ao ler slot.");
      }
    }
  }

  private String formatSlotTitle(int slot) {
    String title = "Slot " + slot;
    if (slot == activeSlot) {
      title += " (ativo)";
    }
    return title;
  }

  private String formatSlotSummary(SaveSlotInfo info) {
    if (!info.exists()) {
      return "Vazio";
    }
    String when = SAVE_TIME_FORMAT.format(Instant.ofEpochMilli(info.getLastModifiedMillis()));
    return "Limp: " + formatMoney(info.getCleanMoney()) + " | Sujo: " + formatMoney(info.getDirtyMoney()) + " | "
        + when;
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

  private static final class SaveSlotRow {
    private final int slot;
    private final HBox root;
    private final Label titleLabel;
    private final Label summaryLabel;
    private final Button loadButton;
    private final Button saveButton;

    private SaveSlotRow(int slot, HBox root, Label titleLabel, Label summaryLabel, Button loadButton,
        Button saveButton) {
      this.slot = slot;
      this.root = root;
      this.titleLabel = titleLabel;
      this.summaryLabel = summaryLabel;
      this.loadButton = loadButton;
      this.saveButton = saveButton;
    }
  }
}

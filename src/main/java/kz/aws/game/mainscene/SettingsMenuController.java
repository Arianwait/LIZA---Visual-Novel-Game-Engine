package kz.aws.game.mainscene;

import java.io.File;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.appsettings.JsonConfigWriter;
import kz.aws.game.panel.Panel;
import kz.aws.game.panel.PanelRegistry;
import kz.aws.game.utils.UiConfigParser;
import kz.aws.game.utils.UiFactory;
import kz.aws.game.utils.VirtualViewport;

/**
 * FXML-контроллер панели настроек. Все размеры — в пикселях
 * дизайн-разрешения {@link VirtualViewport}.
 */
@Panel("settings")
public class SettingsMenuController extends VBox {

    private static final String FXML_PATH = "lib/fxml/settings-menu.fxml";

    @FXML private VBox menuPanel;
    @FXML private CheckBox fullscreenCheckBox;
    @FXML private ChoiceBox<String> resolutionChoiceBox;
    @FXML private Slider volumeSlider;

    private AppSettings appSettings;

    /**
     * Конструктор для PanelRegistry (вызывается через рефлексию по аннотации @Panel).
     *
     * @param appSettings настройки приложения
     */
    public SettingsMenuController(AppSettings appSettings) {
        this.appSettings = appSettings;
        loadFxml();
        initialize();
    }

    /**
     * Загружает FXML из файловой системы (fx:root pattern).
     */
    private void loadFxml() {
        try {
            File fxmlFile = new File(FXML_PATH);
            URL fxmlUrl = fxmlFile.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load settings-menu.fxml", e);
        }
    }

    /**
     * Инициализация после загрузки FXML: настраивает контролы, биндинги и кнопки.
     */
    private void initialize() {
        setupFullscreenCheckBox();
        setupResolutionChoiceBox();
        setupVolumeSlider();
        applyControlFontSizes();
        applyPanelMargins();
        createActionButtons();
        createSubmenuButtons();
    }

    /**
     * Настраивает чекбокс полного экрана и связывает с доступностью выбора разрешения.
     */
    private void setupFullscreenCheckBox() {
        fullscreenCheckBox.setSelected(appSettings.isFullscreen());
        fullscreenCheckBox.selectedProperty().addListener(
                (obs, oldVal, newVal) -> resolutionChoiceBox.setDisable(newVal));
        resolutionChoiceBox.setDisable(fullscreenCheckBox.isSelected());
    }

    /**
     * Заполняет список разрешений (только 16:9 — без полос летербокса)
     * и устанавливает текущее значение.
     */
    private void setupResolutionChoiceBox() {
        resolutionChoiceBox.getItems().addAll("1280x720", "1600x900", "1920x1080");
        String current = appSettings.getWindowWidth() + "x" + appSettings.getWindowHeight();
        if (!resolutionChoiceBox.getItems().contains(current)) {
            current = "1280x720";
        }
        resolutionChoiceBox.setValue(current);
    }

    /**
     * Устанавливает начальное значение слайдера громкости и привязывает к медиаплееру.
     */
    private void setupVolumeSlider() {
        volumeSlider.setValue(appSettings.getVolumeValue());
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // плеера может не быть: музыка меню не загрузилась или уже остановлена
            if (appSettings.getMediaPlayer() != null) {
                appSettings.getMediaPlayer().setVolume(newVal.doubleValue());
            }
            appSettings.setVolumeValue(newVal.doubleValue());
        });
    }

    /**
     * Устанавливает размер шрифта контролов в дизайн-пикселях.
     */
    private void applyControlFontSizes() {
        String fontStyle = String.format("-fx-font-size: %.0fpx;",
                VirtualViewport.height(0.018));
        fullscreenCheckBox.setStyle(fontStyle);
        resolutionChoiceBox.setStyle(fontStyle);
    }

    /**
     * Устанавливает внешние отступы menuPanel в дизайн-пикселях.
     */
    private void applyPanelMargins() {
        VBox.setMargin(menuPanel, new Insets(0,
                VirtualViewport.width(0.7),
                VirtualViewport.height(0.2),
                VirtualViewport.width(0.05)));
    }

    /**
     * Создаёт кнопки "Сохранить" и "Отмена" с адаптивными размерами.
     */
    private void createActionButtons() {
        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> saveSettingsAndReturn());
        styleAndAddButton(saveButton);

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> PanelRegistry.show("main-menu", appSettings));
        styleAndAddButton(cancelButton);
    }

    /**
     * Создаёт дополнительные кнопки из Buttons.xml по контексту "submenu".
     */
    private void createSubmenuButtons() {
        for (UiConfigParser.ButtonConfig btnCfg : UiConfigParser.getButtonsByContext("submenu")) {
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            styleAndAddButton(btn);
        }
    }

    /**
     * Применяет стиль и размеры к кнопке и добавляет в menuPanel:
     * ширина растягивается по панели, высота фиксированная.
     *
     * @param button кнопка для стилизации
     */
    private void styleAndAddButton(Button button) {
        button.getStyleClass().add("game-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(VirtualViewport.height(0.05));
        ButtonAnimation.addButtonHoverAnimation(button);
        menuPanel.getChildren().add(button);
    }

    /**
     * Сохраняет настройки (полноэкранный режим, разрешение, громкость),
     * записывает конфиг и применяет изменения к Stage.
     */
    private void saveSettingsAndReturn() {
        applySettings();
        JsonConfigWriter.writeConfig(appSettings);
        applyStageSize();
        PanelRegistry.show("main-menu", appSettings);
    }

    /**
     * Записывает выбранные значения в appSettings.
     */
    private void applySettings() {
        appSettings.setFullscreen(fullscreenCheckBox.isSelected());
        String[] parts = resolutionChoiceBox.getValue().split("x");
        appSettings.setWindowWidth(Integer.parseInt(parts[0]));
        appSettings.setWindowHeight(Integer.parseInt(parts[1]));
        appSettings.setVolumeValue(volumeSlider.getValue());
    }

    /**
     * Применяет размер окна или полноэкранный режим к Stage.
     */
    private void applyStageSize() {
        Stage stage = appSettings.getStage();
        boolean wasFullscreen = stage.isFullScreen();
        stage.setFullScreen(appSettings.isFullscreen());
        if (!appSettings.isFullscreen()) {
            if (wasFullscreen) {
                javafx.application.Platform.runLater(() -> applyWindowedSize(stage));
            } else {
                applyWindowedSize(stage);
            }
        }
    }

    /**
     * Задаёт размер окна так, чтобы игровая область (Scene) была ровно
     * выбранного разрешения: к целевому размеру добавляется рамка окна,
     * иначе область получается меньше и не 16:9 — появляются полосы.
     *
     * @param stage основной Stage
     */
    private void applyWindowedSize(Stage stage) {
        javafx.scene.Scene scene = appSettings.getScene();
        double decorWidth = Math.max(0, stage.getWidth() - scene.getWidth());
        double decorHeight = Math.max(0, stage.getHeight() - scene.getHeight());
        stage.setWidth(appSettings.getWindowWidth() + decorWidth);
        stage.setHeight(appSettings.getWindowHeight() + decorHeight);
        stage.centerOnScreen();
    }
}

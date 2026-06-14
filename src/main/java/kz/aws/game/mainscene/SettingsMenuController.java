package kz.aws.game.mainscene;

import java.io.File;
import java.net.URL;

import javafx.beans.binding.Bindings;
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

/**
 * FXML-контроллер панели настроек. Заменяет программную сборку UI из SettingsMenu.java
 * на декларативный FXML с адаптивными биндингами для корректного масштабирования.
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
        bindResponsiveFontSizes();
        bindResponsiveMargins();
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
     * Заполняет список разрешений и устанавливает текущее значение.
     */
    private void setupResolutionChoiceBox() {
        resolutionChoiceBox.getItems().addAll("1024x768", "1280x720", "1920x1080");
        resolutionChoiceBox.setValue(
                appSettings.getWindowWidth() + "x" + appSettings.getWindowHeight());
    }

    /**
     * Устанавливает начальное значение слайдера громкости и привязывает к медиаплееру.
     */
    private void setupVolumeSlider() {
        volumeSlider.setValue(appSettings.getVolumeValue());
        volumeSlider.valueProperty().addListener(
                (obs, oldVal, newVal) -> appSettings.getMediaPlayer().setVolume(newVal.doubleValue()));
    }

    /**
     * Привязывает размер шрифта контролов к высоте Stage через property-биндинг.
     */
    private void bindResponsiveFontSizes() {
        Stage stage = appSettings.getStagePain();
        fullscreenCheckBox.styleProperty().bind(Bindings.format(
                "-fx-font-size: %.0fpx;", stage.heightProperty().multiply(0.018)));
        resolutionChoiceBox.styleProperty().bind(Bindings.format(
                "-fx-font-size: %.0fpx;", stage.heightProperty().multiply(0.018)));
    }

    /**
     * Привязывает внешние отступы menuPanel к размерам Stage через listener.
     */
    private void bindResponsiveMargins() {
        Stage stage = appSettings.getStagePain();
        Runnable updateMargin = () -> VBox.setMargin(menuPanel, new Insets(0,
                stage.getWidth() * 0.7,
                stage.getHeight() * 0.2,
                stage.getWidth() * 0.05));
        stage.widthProperty().addListener((obs, o, n) -> updateMargin.run());
        stage.heightProperty().addListener((obs, o, n) -> updateMargin.run());
        updateMargin.run();
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
     * Применяет стиль и адаптивные размеры к кнопке и добавляет в menuPanel.
     *
     * @param button кнопка для стилизации
     */
    private void styleAndAddButton(Button button) {
        button.getStyleClass().add("game-button");
        button.prefWidthProperty().bind(menuPanel.widthProperty().multiply(1));
        button.prefHeightProperty().bind(menuPanel.heightProperty().multiply(0.1));
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
        Stage stage = appSettings.getStagePain();
        stage.setFullScreen(appSettings.isFullscreen());
        if (!appSettings.isFullscreen()) {
            stage.setWidth(appSettings.getWindowWidth());
            stage.setHeight(appSettings.getWindowHeight());
        }
    }
}

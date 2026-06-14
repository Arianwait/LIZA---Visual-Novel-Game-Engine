package kz.aws.game.mainscene;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.Panel;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.MainMenuConfigParser;
import kz.aws.game.utils.MainMenuConfigParser.MainMenuConfig;
import kz.aws.game.utils.MenuResourceCache;
import kz.aws.game.utils.UiConfigParser.ButtonConfig;
import kz.aws.game.utils.UiFactory;

/**
 * FXML-контроллер главного меню. Заменяет программную сборку UI из MainMenu.java
 * на декларативный FXML с адаптивными property-биндингами для корректного масштабирования.
 */
@Panel("main-menu")
public class MainMenuController extends VBox {

    @FXML private VBox menuPanel;
    @FXML private Label titleLabel;

    private AppSettings appSettings;
    private MainMenuConfig config;

    /**
     * Конструктор для PanelRegistry (вызывается через рефлексию по аннотации @Panel).
     *
     * @param appSettings настройки приложения
     */
    public MainMenuController(AppSettings appSettings) {
        this.appSettings = appSettings;
        this.config = MainMenuConfigParser.getConfig();
        MainMenuFxmlLoader.loadInto(this);
        initialize();
    }

    /**
     * Инициализация после загрузки FXML: настраивает заголовок, кнопки и биндинги.
     */
    private void initialize() {
        preloadResources();
        setupTitle();
        bindResponsiveMargins();
        applyMenuPanelSpacing();
        createButtons();
        appSettings.setMainMenuContentPane(this);
        SceneInfo.setHboxButton(menuPanel);
    }

    /**
     * Предзагрузка ресурсов меню через кеш.
     */
    private void preloadResources() {
        if (!MenuResourceCache.isPreloaded()) {
            MenuResourceCache.preloadMenuResources();
        }
    }

    /**
     * Настраивает заголовок: текст и адаптивный размер шрифта через биндинг к высоте Stage.
     */
    private void setupTitle() {
        titleLabel.setText(config.titleText);
        Stage stage = appSettings.getStagePain();
        titleLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("System", FontWeight.findByName(config.titleWeight),
                        stage.getHeight() * config.titleFontSizeMultiplier),
                stage.heightProperty()));
    }

    /**
     * Привязывает внешние отступы (margin) menuPanel к размерам Stage через listener,
     * чтобы при смене разрешения отступы пересчитывались автоматически.
     */
    private void bindResponsiveMargins() {
        Stage stage = appSettings.getStagePain();
        Runnable updateMargin = () -> VBox.setMargin(menuPanel, new Insets(0,
                stage.getWidth() * config.marginRight,
                stage.getHeight() * config.marginTop,
                stage.getWidth() * config.marginLeft));
        stage.widthProperty().addListener((obs, o, n) -> updateMargin.run());
        stage.heightProperty().addListener((obs, o, n) -> updateMargin.run());
        updateMargin.run();
    }

    /**
     * Устанавливает внутренний padding и spacing menuPanel из конфига.
     */
    private void applyMenuPanelSpacing() {
        menuPanel.setPadding(new Insets(config.menuPadding));
        menuPanel.setSpacing(config.menuSpacing);
    }

    /**
     * Создаёт кнопки из Buttons.xml по контексту "main-menu" и добавляет в menuPanel.
     */
    private void createButtons() {
        List<ButtonConfig> buttonConfigs =
                kz.aws.game.utils.UiConfigParser.getButtonsByContext("main-menu");
        for (ButtonConfig btnCfg : buttonConfigs) {
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            bindButtonSize(btn);
            menuPanel.getChildren().add(btn);
        }
    }

    /**
     * Привязывает размер кнопки к размерам menuPanel через property-биндинг.
     *
     * @param button кнопка для привязки
     */
    private void bindButtonSize(Button button) {
        button.getStyleClass().add("game-button");
        button.prefWidthProperty().bind(menuPanel.widthProperty().multiply(1));
        button.prefHeightProperty().bind(menuPanel.heightProperty().multiply(0.1));
        ButtonAnimation.addButtonHoverAnimation(button);
    }
}

package kz.aws.game.mainscene;

import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.Panel;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.MainMenuConfigParser;
import kz.aws.game.utils.MainMenuConfigParser.MainMenuConfig;
import kz.aws.game.utils.MenuResourceCache;
import kz.aws.game.utils.UiConfigParser.ButtonConfig;
import kz.aws.game.utils.UiFactory;
import kz.aws.game.utils.VirtualViewport;

/**
 * FXML-контроллер главного меню. Все размеры и отступы задаются один раз
 * в пикселях дизайн-разрешения {@link VirtualViewport}.
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
        applyPanelMargins();
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
     * Настраивает заголовок: текст и размер шрифта в дизайн-пикселях.
     */
    private void setupTitle() {
        titleLabel.setText(config.titleText);
        titleLabel.setFont(Font.font("System", FontWeight.findByName(config.titleWeight),
                VirtualViewport.height(config.titleFontSizeMultiplier)));
    }

    /**
     * Устанавливает внешние отступы (margin) menuPanel из конфигурации
     * в дизайн-пикселях.
     */
    private void applyPanelMargins() {
        VBox.setMargin(menuPanel, new Insets(0,
                VirtualViewport.width(config.marginRight),
                VirtualViewport.height(config.marginTop),
                VirtualViewport.width(config.marginLeft)));
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
     * Задаёт размер кнопки: ширина растягивается по панели,
     * высота фиксированная в дизайн-пикселях.
     *
     * @param button кнопка для настройки
     */
    private void bindButtonSize(Button button) {
        button.getStyleClass().add("game-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(VirtualViewport.height(0.05));
        ButtonAnimation.addButtonHoverAnimation(button);
    }
}

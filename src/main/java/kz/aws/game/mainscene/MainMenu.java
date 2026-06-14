package kz.aws.game.mainscene;

import java.io.Serializable;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.MainMenuConfigParser;
import kz.aws.game.utils.MainMenuConfigParser.MainMenuConfig;
import kz.aws.game.utils.MenuResourceCache;
import kz.aws.game.utils.UiConfigParser.ButtonConfig;
import kz.aws.game.utils.UiFactory;

/**
 * Устаревшая панель главного меню (заменена на {@link MainMenuController} с FXML).
 * Оставлена для обратной совместимости конструктора (Stage, AppSettings).
 *
 * @deprecated используйте {@link MainMenuController}
 */
@Deprecated
public class MainMenu extends VBox implements Serializable {

    private static final long serialVersionUID = -1714663315168831818L;

    /**
     * Конструктор для встраивания в сцену (ShowMainMenu создаёт контейнер и вызывает PanelRegistry.show).
     * Не используется при навигации по панелям — там вызывается конструктор (AppSettings).
     */
    public MainMenu(Stage primaryStage, AppSettings appSettings) {
        appSettings.setMainMenuContentPane(this);
        buildUI(primaryStage, appSettings);
    }

    /**
     * Конструктор для регистрации как панели по id "main-menu".
     * Создаёт только UI (заголовок и кнопки из Buttons.xml); контейнер задаётся снаружи.
     */
    public MainMenu(AppSettings appSettings) {
        buildUI(appSettings.getStagePain(), appSettings);
    }

    /**
     * Предзагрузка ресурсов главного меню. Использует кеш для предотвращения повторной загрузки.
     */
    private static void preloadMenuResources(AppSettings appSettings) {
        if (MenuResourceCache.isPreloaded()) {
            return;
        }
        MenuResourceCache.preloadMenuResources();
    }

    /**
     * Собирает UI панели: отступы, заголовок, кнопки по контексту main-menu из Buttons.xml.
     */
    @SuppressWarnings("static-access")
    private void buildUI(Stage stage, AppSettings appSettings) {
        preloadMenuResources(appSettings);
        MainMenuConfig config = MainMenuConfigParser.getConfig();

        setSpacing(config.menuSpacing);

        Label titleLabel = new Label(config.titleText);
        titleLabel.getStyleClass().add("hitech-panel-title");
        titleLabel.setStyle("-fx-font-size: " + appSettings.getStagePain().getHeight() * config.titleFontSizeMultiplier + "px; -fx-font-weight: " + config.titleWeight + ";");

        VBox menuItem = new VBox(10);
        SceneInfo.setHboxButton(menuItem);
        this.setMargin(menuItem, new Insets(0, appSettings.getStagePain().getWidth() * config.marginRight,
                appSettings.getStagePain().getHeight() * config.marginTop,
                appSettings.getStagePain().getWidth() * config.marginLeft));
        menuItem.getStyleClass().add("hitech-panel");
        menuItem.getChildren().add(titleLabel);
        menuItem.setAlignment(Pos.CENTER);
        menuItem.setScaleY(1.0);

        for (ButtonConfig btnCfg : kz.aws.game.utils.UiConfigParser.getButtonsByContext("main-menu")) {
            Button btn = UiFactory.createButtonFromConfig(btnCfg, appSettings);
            menuItem.getChildren().add(btn);
            appSettings.ButtonStyle(btn, menuItem);
        }
        menuItem.setPadding(new Insets(config.menuPadding));

        this.getChildren().add(menuItem);
    }
}

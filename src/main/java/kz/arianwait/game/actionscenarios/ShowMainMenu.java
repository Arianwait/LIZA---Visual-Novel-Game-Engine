package kz.arianwait.game.actionscenarios;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.engine.render.SceneRenderer;
import kz.arianwait.game.panel.PanelRegistry;
import kz.arianwait.game.scenedetails.DialogPanelController;
import kz.arianwait.game.scenedetails.GamePauseMenuController;
import kz.arianwait.game.scenelist.SceneInfo;
import kz.arianwait.game.soundtrack.SoundEffect;
import kz.arianwait.game.soundtrack.Soundtrack;
import kz.arianwait.game.utils.MainMenuConfigParser;
import kz.arianwait.game.utils.MenuResourceCache;
import kz.arianwait.game.utils.ThemesConfigParser;

/**
 * Инициализация сцены главного меню: фон, музыка, контейнер панелей и показ панели main-menu по конфигу.
 */
public class ShowMainMenu {

    private static Stage primaryStage;

    /**
     * Ставит корень сцены, фон, музыку и контейнер для панелей, затем показывает панель "main-menu"
     * (привязка по id из Panels.xml и {@link PanelRegistry}).
     */
    public static void initializeMainMenuScene(AppSettings appSettings) {
        appSettings.getRoot().setOnMouseClicked(null);
        primaryStage = appSettings.getStage();

        leaveGameSession();
        SceneRenderer.clearSceneCache();
        ThemesConfigParser.applyMenuBackgroundToRoot(appSettings);

        if (!MenuResourceCache.isPreloaded()) {
            MenuResourceCache.preloadMenuResources();
        }
        try {
            String musicPath = MainMenuConfigParser.getConfig().musicPath;
            appSettings.setMediaPlayer(Soundtrack.startSound(appSettings, musicPath));
            appSettings.getMediaPlayer().play();
            SceneInfo.setMusic(musicPath);
        } catch (Exception e) {
        }

        VBox contentPane = new VBox();
        contentPane.setAlignment(Pos.BOTTOM_LEFT);
        appSettings.setMainMenuContentPane(contentPane);
        appSettings.getRoot().getChildren().setAll(contentPane);

        PanelRegistry.show("main-menu", appSettings);

        primaryStage.setScene(appSettings.getScene());
        primaryStage.setFullScreen(appSettings.isFullscreen());
    }

    /**
     * Снимает следы игровой сессии перед показом меню: игровые хоткеи,
     * звуковые эффекты и статическое состояние меню паузы.
     * Иначе в меню Esc открывал игровую паузу, Tab — журнал,
     * а длинный SFX продолжал играть поверх музыки меню.
     */
    private static void leaveGameSession() {
        DialogPanelController.removeHotkeys();
        GamePauseMenuController.resetState();
        SoundEffect.stopAll();
    }
}

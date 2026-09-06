package kz.arianwait.game.scenedetails;

import java.io.File;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.utils.SceneSettingsParser;
import kz.arianwait.game.utils.SceneSettingsParser.SceneSettings;
import kz.arianwait.game.utils.ResourceLocator;

/**
 * Фабрика для создания панели диалога из FXML.
 * Загружает dialog-panel.fxml из lib/fxml/, создаёт контроллер
 * и инициализирует его с привязкой к текущей сцене.
 */
public final class DialogPanelFactory {

    private static final String FXML_PATH = "lib/fxml/dialog-panel.fxml";

    private DialogPanelFactory() {
    }

    /**
     * Создаёт панель диалога из FXML и инициализирует контроллер.
     *
     * @param appSettings настройки приложения
     * @param scene       текущая сцена JavaFX (для привязки размеров)
     * @return инициализированный контроллер панели
     * @throws RuntimeException если FXML не найден или не загружен
     */
    public static DialogPanelController create(AppSettings appSettings, Scene scene) {
        try {
            File fxmlFile = ResourceLocator.file(FXML_PATH);
            URL fxmlUrl = fxmlFile.toURI().toURL();

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.load();

            DialogPanelController controller = loader.getController();
            SceneSettings settings = SceneSettingsParser.getSettings();
            controller.initialize(appSettings, scene, settings);

            return controller;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load dialog-panel.fxml: " + e.getMessage(), e);
        }
    }
}

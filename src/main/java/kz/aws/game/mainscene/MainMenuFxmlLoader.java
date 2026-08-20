package kz.aws.game.mainscene;

import java.io.File;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import kz.aws.game.utils.ResourceLocator;

/**
 * Загрузчик FXML для главного меню. Загружает lib/fxml/main-menu.fxml
 * из файловой системы (не classpath), т.к. проект использует lib/ как рабочую директорию.
 */
public final class MainMenuFxmlLoader {

    private static final String FXML_PATH = "lib/fxml/main-menu.fxml";

    private MainMenuFxmlLoader() {
    }

    /**
     * Загружает FXML в указанный контроллер-корень (fx:root pattern).
     *
     * @param controller контроллер, являющийся одновременно корневым узлом
     */
    public static void loadInto(MainMenuController controller) {
        try {
            File fxmlFile = ResourceLocator.file(FXML_PATH);
            URL fxmlUrl = fxmlFile.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setRoot(controller);
            loader.setController(controller);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load main-menu.fxml", e);
        }
    }
}

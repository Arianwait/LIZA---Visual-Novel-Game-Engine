package kz.aws.game.scenelist;

import java.io.Serializable;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.actionscenarios.SaveManadger;
import kz.aws.game.actionscenarios.ShowMainMenu;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.GameEngine;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenedetails.DialogPanelFactory;

/**
 * Построитель игровой сцены. Инициализирует панель диалога (FXML),
 * игровой движок, обработку кликов и навигатор сцен.
 */
public class SceneBuilder extends VBox implements Serializable {

    private static final long serialVersionUID = 6046255773257160060L;
    private GameEngine gameEngine;
    private DialogPanel dialogPanel;
    private EventHandler<MouseEvent> mouseClickHandler;

    /**
     * Создаёт игровую сцену. При sceneID == 0 возвращает в главное меню.
     *
     * @param appSettings  настройки приложения
     * @param saveFilePath путь к файлу сохранения (null — новая игра)
     * @param sceneID      идентификатор сцены
     */
    public SceneBuilder(AppSettings appSettings, String saveFilePath, int sceneID) {
        if (sceneID == 0) {
            ShowMainMenu.initializeMainMenuScene(appSettings);
            return;
        }

        SceneInfo.setAppSettings(appSettings);
        StackPane root = prepareRoot(appSettings);

        dialogPanel = createDialogPanel(appSettings, root);
        int resolvedSceneId = initializeEngine(appSettings, root, saveFilePath, sceneID);
        setupInputHandling(appSettings, root);
        registerSceneNavigator(appSettings);
    }

    /**
     * Очищает root и устанавливает чёрный фон.
     *
     * @param appSettings настройки
     * @return корневой StackPane
     */
    private StackPane prepareRoot(AppSettings appSettings) {
        StackPane root = appSettings.getRoot();
        root.getChildren().clear();
        root.setStyle("-fx-background-color: black;");
        return root;
    }

    /**
     * Создаёт панель диалога из FXML и добавляет на сцену.
     *
     * @param appSettings настройки
     * @param root        корневой StackPane
     * @return созданная панель
     */
    private DialogPanel createDialogPanel(AppSettings appSettings, StackPane root) {
        DialogPanel panel = DialogPanelFactory.create(appSettings, appSettings.getScene());
        panel.addToScene(root);
        SceneInfo.setTableDatail(panel);
        return panel;
    }

    /**
     * Инициализирует или переиспользует GameEngine.
     * Загружает сохранение, если указан путь.
     *
     * @param appSettings  настройки
     * @param root         корневой StackPane
     * @param saveFilePath путь к сохранению
     * @param sceneID      исходный id сцены
     * @return разрешённый id сцены (может измениться из сохранения)
     */
    private int initializeEngine(AppSettings appSettings, StackPane root,
                                  String saveFilePath, int sceneID) {
        GameData savedData = loadSaveData(saveFilePath);
        if (savedData != null) {
            sceneID = savedData.getCurrentSceneId();
        }

        if (SceneInfo.getGameEngine() == null) {
            gameEngine = createNewEngine(root, dialogPanel, savedData, sceneID);
        } else {
            gameEngine = reuseExistingEngine(root, dialogPanel, savedData, sceneID);
        }
        return sceneID;
    }

    /**
     * Загружает данные сохранения из файла.
     *
     * @param saveFilePath путь (null — нет сохранения)
     * @return данные или null
     */
    private GameData loadSaveData(String saveFilePath) {
        if (saveFilePath == null) return null;
        return SaveManadger.deserializeClicker(saveFilePath);
    }

    /**
     * Создаёт новый GameEngine и запускает игру.
     *
     * @param root      корневой StackPane
     * @param panel     панель диалога
     * @param savedData данные сохранения (null — новая игра)
     * @param sceneID   id начальной сцены
     * @return созданный движок
     */
    private GameEngine createNewEngine(StackPane root, DialogPanel panel,
                                        GameData savedData, int sceneID) {
        GameEngine engine = new GameEngine(root, panel);
        engine.initializeAndLoadAll();
        SceneInfo.setGameEngine(engine);

        if (savedData != null) {
            engine.restoreState(savedData);
        } else {
            engine.startNewGame(sceneID);
        }
        return engine;
    }

    /**
     * Переиспользует существующий GameEngine с новыми View-ссылками.
     *
     * @param root      корневой StackPane
     * @param panel     панель диалога
     * @param savedData данные сохранения (null — новая игра)
     * @param sceneID   id сцены
     * @return существующий движок
     */
    private GameEngine reuseExistingEngine(StackPane root, DialogPanel panel,
                                            GameData savedData, int sceneID) {
        GameEngine engine = SceneInfo.getGameEngine();
        engine.updateView(root, panel);

        if (savedData != null) {
            engine.restoreState(savedData);
        } else {
            engine.startNewGame(sceneID);
        }
        return engine;
    }

    /**
     * Настраивает обработку кликов мыши для продвижения по диалогу.
     *
     * @param appSettings настройки
     * @param root        корневой StackPane
     */
    private void setupInputHandling(AppSettings appSettings, StackPane root) {
        mouseClickHandler = event -> handleMouseClick(appSettings, event);

        SceneInfo.setEventHandler(mouseClickHandler);
        SceneInfo.setEventBack(event -> gameEngine.back());
        SceneInfo.enableEventHandler(root);
    }

    /**
     * Обрабатывает клик мыши: продвигает диалог или возвращает в меню.
     *
     * @param appSettings настройки
     * @param event       событие мыши
     */
    private void handleMouseClick(AppSettings appSettings, MouseEvent event) {
        if (!SceneInfo.isHandlerEnabled()) return;

        if (gameEngine.isFinished()) {
            returnToMainMenu(appSettings);
            return;
        }

        gameEngine.handleMouseClick(event);

        if (gameEngine.isFinished()) {
            returnToMainMenu(appSettings);
        }
    }

    /**
     * Очищает движок и возвращает в главное меню.
     *
     * @param appSettings настройки
     */
    private void returnToMainMenu(AppSettings appSettings) {
        gameEngine.cleanup();
        SceneInfo.setGameEngine(null);
        ShowMainMenu.initializeMainMenuScene(appSettings);
    }

    /**
     * Регистрирует навигатор для перехода между сценами из загадок.
     * Внутри игры переход делает {@code GameEngine.jumpTo} без пересоздания
     * сцены; SceneBuilder создаётся только если движка ещё нет.
     *
     * @param appSettings настройки
     */
    private void registerSceneNavigator(AppSettings appSettings) {
        appSettings.setSceneNavigator(targetSceneId -> {
            GameEngine engine = SceneInfo.getGameEngine();
            if (engine != null) {
                engine.jumpTo(targetSceneId);
            } else {
                new SceneBuilder(appSettings, null, targetSceneId);
            }
        });
    }
}

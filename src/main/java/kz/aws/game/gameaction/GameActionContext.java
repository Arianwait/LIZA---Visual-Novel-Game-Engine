package kz.aws.game.gameaction;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenedetails.DialogPanel;

/**
 * Контекст выполнения игровой команды: целевой объект, параметры, сцена, настройки.
 * Передаётся в {@link GameActionHandler#run(GameActionContext)}.
 */
public final class GameActionContext {

    private final String target;
    private final String value;
    private final String[] parameters;
    private final AppSettings appSettings;
    private final StackPane root;
    private final Stage primaryStage;
    private final DialogPanel tableDatail;

    /**
     * Создаёт контекст выполнения команды.
     *
     * @param target       имя цели (персонажа или объекта)
     * @param value        основное значение команды
     * @param parameters   все параметры команды
     * @param appSettings  настройки приложения
     * @param root         корневой StackPane сцены
     * @param primaryStage главное окно
     * @param tableDatail  панель диалога
     */
    public GameActionContext(String target, String value, String[] parameters,
            AppSettings appSettings, StackPane root,
            Stage primaryStage, DialogPanel tableDatail) {
        this.target = target != null ? target : "";
        this.value = value != null ? value : "";
        this.parameters = parameters != null ? parameters : new String[0];
        this.appSettings = appSettings;
        this.root = root;
        this.primaryStage = primaryStage;
        this.tableDatail = tableDatail;
    }

    /** Имя цели (персонажа или объекта). */
    public String getTarget() { return target; }
    /** Первый параметр или значение (часто поза, путь к фону и т.д.). */
    public String getValue() { return value; }
    /** Все параметры команды. */
    public String[] getParameters() { return parameters; }
    public AppSettings getAppSettings() { return appSettings; }
    public StackPane getRoot() { return root; }
    public Stage getPrimaryStage() { return primaryStage; }
    public DialogPanel getDialogPanel() { return tableDatail; }

}

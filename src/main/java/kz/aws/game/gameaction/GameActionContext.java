package kz.aws.game.gameaction;

import java.util.Collections;
import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.scenedetails.DialogPanel;

/**
 * Контекст выполнения игровой команды: целевой объект, параметры, сцена, персонажи, настройки.
 * Передаётся в {@link GameActionHandler#run(GameActionContext)}.
 */
public final class GameActionContext {

    private final String target;
    private final String value;
    private final String[] parameters;
    private final AppSettings appSettings;
    private final StackPane root;
    private final List<ICharacter> characterList;
    private final Stage primaryStage;
    private final DialogPanel tableDatail;

    public GameActionContext(String target, String value, String[] parameters,
            AppSettings appSettings, StackPane root, List<ICharacter> characterList,
            Stage primaryStage, DialogPanel tableDatail) {
        this.target = target != null ? target : "";
        this.value = value != null ? value : "";
        this.parameters = parameters != null ? parameters : new String[0];
        this.appSettings = appSettings;
        this.root = root;
        this.characterList = characterList != null ? characterList : Collections.emptyList();
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
    public List<ICharacter> getCharacterList() { return characterList; }
    public Stage getPrimaryStage() { return primaryStage; }
    public DialogPanel getDialogPanel() { return tableDatail; }

    /** Индекс персонажа в списке по имени цели, или -1 если не найден. */
    public int findCharacterIndex() {
        return kz.aws.game.fileutils.ListParser.findIdInList(target, characterList);
    }

    /** Персонаж по имени цели, или null. */
    public ICharacter findCharacter() {
        int idx = findCharacterIndex();
        if (idx >= 0 && idx < characterList.size()) return characterList.get(idx);
        return null;
    }
}

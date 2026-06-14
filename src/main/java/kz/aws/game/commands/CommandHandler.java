package kz.aws.game.commands;

import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.scenedetails.DialogPanel;

/**
 * Интерфейс для обработчиков команд
 */
public interface CommandHandler {
    
    /**
     * Выполняет команду
     * 
     * @param command команда для выполнения
     * @param appSettings настройки приложения
     * @param root корневой контейнер
     * @param characterList список персонажей
     * @param primaryStage основное окно
     * @param tableDatail детали таблицы
     */
    void execute(Command command, AppSettings appSettings, StackPane root, 
                List<ICharacter> characterList, Stage primaryStage, DialogPanel tableDatail);
}

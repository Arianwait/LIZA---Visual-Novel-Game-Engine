package kz.aws.game.commands;

import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenelist.SceneController;

/**
 * Команда для установки выбора игрока
 */
public class SetChoiceCommand implements CommandHandler {
    
    @Override
    public void execute(Command command, AppSettings appSettings, StackPane root, 
            List<ICharacter> ICharacterList, Stage primaryStage, DialogPanel tableDatail) {
        String[] args = command.getParameters();
        if (args.length >= 2) {
            String choiceId = args[0];
            String choiceValue = args[1];
            SceneController.setPlayerChoice(choiceId, choiceValue);
            System.out.println("Выбор установлен: " + choiceId + " = " + choiceValue);
        } else {
            System.err.println("Недостаточно аргументов для команды SetChoice");
        }
    }
}

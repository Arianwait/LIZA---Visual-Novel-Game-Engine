package kz.aws.game.commands;

import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenelist.SceneController;

/**
 * Команда для установки репутации персонажа
 */
public class SetReputationCommand implements CommandHandler {
    
    @Override
    public void execute(Command command, AppSettings appSettings, StackPane root, 
            List<ICharacter> ICharacterList, Stage primaryStage, DialogPanel tableDatail) {
        String[] args = command.getParameters();
        if (args.length >= 2) {
            String characterName = args[0];
            int reputation = Integer.parseInt(args[1]);
            SceneController.setCharacterReputation(characterName, reputation);
            System.out.println("Репутация установлена: " + characterName + " = " + reputation);
        } else {
            System.err.println("Недостаточно аргументов для команды SetReputation");
        }
    }
}

package kz.aws.game.commands;

import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenelist.SceneController;

/**
 * Команда для установки флага игры
 */
public class SetFlagCommand implements CommandHandler {
    
    @Override
    public void execute(Command command, AppSettings appSettings, StackPane root, 
            List<ICharacter> ICharacterList, Stage primaryStage, DialogPanel tableDatail) {
        String[] args = command.getParameters();
        if (args.length >= 2) {
            String flagName = args[0];
            boolean flagValue = Boolean.parseBoolean(args[1]);
            SceneController.setFlag(flagName, flagValue);
            System.out.println("Флаг установлен: " + flagName + " = " + flagValue);
        } else {
            System.err.println("Недостаточно аргументов для команды SetFlag");
        }
    }
}

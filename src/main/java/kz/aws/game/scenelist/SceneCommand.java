package kz.aws.game.scenelist;

import java.util.Collections;
import java.util.List;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.commands.CommandProcessor;
import kz.aws.game.gameaction.GameActionContext;
import kz.aws.game.gameaction.GameActionRegistry;
import kz.aws.game.scenedetails.DialogPanel;

public class SceneCommand {
	private static String Newemothion = "";
	public static void removeNewemothion() {
		Newemothion = "";
	}
    public static void setEmothion(String emothion, AppSettings appSettings, StackPane root, ICharacter eleinPerson, Stage primaryStage, DialogPanel tableDatail) {
    	if (!emothion.trim().equals(Newemothion.trim()) && emothion != null && !emothion.isEmpty()) {
    		Newemothion = emothion;
    		String[] emothions = emothion.split("_");
    		String pose = emothions.length > 1 ? emothions[1] : "";
    		GameActionContext ctx = new GameActionContext(
    				eleinPerson.getName(), pose, new String[] { pose },
    				appSettings, root, Collections.singletonList(eleinPerson), primaryStage, tableDatail);
    		GameActionRegistry.run("showPerson", ctx);
//        	switch(emothion) {
//        	case "Elein_Stay":
//        		elein.showCharacter(appSettings,  appSettings.getScene(), primaryStage, eleinPerson, "", "");
//        		break;
//        	case "Elein_Tired":
//        		elein.showCharacter(appSettings,  appSettings.getScene(), primaryStage, eleinPerson, "", "");
//        		break;
//        	case "Elein_Think":
//        		elein.showCharacter(appSettings,  appSettings.getScene(), primaryStage, eleinPerson, "", "");
//        		break;
//        	case "Elein_ThinkWithCloseEye":
//        		elein.showCharacter(appSettings,  appSettings.getScene(), primaryStage, eleinPerson, "", "'");
//        		break;
//        	default:
//        		break;
//        	}
    	}
    }
    
    public static void setActions(String commandString, AppSettings appSettings, StackPane root, List <ICharacter> ICharacterList, Stage primaryStage, DialogPanel tableDatail) {
        // Используем новую улучшенную систему команд
        CommandProcessor.executeCommand(commandString, appSettings, root, ICharacterList, primaryStage, tableDatail);
    }
}

package kz.aws.game.scenelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.GameEngine;
import kz.aws.game.scenedetails.DialogPanel;

public  class SceneInfo {
	private static int Cliker = 0;
	private static EventHandler<MouseEvent> mouseClickHandler;
	private static String background = "";
	private static String music = "";
	private static EventHandler<ActionEvent> EventBack;
	private static List<Map<String, String>> dialogList = new ArrayList<>();
	private static final int MAX_SIZE = 7; // Максимальное количество элементов
	private static List<String> choiceList = new ArrayList<>();; // Список выборов
	private static Node buttons = new HBox();
	private static DialogPanel tableDatail;
	private static boolean handlerEnabled = true;
	private static AppSettings appSettings;
	private static GameEngine gameEngine;
	private static kz.aws.game.panel.BaseGamePanel activePuzzle;

	/** Устанавливает активный паззл (null — паззл завершён). */
	public static void setActivePuzzle(kz.aws.game.panel.BaseGamePanel panel) { activePuzzle = panel; }
	/** Возвращает true если сейчас активен паззл/мини-игра. */
	public static boolean isPuzzleActive() { return activePuzzle != null; }
	/** Возвращает активный паззл. */
	public static kz.aws.game.panel.BaseGamePanel getActivePuzzle() { return activePuzzle; }

	public static void setAppSettings(AppSettings appSettings) { SceneInfo.appSettings = appSettings; }
	public static AppSettings getAppSettings() { return appSettings; }

    public static void setGameEngine(GameEngine engine) {
        gameEngine = engine;
    }

    public static GameEngine getGameEngine() {
        return gameEngine;
    }
	
    public static void addDialog(String name, String dialog) {
        // Удаляем первый элемент, если список достиг максимального размера
        if (dialogList.size() >= MAX_SIZE) {
            dialogList.remove(0); // Удаляем первый элемент (FIFO)
        }

        // Добавляем новый диалог
        Map<String, String> dialogEntry = new HashMap<>();
        dialogEntry.put("name", name);
        dialogEntry.put("dialog", dialog);
        dialogList.add(dialogEntry);
    }
    
    // Добавить выбор в список
    public static void addChoice(String choice) {
        if (!choiceList.contains(choice)) {
            choiceList.add(choice);
        }
    }

    public static void clearChoices() {
        choiceList.clear();
    }
    
    public static void loadChoiceList(List<String> saves) {
    	choiceList = saves;
    }
    
    // Получить весь список выборов
    public static List<String> getChoiceList() {
        return new ArrayList<>(choiceList); // Возвращаем копию списка
    }

    // Удалить выбор из списка
    public static void removeChoice(String choice) {
        choiceList.remove(choice);
    }

    // Проверить, содержит ли список указанный выбор
    public static boolean containsChoice(String choice) {
        return choiceList.contains(choice);
    }
    
    // Получение всех диалогов
    public static List<Map<String, String>> getAllDialogs() {
        return dialogList;
    }
    
    public static void clearDialogs() {
        dialogList.clear();
    }
    
	public static void setEventBack(EventHandler<ActionEvent> EventBack) {
		SceneInfo.EventBack = EventBack;
	}
	
	public static EventHandler<ActionEvent> getEventBack() {
		return EventBack;
	}	
	
	public static void setCliker(int Cliker) {
		SceneInfo.Cliker = Cliker;
	}
	
	public static int getCliker() {
		return Cliker;
	}	
	
	public static void setEventHandler(EventHandler<MouseEvent> mouseClickHandler) {
		SceneInfo.mouseClickHandler = mouseClickHandler;
	}
	
	public static EventHandler<MouseEvent> getEventHandler() {
		return mouseClickHandler;
	}	
	
	public static void disableEventHandler(StackPane root) {
		root.setOnMouseClicked(null);
		buttons.setDisable(true);
		handlerEnabled = false;
	}	
	
	public static void enableEventHandler(StackPane root) {
		root.setOnMouseClicked(mouseClickHandler);
		buttons.setDisable(false);
		handlerEnabled = true;
	}
	
	public static boolean isHandlerEnabled() {
		return handlerEnabled;
	}	
	
	public static void setHboxButton(Node root) {
		SceneInfo.buttons = root;
	}	
	
	public static void OnMenuButtons(HBox root) {
		buttons.setDisable(false);
	}	
	
	public static void setBackground(String background) {
		SceneInfo.background = background;
	}
	
	public static String getBackground() {
		return background;
	}	
	
	public static void setMusic(String music) {
		SceneInfo.music = music;
	}
	
	public static String getMusic() {
		return music;
	}	
	
    
    public static void setTableDatail(DialogPanel tableDatail) {
    	SceneInfo.tableDatail = tableDatail;
    }

    public static DialogPanel getTableDatail() {
    	return tableDatail;
    }
}

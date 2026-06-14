package kz.aws.game.scenedetails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import kz.aws.game.actionscenarios.SaveManadger;
import kz.aws.game.animation.AnimationUtils;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.GameEngine;
import kz.aws.game.fileutils.FileUtils;
import kz.aws.game.scenelist.GameData;
import kz.aws.game.scenelist.SceneInfo;

public class SaveTable {
	private final String SAVE_DIRECTORY = "save";
	private Text labelTextNode;
	private Button returnButton;
	private StackPane root;
	private int className;
	private VBox buttonsBox;
	private StackPane container;
	private VBox menu = new VBox(10);
	private AppSettings appSettings;

    public SaveTable(AppSettings appSettings, int className) {
    	this.className = className;
    	this.appSettings = appSettings;

    	buttonsBox = createButtons(appSettings);

    	menu.getStyleClass().add("hitech-panel");
    	menu.setPadding(new Insets(50));
    	menu.setAlignment(Pos.CENTER);

    	returnButton = new Button("Вернутся");
    	returnButton.setOnAction(event -> closeLoudTable());

    	labelTextNode = new Text("Сохранения игры");
    	labelTextNode.getStyleClass().add("hitech-panel-title");
    	labelTextNode.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        StackPane.setMargin(returnButton, new Insets(appSettings.getScene().getHeight()*0.3 , appSettings.getScene().getWidth() * 0.12,  0, 0));
        appSettings.ButtonStyle(returnButton, buttonsBox);
        
        menu.getChildren().addAll(labelTextNode, buttonsBox, returnButton);
    }
    
    private VBox createButtons(AppSettings appSettings) {
        buttonsBox = new VBox(3); // 10 - это расстояние между кнопками
        
        Button[] buttons = new Button[6];
        List<Integer> allNumbers = FileUtils.extractNumbersFromDirectory(SAVE_DIRECTORY);
        for (int i = 0; i < 6; i++) {
        	if(allNumbers.contains(i)) {
        		 // Разделяем строку по символу '_'
                String[] blocks = FileUtils.findFileByNumber(SAVE_DIRECTORY, i).split("_");
                String buttonLabel = blocks[blocks.length - 1].replace(".ser", "");
                buttons[i] = createButton(appSettings, buttonLabel, i, false);
        	} else {
        		buttons[i] = createButton(appSettings, "Сохранение" + (i + 1), i, true);
        	}
        	
        	appSettings.ButtonStyle(buttons[i], buttonsBox);
        	VBox.setMargin(buttons[i], new javafx.geometry.Insets(0, 0, 2, 0)); // Устанавливаем внешний отступ только слева
            buttonsBox.getChildren().add(buttons[i]);
        }
        
        return buttonsBox;
    }
    
    private Button createButton(AppSettings appSettings, String label, int buttonIndex, boolean NewButtonStatus) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        Button button = new Button(label);
        if(NewButtonStatus) {
        	button.setOnAction(event -> saveButtonClick(buttonIndex, button, buttonIndex + "_" + className + "_" + dateTime + ".ser"));
        } else {
        	button.setOnAction(event -> {accessChangeSave(appSettings, buttonIndex, button, buttonIndex  + "_" + className + "_" + dateTime + ".ser", label);});
        }
        return button;
    }

	private void accessChangeSave(AppSettings appSettings, int buttonIndex, Button button, String saveFileName, String label) { 	
    	ConfirmActionDialog confirmDialog = new ConfirmActionDialog(appSettings);

        confirmDialog.show(
            "Вы уверены, что хотите поменять сохранения?", // Сообщение
            "file:lib/MainMenuImg/table/MenuTable.png",   // Путь к изображению
            () -> { // Действие при нажатии "Да"
                deleteFile("save/" + FileUtils.findFileByNumber(SAVE_DIRECTORY, buttonIndex));
                saveButtonClick(buttonIndex, button, saveFileName);
            },
            () -> { // Действие при нажатии "Нет"
                System.out.println("Отмена смены сохранения");
            },
            buttonsBox
        );
    }

    
    private void saveButtonClick(int buttonIndex, Button button, String saveFileNameTemplate) {
    	GameEngine engine = SceneInfo.getGameEngine();
    	if (engine == null) {
    	    System.err.println("Cannot save: GameEngine is null.");
    	    return;
    	}
    	
    	GameData gameData = engine.getSaveData();
    	
    	// Use actual scene ID from engine, not the stale className
    	int actualSceneId = gameData.getCurrentSceneId();
    	
    	// Reconstruct filename with correct scene ID
    	// Template was: buttonIndex + "_" + className + "_" + dateTime + ".ser"
    	// New: buttonIndex + "_" + actualSceneId + "_" + dateTime + ".ser"
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String dateTime = dateFormat.format(new Date());
        String saveFileName = buttonIndex + "_" + actualSceneId + "_" + dateTime + ".ser";

    	SaveManadger.serializeClicker(gameData, SAVE_DIRECTORY + "/" + saveFileName);
        
        button.setText(dateTime);
        
        root.getChildren().removeAll(container);
        // Assuming container is 'menu' here, but variable name in existing code was container/menu mixed up? 
        // In existing code 'container' was declared but not init? 
        // 'menu' is added to 'root'. So remove 'menu'.
        root.getChildren().removeAll(menu);
        
        SceneInfo.enableEventHandler(root);
        System.out.println("Нажата кнопка " + buttonIndex + ". Сохранение...");
    }
    
    public void deleteFile(String filePath) {
        Path path = Paths.get(filePath);

        try {
            Files.delete(path);
            System.out.println("Файл успешно удален: " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при удалении файла: " + filePath);
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("static-access")
	public void showLoudTable(StackPane root) {
    	this.root = root;
    	root.getChildren().addAll(menu);
        AnimationUtils.slideInFromSide(
                menu,
                appSettings.getScene().getWidth(), // Начальная позиция (справа от экрана)
                0,                                // Конечная позиция (в центре)
                500                               // Длительность анимации (500 мс)
            );
    	root.setMargin(menu, new Insets(appSettings.getScene().getHeight() * 0.2, 0,  appSettings.getScene().getHeight() * 0.2, appSettings.getScene().getWidth() * 0.70));
        buttonsBox.setAlignment(Pos.CENTER);
        SceneInfo.disableEventHandler(root);
    }
    
    public void closeLoudTable() {
        AnimationUtils.slideOutToSide(
                menu,
                0,                                // Начальная позиция (в центре)
                appSettings.getScene().getWidth(), // Конечная позиция (справа от экрана)
                500,                              // Длительность анимации (500 мс)
                () -> {                           // Действие после завершения анимации
                    root.getChildren().removeAll(menu);
                    SceneInfo.enableEventHandler(root);
                }
            );
    	SceneInfo.enableEventHandler(root);
    }
}

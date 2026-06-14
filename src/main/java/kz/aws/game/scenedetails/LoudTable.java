package kz.aws.game.scenedetails;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import kz.aws.game.animation.AnimationUtils;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.fileutils.FileUtils;
import kz.aws.game.mainscene.LoadingScreen;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;

public class LoudTable extends StackPane {
	private static final String SAVE_DIRECTORY = "save";
	private Text labelTextNode;
	private Button returnButton;
	private StackPane root;
	private VBox buttonsBox;
	private VBox menu = new VBox(10);
	private AppSettings appSettings;
	private boolean checkFile = false;;
	public LoudTable(AppSettings appSettings, boolean checkFile) {
		this.checkFile = checkFile;

		buttonsBox = createButtons(appSettings);

		this.appSettings = appSettings;

		menu.getStyleClass().add("hitech-panel");
		menu.setPadding(new Insets(50));
		menu.setAlignment(Pos.CENTER);

		returnButton = new Button("Вернутся");
		returnButton.setOnAction(event -> closeLoudTable());
		appSettings.ButtonStyle(returnButton, menu);

		labelTextNode = new Text("Загрузка игры");
		labelTextNode.getStyleClass().add("hitech-panel-title");
		labelTextNode.styleProperty().bind(javafx.beans.binding.Bindings.format("-fx-font-size: %.0fpx;",
				appSettings.getStagePain().heightProperty().multiply(0.03)));
        
        VBox.setMargin(menu, new Insets(0, 0,  appSettings.getStagePain().getHeight() * 0.2, appSettings.getStagePain().getWidth() * 0.8));
        menu.getChildren().addAll(labelTextNode, buttonsBox, returnButton);
        //StackPane.setMargin(labelTextNode, new Insets(0 , appSettings.getScene().getWidth() * 0.06,  appSettings.getScene().getHeight()*0.35, 0));
        //StackPane.setMargin(buttonsBox, new Insets(0 , appSettings.getScene().getWidth() * 0.06,  0, 0));
	}
	
	 private VBox createButtons(AppSettings appSettings) {
	        buttonsBox = new VBox(3); // 10 - это расстояние между кнопками
	        buttonsBox.setAlignment(Pos.CENTER);
	        Button[] buttons = new Button[6];
	        List<Integer> allNumbers = FileUtils.extractNumbersFromDirectory(SAVE_DIRECTORY);
	        for (int i = 0; i < 6; i++) {
	        	if(allNumbers.contains(i)) {
	        		 // Разделяем строку по символу '_'
	        		String fileName = FileUtils.findFileByNumber(SAVE_DIRECTORY, i);
	                String[] blocks = FileUtils.findFileByNumber(SAVE_DIRECTORY, i).split("_");
	                String buttonLabel = blocks[blocks.length - 1].replace(".ser", "");
	                Button buttonsWork = buttons[i];
	                buttons[i] = new Button(buttonLabel);
	                buttons[i].setOnAction(event -> openNewSave(fileName, appSettings, buttonsWork, fileName, Integer.parseInt(blocks[1])));
	        	} else {
	        		buttons[i] = new Button("Пусто");
	        	}
	        	
	        	appSettings.ButtonStyle(buttons[i], buttonsBox);
	        	buttons[i].setAlignment(Pos.CENTER_LEFT);
	        	//VBox.setMargin(buttons[i], new javafx.geometry.Insets(0, 0, 2, 0)); // Устанавливаем внешний отступ только слева
	        	
	            buttonsBox.getChildren().add(buttons[i]);
	        }
	        
	        return buttonsBox;
	    }
	
	 private void openNewSave(String fileName, AppSettings appSettings, Button buttons, String saveFile, int fileTitle) {

	    	ConfirmActionDialog confirmDialog = new ConfirmActionDialog(appSettings);
	    	
	    	if(checkFile) {
		        confirmDialog.show(
			            "Вы уверены, загрузить сохранения, все не сохраненые данные будут удалены?", // Сообщение
			            "file:lib/MainMenuImg/table/MenuTable.png",   // Путь к изображению
			            () -> { // Действие при нажатии "Да"
			       		 appSettings.getRoot().getChildren().removeAll(menu);
			    		 appSettings.getRoot().getChildren().clear();
			            	loudSave(appSettings, saveFile, fileTitle);
			            },
			            () -> { // Действие при нажатии "Нет"
			                System.out.println("Отмена смены сохранения");
			            },
			            buttonsBox
			       );
	    	} else {
	   		 appSettings.getRoot().getChildren().removeAll(menu);
			 appSettings.getRoot().getChildren().clear();
	    		loudSave(appSettings, saveFile, fileTitle);
	    	}
	 }
	 
	 @SuppressWarnings("static-access")
	 public void showLoudTable(StackPane root) {
	     this.root = root;
	     root.getChildren().addAll(menu);

	     // Анимация появления меню
	     AnimationUtils.slideInFromSide(
	         menu,
	         appSettings.getScene().getWidth(), // Начальная позиция (справа от экрана)
	         0,                                // Конечная позиция (в центре)
	         500                               // Длительность анимации (500 мс)
	     );

	     root.setMargin(menu, new Insets(appSettings.getScene().getHeight() * 0.2, 0, appSettings.getScene().getHeight() * 0.2, appSettings.getScene().getWidth() * 0.70));
	     SceneInfo.disableEventHandler(root);
	 }
    
	 public void closeLoudTable() {
		    // Анимация скрытия меню
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
		}
    
    private void loudSave(AppSettings appSettings, String FilePath, int fileTitle) {
        // Show loading screen before loading save
        LoadingScreen loadingScreen = new LoadingScreen(appSettings.getStagePain().getWidth(), appSettings.getStagePain().getHeight());
        appSettings.getStagePain().getScene().setRoot(loadingScreen.getRoot());
        appSettings.applyThemeToRoot();
        loadingScreen.startLoading(1, () -> { // 1 is dummy, logic inside loader loads all for now
             new SceneBuilder(appSettings, FilePath, fileTitle);
             appSettings.getStagePain().getScene().setRoot(appSettings.getRoot());
        });
    }
}

package kz.aws.game.scenedetails;

import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;

public class DialogButtonPane extends StackPane {
	private VBox vbox = new VBox(10);;
	private String imagePath = "file:lib/MainMenuImg/table/MenuTable.png";
	ImageView imageView;
	public DialogButtonPane(AppSettings appSettings) {
		getChildren().addAll(vbox);
	}
	
    @SuppressWarnings("static-access")
	public void createButtons(List<String> options, List<Integer> requestID, List<Boolean> trustButton, AppSettings appSettings) {
    	vbox.getChildren().clear();
    	
    	vbox.setSpacing(10);
		
        for (int c1 = 0; c1 < options.size(); c1++) {
            final int currentIndex = c1; // Создание эффективно финальной переменной
            Button optionButton = new Button(options.get(c1));
            appSettings.ButtonStyle(optionButton, vbox);
            // Установка обработчика событий для каждой кнопки
            optionButton.setOnAction(event -> { 
            	SceneInfo.addChoice(options.get(currentIndex));
                SceneBuilder sceneBuilder = new SceneBuilder(appSettings, null, requestID.get(currentIndex));
                vbox.getChildren().addAll(sceneBuilder);
            });
            
            optionButton.setVisible(trustButton.get(currentIndex));
            vbox.setMargin(optionButton, new Insets(0, appSettings.getScene().getWidth() * 0.05,  0, appSettings.getScene().getWidth() * 0.05));

            if(c1 == options.size() - 1) {
                vbox.setMargin(optionButton, new Insets(0, appSettings.getScene().getWidth() * 0.05,  appSettings.getScene().getHeight() * 0.05, appSettings.getScene().getWidth() * 0.05));
            } else if(c1 == 0) {
                vbox.setMargin(optionButton, new Insets(appSettings.getScene().getHeight() * 0.05, appSettings.getScene().getWidth() * 0.05,  0, appSettings.getScene().getWidth() * 0.05));
            }
            
            vbox.getChildren().add(optionButton);
        }
        
        Image backgroundImage = new Image(imagePath);
        BackgroundSize backgroundSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
        BackgroundImage backgroundImg = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        Background background = new Background(backgroundImg);
        vbox.setBackground(background);
        
        setMargin(vbox, new Insets(appSettings.getScene().getHeight() * (0.06 * (10 - (0.06 * options.size()))), appSettings.getScene().getWidth() * 0.3,  appSettings.getScene().getHeight() * 0.3, appSettings.getScene().getWidth() * 0.3));
    }
    
    public void showDialogButtonPane(StackPane root) {
    	root.getChildren().add(this);
    	
    	SceneInfo.disableEventHandler(root);
    }
    
    public void closeDialogButtonPane(StackPane root) {
    	SceneInfo.enableEventHandler(root);
    	root.getChildren().remove(vbox);
    }
}

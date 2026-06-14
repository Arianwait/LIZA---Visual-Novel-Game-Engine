package kz.aws.game.scenedetails;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import kz.aws.game.appsettings.AppSettings;

public  class MessageTable extends StackPane {
	private ImageView imageView;
    private Text dialogueText;
    private  Button returnButton;
    StackPane root;
	public MessageTable(AppSettings appSettings, Scene scene) {
        imageView = new ImageView(new Image("file:lib/Scene/Окошко.png"));
        dialogueText = new Text("В данный момент это муляж");
        dialogueText.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: brown;");
        imageView.setFitWidth(scene.getHeight()*0.5);
        imageView.setFitHeight(scene.getWidth()*0.15);
        
        returnButton = new Button("Ok");
        returnButton.setStyle("-fx-background-color: transparent; -fx-text-fill: brown; -fx-font-size: 18; -fx-font-weight: bold;");
        StackPane.setAlignment(returnButton, Pos.CENTER);
        returnButton.setOnMouseEntered(e -> returnButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #D2B48C; -fx-font-size: 18; -fx-font-weight: bold;"));
        returnButton.setOnMouseExited(e -> returnButton.setStyle("-fx-background-color: transparent; -fx-text-fill: brown; -fx-font-size: 18; -fx-font-weight: bold;"));
        StackPane.setMargin(returnButton, new Insets(scene.getHeight()*0.07 , 0,  0, 0));
        
        returnButton.setOnAction(event -> closeErrorMessage()); // Пример: отмена обработчика кликов
	}
	
	public void showErrorMessage(StackPane root) {
		this.root = root;
    	root.getChildren().addAll(imageView, dialogueText);
    	root.getChildren().addAll(returnButton);
	}
	
	public void closeErrorMessage() {
    	root.getChildren().removeAll(imageView, dialogueText);
    	root.getChildren().removeAll(returnButton);
	}
}

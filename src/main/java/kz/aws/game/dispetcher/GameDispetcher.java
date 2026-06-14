package kz.aws.game.dispetcher;

import java.io.Serializable;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.appsettings.JsonParser;
import kz.aws.game.mainscene.LogoAnimation;

public class GameDispetcher extends Application implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8222725889624267118L;
	private Stage primaryStage; // Сохраняем основной Stage

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

			Image ico = new Image("file:lib/Logo/logo.png");
			primaryStage.getIcons().add(ico);

			primaryStage.setResizable(false);

			// Чтение настроек из JSON
			AppSettings appSettings = JsonParser.readConfig();
			changeResolution(appSettings);
			primaryStage.setTitle("Innagano");

			appSettings.setGamedispetcher(this);
			appSettings.setStagePain(primaryStage);

			// Создаем корневой узел для сцены

			StackPane root = new StackPane();
			appSettings.setRoot(root);
			primaryStage.setFullScreen(appSettings.isFullscreen());

			// Создаем основную сцену
			Scene scene = new Scene(root, appSettings.getWindowWidth(), appSettings.getWindowHeight());
			appSettings.setScene(scene);

			scene.getStylesheets().add("file:lib/config/style.css");
            // scene.getStylesheets().add("file:lib/config/dialog_styles.css"); // Uncomment if needed

			LogoAnimation logoAnimation = new LogoAnimation(appSettings);
			root.getChildren().add(logoAnimation);

			// Устанавливаем сцену на основном stage
			primaryStage.setScene(scene);

			root.setAlignment(Pos.CENTER);

			// Убираем сообщение "Press Esc to exit fullscreen"
			primaryStage.setFullScreenExitHint("");

			// Заблокируем выход из полноэкранного режима на Esc
			primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

			// Отображаем основное окно
			primaryStage.show();
	}

	public void changeResolution(AppSettings appSettings) {
		// Здесь вы можете добавить код для изменения разрешения приложения
		// Например, изменить размер сцены или выполнить другие действия, связанные с
		// изменением разрешения
		primaryStage.setFullScreen(appSettings.isFullscreen());
		primaryStage.setWidth(appSettings.getWindowWidth());
		primaryStage.setHeight(appSettings.getWindowHeight());

	}

	public void showScene(Scene scene, AppSettings appSettings) {
		primaryStage.setScene(scene);
		primaryStage.setFullScreen(appSettings.isFullscreen());
	}
}

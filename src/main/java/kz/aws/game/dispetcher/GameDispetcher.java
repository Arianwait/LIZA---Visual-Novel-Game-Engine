package kz.aws.game.dispetcher;

import java.io.Serializable;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.appsettings.JsonParser;
import kz.aws.game.mainscene.LogoAnimation;
import kz.aws.game.utils.VirtualViewport;

/**
 * Точка входа приложения. Создаёт Stage и Scene, разворачивает
 * {@link VirtualViewport}: весь интерфейс живёт в контейнере фиксированного
 * дизайн-разрешения и масштабируется под реальный размер окна одним трансформом.
 */
public class GameDispetcher extends Application implements Serializable {

	private static final long serialVersionUID = 8222725889624267118L;
	private Stage primaryStage;

	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Инициализирует окно, вьюпорт и стартовую анимацию логотипа.
	 *
	 * @param primaryStage основной Stage приложения
	 */
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;

		primaryStage.getIcons().add(new Image("file:lib/Logo/logo.png"));

		AppSettings appSettings = JsonParser.readConfig();
		changeResolution(appSettings);
		primaryStage.setTitle("Innagano");

		appSettings.setGamedispetcher(this);
		appSettings.setStagePain(primaryStage);

		VirtualViewport viewport = new VirtualViewport();
		appSettings.setRoot(viewport.getContentRoot());
		primaryStage.setFullScreen(appSettings.isFullscreen());

		Scene scene = new Scene(viewport.getScreenRoot(),
				appSettings.getWindowWidth(), appSettings.getWindowHeight());
		appSettings.setScene(scene);
		viewport.bindTo(scene);

		scene.getStylesheets().add("file:lib/config/style.css");

		appSettings.getRoot().getChildren().add(new LogoAnimation(appSettings));

		primaryStage.setScene(scene);
		primaryStage.setFullScreenExitHint("");
		primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
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

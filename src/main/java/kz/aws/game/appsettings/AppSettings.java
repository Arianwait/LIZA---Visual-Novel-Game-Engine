package kz.aws.game.appsettings;

import java.util.function.Consumer;

import org.json.simple.JSONObject;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import kz.aws.game.dispetcher.GameDispetcher;

@SuppressWarnings("deprecation")
public interface AppSettings {

    int getWindowWidth();

    void setWindowWidth(int windowWidth);

    int getWindowHeight();

    void setWindowHeight(int windowHeight);

    boolean isFullscreen();

    void setFullscreen(boolean fullscreen);

    void setStagePain(Stage primaryStage);
    
    void setScene(Scene scene);
    
    void setRoot(StackPane root);
    
    void setGamedispetcher(GameDispetcher gameDispetcher);
    
    Stage getStagePain();
    
    GameDispetcher getGamedispetcher();
    
    Scene getScene();
    
    StackPane getRoot();
    
    void updateSettings(JSONObject jsonObject);
    
    void setMediaPlayer(MediaPlayer mediaPlayer);
    
    MediaPlayer getMediaPlayer();
    
    Button ButtonStyle(Button button, VBox root);
    
    Button ButtonStyle(Button button, HBox root);
    
    Button ButtonStyle(Button button, BorderPane root);
    
    Button ButtonStyle(Button button, Scene root);

	void updateSettings();

	void setVolumeValue(Double volumValue);

	Double getVolumeValue();

	/** Идентификатор темы интерфейса (hitech, classic, walk и т.д.). Меню, загрузка, диалоги подстраиваются под тему. */
	String getUiTheme();

	void setUiTheme(String themeId);

	/** Применяет текущую тему к корню сцены (добавляет класс theme-{id}). Вызывается при смене темы и при установке root. */
	void applyThemeToRoot();

	/** Контейнер главного меню (VBox), в котором меняются экраны: меню, настройки, выбор главы. Устанавливается при построении MainMenu. */
	VBox getMainMenuContentPane();
	void setMainMenuContentPane(VBox pane);

	/**
	 * Коллбэк для перехода на сцену по её номеру.
	 * Устанавливается в {@code SceneBuilder} и используется загадками
	 * ({@code openPuzzle}) для перехода на {@code sceneOnSuccess/sceneOnFailure}.
	 */
	Consumer<Integer> getSceneNavigator();
	void setSceneNavigator(Consumer<Integer> navigator);
}
package kz.aws.game.appsettings;

import java.io.IOException;
import java.util.function.Consumer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.dispetcher.GameDispetcher;
import kz.aws.game.utils.VirtualViewport;

@SuppressWarnings("deprecation")
public class DefaultAppSettings implements AppSettings {
	
    private static String filePath = "lib/config/SettingsConfig.json";
    /** Идентификатор темы: hitech, classic, walk. По умолчанию — хайтек. */
    public static final String DEFAULT_UI_THEME = "hitech";
    /** Размер окна по умолчанию, если конфиг отсутствует или повреждён. */
    private static final int DEFAULT_WINDOW_WIDTH = 1280;
    private static final int DEFAULT_WINDOW_HEIGHT = 720;
    private int windowWidth = DEFAULT_WINDOW_WIDTH;
    private int windowHeight = DEFAULT_WINDOW_HEIGHT;
    private Double volumValue = 0.1;
    private boolean fullscreen;
    private String uiTheme = DEFAULT_UI_THEME;
    private Stage primaryStage;
    private GameDispetcher gameDispetcher;
    private Scene scene;
    private StackPane root;
    private MediaPlayer mediaPlayer;
    private VBox mainMenuContentPane;
    private Consumer<Integer> sceneNavigator;
    
    
    // Реализация методов интерфейса
    @Override
    public int getWindowWidth() {
        return windowWidth;
    }

    @Override
    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth > 0 ? windowWidth : DEFAULT_WINDOW_WIDTH;
    }

    @Override
    public Double getVolumeValue() {
        return volumValue;
    }

    @Override
    public void setVolumeValue(Double volumValue) {
        this.volumValue = volumValue;
    }
    
    @Override
    public int getWindowHeight() {
        return windowHeight;
    }

    @Override
    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight > 0 ? windowHeight : DEFAULT_WINDOW_HEIGHT;
    }

    @Override
    public void updateSettings(JSONObject jsonObject) {
        setWindowWidth(Integer.parseInt(jsonObject.get("windowWidth").toString()));
        setWindowHeight(Integer.parseInt(jsonObject.get("windowHeight").toString()));
        setVolumeValue(Double.parseDouble(jsonObject.get("volumeValue").toString()));
        setFullscreen(Boolean.parseBoolean(jsonObject.get("fullscreen").toString()));
        if (jsonObject.get("uiTheme") != null) {
            setUiTheme(jsonObject.get("uiTheme").toString().trim());
        }
    }
    
    @Override
    public void updateSettings() {
        JSONParser parser = new JSONParser();
        
        try (java.io.Reader reader = java.nio.file.Files.newBufferedReader(
                java.nio.file.Path.of(filePath), java.nio.charset.StandardCharsets.UTF_8)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            this.updateSettings(jsonObject);
        } catch (IOException | ParseException | RuntimeException e) {
            System.err.println("Настройки не перечитаны (" + filePath + "): " + e.getMessage());
        }
    }
    
    @Override
    public boolean isFullscreen() {
        return fullscreen;
    }

    @Override
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

	@Override
	public void setStagePain(Stage primaryStage) {
		this.primaryStage = primaryStage;
		
	}

	@Override
	public void setGamedispetcher(GameDispetcher gameDispetcher) {
		this.gameDispetcher = gameDispetcher;
		
	}

	@Override
	public Stage getStagePain() {
		return primaryStage;
	}

	@Override
	public GameDispetcher getGamedispetcher() {
		return gameDispetcher;
	}

	@Override
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	@Override
	public void setRoot(StackPane root) {
		this.root = root;
		applyThemeToRoot();
	}

	@Override
	public String getUiTheme() {
		return uiTheme != null && !uiTheme.isEmpty() ? uiTheme : DEFAULT_UI_THEME;
	}

	@Override
	public void setUiTheme(String themeId) {
		this.uiTheme = themeId != null && !themeId.isEmpty() ? themeId.trim() : DEFAULT_UI_THEME;
		applyThemeToRoot();
	}

	@Override
	public void applyThemeToRoot() {
		Node target = root;
		if (target == null || target.getStyleClass() == null) return;
		String themeClass = "theme-" + getUiTheme();
		target.getStyleClass().removeIf(s -> s.startsWith("theme-"));
		target.getStyleClass().add(themeClass);
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	@Override
	public StackPane getRoot() {
		return root;
	}
	
	public void setMediaPlayer(MediaPlayer mediaPlayer){
		this.mediaPlayer = mediaPlayer;
	}
	
	public MediaPlayer getMediaPlayer(){
		return this.mediaPlayer;
	}
	
	@Override
	public Button ButtonStyle(Button button, VBox root) {
		button.getStyleClass().add("game-button");
		button.setMaxWidth(Double.MAX_VALUE);
		button.setPrefHeight(VirtualViewport.height(0.05));
		ButtonAnimation.addButtonHoverAnimation(button);
		return button;
	}

	@Override
	public VBox getMainMenuContentPane() {
		return mainMenuContentPane;
	}

	@Override
	public void setMainMenuContentPane(VBox pane) {
		this.mainMenuContentPane = pane;
	}

	@Override
	public Consumer<Integer> getSceneNavigator() {
		return sceneNavigator;
	}

	@Override
	public void setSceneNavigator(Consumer<Integer> navigator) {
		this.sceneNavigator = navigator;
	}
}

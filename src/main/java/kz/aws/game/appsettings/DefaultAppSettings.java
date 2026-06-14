package kz.aws.game.appsettings;

import java.io.FileReader;
import java.io.IOException;
import java.util.function.Consumer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.dispetcher.GameDispetcher;

@SuppressWarnings("deprecation")
public class DefaultAppSettings implements AppSettings {
	
    private static String filePath = "lib/config/SettingsConfig.json";
    /** Идентификатор темы: hitech, classic, walk. По умолчанию — хайтек. */
    public static final String DEFAULT_UI_THEME = "hitech";
    private int windowWidth;
    private int windowHeight;
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
        this.windowWidth = windowWidth;
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
        this.windowHeight = windowHeight;
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
        
        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            this.updateSettings(jsonObject);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            // Обработка ошибок при чтении JSON
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
		Node target = (primaryStage != null && primaryStage.getScene() != null)
				? primaryStage.getScene().getRoot() : root;
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
		// TODO Auto-generated method stub
		return root;
	}
	
	public void setMediaPlayer(MediaPlayer mediaPlayer){
		this.mediaPlayer = mediaPlayer;
	}
	
	public MediaPlayer getMediaPlayer(){
		return this.mediaPlayer;
	}
	
	public Button ButtonStyle(Button button, VBox root) {
		button.getStyleClass().add("game-button");
		// button.setStyle("-fx-font-size: "+ getRoot().getHeight() * 0.015 + "px;"); // REMOVED: Managed by UiFactory
		//button.prefWidthProperty().bind(root.widthProperty().multiply(1));
		//button.prefHeightProperty().bind(root.heightProperty().multiply(0.1));
		//VBox.setMargin(button, new Insets(getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005,  getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005));
		// Привязка размеров кнопки к размерам корневого элемента
        button.prefWidthProperty().bind(root.widthProperty().multiply(1));  // 5% от ширины root
        button.prefHeightProperty().bind(root.heightProperty().multiply(0.1)); // 5% от высоты root
        // button.setFont(Font.font(getRoot().getHeight() * 0.005)); // REMOVED: Managed by UiFactory
		ButtonAnimation.addButtonHoverAnimation(button);
		return button;
	}
	
	public Button ButtonStyle(Button button, HBox root) {		
		button.getStyleClass().add("game-button");
		//HBox.setMargin(button, new Insets(getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005,  getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005));
        button.prefWidthProperty().bind(root.widthProperty().multiply(0.1));  // 5% от ширины root
        button.prefHeightProperty().bind(root.heightProperty().multiply(0.5)); // 5% от высоты ro
		ButtonAnimation.addButtonHoverAnimation(button);
		return button;
	}
	
	public Button ButtonStyle(Button button, BorderPane root) {
		button.getStyleClass().add("game-button");
		//button.prefWidthProperty().bind(root.widthProperty().multiply(0.1));
		//button.prefHeightProperty().bind(root.heightProperty().multiply(0.1));
		BorderPane.setMargin(button, new Insets(getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005,  getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005));
		ButtonAnimation.addButtonHoverAnimation(button);
		return button;
	}
	
	public Button ButtonStyle(Button button, Scene root) {
		button.getStyleClass().add("game-button");
		// button.setStyle("-fx-font-size: "+ getRoot().getHeight() * 0.015 + "px;"); // REMOVED: Managed by UiFactory
		//button.prefWidthProperty().bind(root.widthProperty().multiply(1));
		//button.prefHeightProperty().bind(root.heightProperty().multiply(0.1));
		//VBox.setMargin(button, new Insets(getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005,  getRoot().getHeight() * 0.005, getRoot().getWidth() * 0.005));
		// Привязка размеров кнопки к размерам корневого элемента
        button.prefWidthProperty().bind(root.widthProperty().multiply(1));  // 5% от ширины root
        button.prefHeightProperty().bind(root.heightProperty().multiply(0.01)); // 5% от высоты root
        // button.setFont(Font.font(getRoot().getHeight() * 0.005)); // REMOVED: Managed by UiFactory
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

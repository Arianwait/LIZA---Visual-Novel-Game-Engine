package kz.aws.game.scenedetails;

import java.io.File;
import java.net.URL;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.buttonaction.ButtonActionRegistry;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Overlay-меню паузы во время игры. Появляется поверх сцены
 * с затемнением фона. Содержит: Продолжить, Сохранить, Загрузить,
 * История, Главное меню.
 */
public class GamePauseMenuController extends StackPane {

    private static final String FXML_PATH = "lib/fxml/pause-menu.fxml";
    private static boolean opened = false;
    private static GamePauseMenuController currentInstance;

    @FXML private StackPane dimBackground;
    @FXML private VBox menuPanel;
    @FXML private Text titleText;
    @FXML private VBox buttonsBox;

    private final AppSettings appSettings;
    private StackPane root;

    /**
     * Проверяет, открыто ли меню паузы.
     *
     * @return true если меню сейчас отображается
     */
    public static boolean isOpen() {
        return opened;
    }

    /**
     * Открывает меню паузы, если оно ещё не открыто.
     *
     * @param appSettings настройки приложения
     */
    public static void open(AppSettings appSettings) {
        if (opened) return;
        new GamePauseMenuController(appSettings).show(appSettings.getRoot());
    }

    /**
     * Закрывает текущее открытое меню паузы.
     */
    public static void closeCurrent() {
        if (currentInstance != null) {
            currentInstance.close();
        }
    }

    /**
     * Создаёт контроллер меню паузы.
     *
     * @param appSettings настройки приложения
     */
    public GamePauseMenuController(AppSettings appSettings) {
        this.appSettings = appSettings;
        loadFxml();
    }

    /**
     * Загружает FXML из файловой системы (fx:root pattern).
     */
    private void loadFxml() {
        try {
            File fxmlFile = new File(FXML_PATH);
            URL fxmlUrl = fxmlFile.toURI().toURL();
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load pause-menu.fxml", e);
        }
    }

    /**
     * Показывает меню паузы поверх игры.
     *
     * @param root корневой StackPane сцены
     */
    public void show(StackPane root) {
        this.root = root;
        opened = true;
        currentInstance = this;
        setupTitle();
        createButtons();
        bindLayout();
        root.getChildren().add(this);
        SceneInfo.disableEventHandler(root);
    }

    /**
     * Закрывает меню паузы.
     */
    private void close() {
        opened = false;
        currentInstance = null;
        if (root != null) {
            root.getChildren().remove(this);
            SceneInfo.enableEventHandler(root);
        }
    }

    /**
     * Настраивает заголовок с адаптивным шрифтом.
     */
    private void setupTitle() {
        titleText.setFill(Color.web("#e8d5c0"));
        titleText.styleProperty().bind(Bindings.format(
                "-fx-font-size: %.0fpx; -fx-font-weight: bold;",
                appSettings.getStagePain().heightProperty().multiply(0.035)));
    }

    /**
     * Создаёт кнопки меню.
     */
    private void createButtons() {
        buttonsBox.getChildren().clear();
        addButton("Продолжить", e -> close());
        addButton("Сохранить игру", e -> ButtonActionRegistry.run("game-btn-save", appSettings));
        addButton("Загрузить игру", e -> ButtonActionRegistry.run("game-btn-load", appSettings));
        addButton("История", e -> ButtonActionRegistry.run("game-btn-history", appSettings));
        addButton("Главное меню", e -> {
            close();
            ButtonActionRegistry.run("game-btn-menu", appSettings);
        });
    }

    /**
     * Добавляет кнопку в панель.
     *
     * @param text    текст кнопки
     * @param handler обработчик нажатия
     */
    private void addButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("game-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(handler);
        ButtonAnimation.addButtonHoverAnimation(btn);
        buttonsBox.getChildren().add(btn);
    }

    /**
     * Привязывает размеры панели к сцене.
     */
    private void bindLayout() {
        Scene scene = appSettings.getScene();

        menuPanel.maxWidthProperty().bind(scene.widthProperty().multiply(0.3));
        menuPanel.maxHeightProperty().bind(scene.heightProperty().multiply(0.6));
        StackPane.setAlignment(menuPanel, Pos.CENTER);

        menuPanel.spacingProperty().bind(scene.heightProperty().multiply(0.018));
        menuPanel.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new javafx.geometry.Insets(
                        scene.getHeight() * 0.03,
                        scene.getWidth() * 0.025,
                        scene.getHeight() * 0.03,
                        scene.getWidth() * 0.025),
                scene.widthProperty(), scene.heightProperty()));
        buttonsBox.spacingProperty().bind(scene.heightProperty().multiply(0.01));

        for (javafx.scene.Node node : buttonsBox.getChildren()) {
            if (!(node instanceof Button btn)) continue;
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.prefHeightProperty().bind(scene.heightProperty().multiply(0.055));
            btn.styleProperty().bind(Bindings.concat(
                    "-fx-font-size: ",
                    scene.heightProperty().multiply(0.018).asString(),
                    "px;"));
        }

        dimBackground.setOnMouseClicked(e -> {
            if (e.getTarget() == dimBackground) close();
        });
    }
}

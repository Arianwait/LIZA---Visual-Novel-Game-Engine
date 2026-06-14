package kz.aws.game.scenedetails;

import java.io.File;
import java.net.URL;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.SceneSettingsParser;
import kz.aws.game.utils.SceneSettingsParser.SceneSettings;

/**
 * FXML-контроллер панели выбора в диалоге. Отображает варианты ответов
 * компактной панелью, позиция настраивается через SceneSettings.xml
 * (x-position, y-position — доли экрана).
 */
public class DialogChoicesController extends StackPane {

    private static final String FXML_PATH = "lib/fxml/dialog-choices.fxml";

    @FXML private VBox buttonsContainer;

    private final AppSettings appSettings;
    private final SceneSettings settings;

    /**
     * Создаёт панель выбора в диалоге.
     *
     * @param appSettings настройки приложения
     */
    public DialogChoicesController(AppSettings appSettings) {
        this.appSettings = appSettings;
        this.settings = SceneSettingsParser.getSettings();
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
            throw new RuntimeException("Failed to load dialog-choices.fxml", e);
        }
    }

    /**
     * Создаёт кнопки выбора и располагает панель на экране.
     *
     * @param options     тексты вариантов
     * @param requestID   id сцен для перехода
     * @param trustButton видимость кнопок (false — заблокирована условием)
     * @param appSettings настройки приложения
     */
    public void createButtons(List<String> options, List<Integer> requestID,
                               List<Boolean> trustButton, AppSettings appSettings) {
        buttonsContainer.getChildren().clear();
        buttonsContainer.setSpacing(settings.choicesSpacing);
        buttonsContainer.setAlignment(Pos.CENTER);

        for (int i = 0; i < options.size(); i++) {
            if (!trustButton.get(i)) continue;
            Button btn = createChoiceButton(options.get(i), requestID.get(i));
            buttonsContainer.getChildren().add(btn);
        }

        bindLayout();
    }

    /**
     * Создаёт одну кнопку выбора с действием перехода на сцену.
     *
     * @param text    текст варианта
     * @param sceneId id сцены для перехода
     * @return настроенная кнопка
     */
    private Button createChoiceButton(String text, int sceneId) {
        Button button = new Button(text);
        button.getStyleClass().add("choice-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setWrapText(true);

        Scene scene = appSettings.getScene();
        button.prefHeightProperty().bind(
                scene.heightProperty().multiply(settings.choicesButtonHeightMultiplier));
        button.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ",
                scene.heightProperty().multiply(settings.choicesFontSizeMultiplier).asString(),
                "px;"));

        ButtonAnimation.addButtonHoverAnimation(button);

        button.setOnAction(event -> {
            SceneInfo.addChoice(text);
            new SceneBuilder(appSettings, null, sceneId);
        });
        return button;
    }

    /**
     * Привязывает позицию buttonsContainer через translateX/translateY.
     * x-position и y-position из конфига задают точку (доля экрана 0.0–1.0),
     * вокруг которой центрируется панель.
     */
    private void bindLayout() {
        Scene scene = appSettings.getScene();

        buttonsContainer.maxWidthProperty().bind(
                scene.widthProperty().multiply(settings.choicesWidthMultiplier));

        Runnable update = () -> {
            double w = scene.getWidth();
            double h = scene.getHeight();

            double offsetX = (settings.choicesXPosition - 0.5) * w;
            double offsetY = (settings.choicesYPosition - 0.5) * h;

            buttonsContainer.setTranslateX(offsetX);
            buttonsContainer.setTranslateY(offsetY);
        };

        scene.widthProperty().addListener((obs, o, n) -> update.run());
        scene.heightProperty().addListener((obs, o, n) -> update.run());
        buttonsContainer.needsLayoutProperty().addListener((obs, o, n) -> update.run());
        update.run();
    }

    /**
     * Показывает панель выбора на указанном root и отключает обработчик мыши.
     *
     * @param root корневой StackPane сцены
     */
    public void showDialogButtonPane(StackPane root) {
        root.getChildren().add(this);
        SceneInfo.disableEventHandler(root);
    }

    /**
     * Закрывает панель выбора и включает обработчик мыши.
     *
     * @param root корневой StackPane сцены
     */
    public void closeDialogButtonPane(StackPane root) {
        SceneInfo.enableEventHandler(root);
        root.getChildren().remove(this);
    }
}

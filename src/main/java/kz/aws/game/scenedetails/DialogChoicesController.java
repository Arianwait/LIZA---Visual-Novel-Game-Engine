package kz.aws.game.scenedetails;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.scenelist.SceneBuilder;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.SceneSettingsParser;
import kz.aws.game.utils.SceneSettingsParser.SceneSettings;
import kz.aws.game.utils.VirtualViewport;

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
     * Создаёт кнопки из списка вариантов выбора. Панель сама не переходит
     * по сценам: при клике закрывается и передаёт вариант в onChoose.
     *
     * @param options  доступные варианты выбора (уже отфильтрованные)
     * @param onChoose обработчик выбранного варианта (обычно GameEngine::choose)
     */
    public void createButtons(List<ChoiceOption> options, Consumer<ChoiceOption> onChoose) {
        buttonsContainer.getChildren().clear();
        buttonsContainer.setSpacing(settings.choicesSpacing);
        buttonsContainer.setAlignment(Pos.CENTER);

        for (ChoiceOption option : options) {
            buttonsContainer.getChildren().add(createChoiceButton(option, onChoose));
        }

        bindLayout();
    }

    /**
     * Создаёт кнопки выбора для legacy-пути (SceneController.startScene).
     *
     * @param options     тексты вариантов
     * @param requestID   id сцен для перехода
     * @param trustButton видимость кнопок (false — заблокирована условием)
     * @param appSettings настройки приложения
     * @deprecated используется только мёртвым legacy-путём; новый путь —
     *             {@link #createButtons(List, Consumer)}
     */
    @Deprecated
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
     * Создаёт одну кнопку выбора: при клике закрывает панель
     * и передаёт вариант обработчику.
     *
     * @param option   вариант выбора
     * @param onChoose обработчик выбора
     * @return настроенная кнопка
     */
    private Button createChoiceButton(ChoiceOption option, Consumer<ChoiceOption> onChoose) {
        Button button = styledChoiceButton(option.getText());
        button.setOnAction(event -> {
            closeDialogButtonPane(appSettings.getRoot());
            onChoose.accept(option);
        });
        return button;
    }

    /**
     * Создаёт одну кнопку выбора legacy-пути с переходом через SceneBuilder.
     *
     * @param text    текст варианта
     * @param sceneId id сцены для перехода
     * @return настроенная кнопка
     * @deprecated используется только мёртвым legacy-путём
     */
    @Deprecated
    private Button createChoiceButton(String text, int sceneId) {
        Button button = styledChoiceButton(text);
        button.setOnAction(event -> {
            SceneInfo.addChoice(text);
            new SceneBuilder(appSettings, null, sceneId);
        });
        return button;
    }

    /**
     * Создаёт кнопку выбора со стилем и hover-анимацией, без обработчика клика.
     *
     * @param text текст варианта
     * @return настроенная кнопка
     */
    private Button styledChoiceButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("choice-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setWrapText(true);
        button.setPrefHeight(VirtualViewport.height(settings.choicesButtonHeightMultiplier));
        button.setStyle(String.format("-fx-font-size: %.1fpx;",
                VirtualViewport.height(settings.choicesFontSizeMultiplier)));
        ButtonAnimation.addButtonHoverAnimation(button);
        return button;
    }

    /**
     * Позиционирует buttonsContainer в дизайн-координатах.
     * x-position и y-position из конфига задают точку (доля экрана 0.0–1.0),
     * вокруг которой центрируется панель.
     */
    private void bindLayout() {
        buttonsContainer.setMaxWidth(
                VirtualViewport.width(settings.choicesWidthMultiplier));
        buttonsContainer.setTranslateX(
                VirtualViewport.width(settings.choicesXPosition - 0.5));
        buttonsContainer.setTranslateY(
                VirtualViewport.height(settings.choicesYPosition - 0.5));
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

package kz.aws.game.scenedetails;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import kz.aws.game.animation.AnimationUtils;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.DialogList;
import kz.aws.game.scenelist.DialogList.DialogEntry;
import kz.aws.game.scenelist.SceneInfo;
import kz.aws.game.utils.VirtualViewport;

/**
 * Компактная панель истории диалогов. Выезжает справа, занимает ~35% ширины экрана,
 * содержит прокручиваемый список последних реплик и кнопку закрытия.
 */
public class DialogTreePane {

    private static final int SLIDE_DURATION_MS = 400;

    private final VBox container;
    private StackPane root;

    /**
     * Создаёт панель истории диалогов.
     *
     * @param appSettings настройки приложения (не используется, сохранён для совместимости)
     * @param sceneID     id текущей сцены (не используется, сохранён для совместимости)
     * @param clicker     индекс текущего кадра (не используется, сохранён для совместимости)
     */
    public DialogTreePane(AppSettings appSettings, int sceneID, int clicker) {
        this.container = new VBox(8);
        buildPanel();
    }

    /**
     * Собирает панель: заголовок, ScrollPane с диалогами, кнопка закрытия.
     */
    private void buildPanel() {
        container.getStyleClass().add("hitech-panel");
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.TOP_CENTER);

        Label title = createTitle();
        ScrollPane scrollPane = createScrollableDialogList();
        Button exitButton = createExitButton();

        container.getChildren().addAll(title, scrollPane, exitButton);
    }

    /**
     * Создаёт заголовок с адаптивным шрифтом.
     *
     * @return Label заголовка
     */
    private Label createTitle() {
        Label title = new Label("История диалогов");
        title.getStyleClass().add("hitech-panel-title");
        title.setStyle(String.format(
                "-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: white;",
                VirtualViewport.height(0.025)));
        return title;
    }

    /**
     * Создаёт ScrollPane со списком записей диалогов.
     *
     * @return прокручиваемый контейнер
     */
    private ScrollPane createScrollableDialogList() {
        VBox dialogsBox = new VBox(6);
        dialogsBox.setPadding(new Insets(5));

        List<DialogEntry> dialogs = DialogList.getAllDialogs();
        Color defaultNameColor = Color.web("#ffcc00");
        Color defaultTextColor = Color.WHITE;

        for (DialogEntry entry : dialogs) {
            Color nameColor = defaultNameColor;
            Color textColor = defaultTextColor;
            if (!entry.getCharacterColor().isEmpty()) {
                nameColor = Color.web(entry.getCharacterColor());
            }
            if (!entry.getDialogColor().isEmpty()) {
                textColor = Color.web(entry.getDialogColor());
            }
            addDialogEntry(dialogsBox, entry, nameColor, textColor);
        }

        ScrollPane scroll = new ScrollPane(dialogsBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(VirtualViewport.height(0.5));
        // после layout: до него высота контента ещё 0 и прокрутка не применяется
        javafx.application.Platform.runLater(() -> scroll.setVvalue(1.0));
        return scroll;
    }

    /**
     * Добавляет одну запись диалога (имя + текст) в контейнер.
     *
     * @param dialogsBox контейнер записей
     * @param entry      запись диалога
     * @param nameColor  цвет имени
     * @param textColor  цвет текста
     */
    private void addDialogEntry(VBox dialogsBox, DialogEntry entry,
                                 Color nameColor, Color textColor) {
        String speakerName = entry.getCharacterName().isEmpty() ? "[мысли]" : entry.getCharacterName();

        Label nameLabel = new Label(speakerName);
        nameLabel.setTextFill(nameColor);
        nameLabel.setStyle(String.format(
                "-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-font-family: 'Constantia', sans-serif;",
                VirtualViewport.height(0.02)));

        Label textLabel = new Label(entry.getDialogText());
        textLabel.setTextFill(textColor);
        textLabel.setWrapText(true);
        textLabel.setStyle(String.format(
                "-fx-font-size: %.0fpx; -fx-font-family: 'Constantia', sans-serif;",
                VirtualViewport.height(0.016)));

        dialogsBox.getChildren().addAll(nameLabel, textLabel);
    }

    /**
     * Создаёт кнопку «Закрыть» с адаптивным стилем.
     *
     * @return кнопка закрытия
     */
    private Button createExitButton() {
        Button exit = new Button("Закрыть");
        exit.getStyleClass().add("game-button");
        exit.setOnAction(event -> removeFromScene());
        ButtonAnimation.addButtonHoverAnimation(exit);
        return exit;
    }

    /**
     * Показывает панель с анимацией slide-in справа. Занимает ~35% ширины экрана.
     *
     * @param root корневой StackPane
     */
    public void addInScene(StackPane root) {
        this.root = root;
        StackPane.setAlignment(container, Pos.CENTER_RIGHT);

        container.setPrefWidth(VirtualViewport.width(0.35));
        container.setMaxWidth(VirtualViewport.width(0.35));
        StackPane.setMargin(container, new Insets(
                VirtualViewport.height(0.05), VirtualViewport.width(0.02),
                VirtualViewport.height(0.05), 0));

        kz.aws.game.utils.OverlayMarker.mark(container);
        root.getChildren().add(container);
        AnimationUtils.slideInFromSide(container,
                VirtualViewport.DESIGN_WIDTH, 0, SLIDE_DURATION_MS);
        SceneInfo.disableEventHandler(root);
    }

    /**
     * Закрывает панель с анимацией slide-out вправо.
     */
    public void removeFromScene() {
        AnimationUtils.slideOutToSide(container, 0,
                VirtualViewport.DESIGN_WIDTH, SLIDE_DURATION_MS,
                () -> {
                    root.getChildren().remove(container);
                    SceneInfo.enableEventHandler(root);
                });
    }
}

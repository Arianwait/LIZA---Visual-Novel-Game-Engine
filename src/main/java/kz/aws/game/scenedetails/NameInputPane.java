package kz.aws.game.scenedetails;

import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Окно ввода имени (или другого значения) в стиле игры.
 * Размеры окна и отступы — динамические (% от экрана).
 * Фон панели задаётся в CSS (.name-input-pane): картинка или градиент — можно поставить свою «доску».
 */
public class NameInputPane {

    /** Ширина панели в долях от ширины экрана (0.45 = 45%). */
    private static final double PANEL_WIDTH_RATIO = 0.45;
    /** Высота панели в долях от высоты экрана. */
    private static final double PANEL_HEIGHT_RATIO = 0.28;
    /** Отступ панели от краёв экрана (доли: 0.08 = 8%). */
    private static final double MARGIN_RATIO = 0.08;
    /** Внутренние отступы панели (доли от экрана). */
    private static final double PADDING_RATIO = 0.06;
    /** Размер шрифта в долях от высоты экрана. */
    private static final double FONT_SIZE_RATIO = 0.022;
    /** Минимальный размер шрифта в пикселях. */
    private static final int MIN_FONT_SIZE = 16;

    /** Имена CSS-классов для переопределения стилей (кроме размеров текста и отступов окна). */
    public static final String STYLE_CLASS_PANE = "name-input-pane";
    public static final String STYLE_CLASS_LABEL = "name-input-label";
    public static final String STYLE_CLASS_FIELD = "name-input-field";
    public static final String STYLE_CLASS_OK_BUTTON = "name-input-ok-button";

    private StackPane container;
    private VBox menu;
    private final StackPane root;
    private final AppSettings appSettings;
    private final String prompt;
    private final String initialValue;
    private final Consumer<String> onConfirm;

    /**
     * Показывает окно ввода в стиле игры (размер и отступы в % от экрана).
     * Одна кнопка OK, отмены нет.
     *
     * @param root        Корень сцены.
     * @param appSettings Настройки приложения.
     * @param prompt      Подсказка (например, "Введите имя:").
     * @param initialValue Начальное значение.
     * @param onConfirm   Вызывается при нажатии OK с введённой строкой.
     * @param onCancel    Не используется (кнопки отмены нет), оставлен для совместимости API.
     */
    public static void show(StackPane root, AppSettings appSettings, String prompt,
                            String initialValue, Consumer<String> onConfirm, Runnable onCancel) {
        new NameInputPane(root, appSettings, prompt, initialValue, onConfirm).show();
    }

    private NameInputPane(StackPane root, AppSettings appSettings, String prompt,
                          String initialValue, Consumer<String> onConfirm) {
        this.root = root;
        this.appSettings = appSettings;
        this.prompt = prompt;
        this.initialValue = initialValue;
        this.onConfirm = onConfirm != null ? onConfirm : v -> {};
    }

    private void show() {
        container = new StackPane();
        container.setMaxWidth(Double.MAX_VALUE);
        container.setMaxHeight(Double.MAX_VALUE);
        container.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        container.setOnMouseClicked(e -> e.consume());

        menu = new VBox();
        menu.getStyleClass().add(STYLE_CLASS_PANE);
        menu.setAlignment(Pos.CENTER);
        menu.spacingProperty().bind(root.heightProperty().multiply(0.015));
        menu.maxWidthProperty().bind(root.widthProperty().multiply(PANEL_WIDTH_RATIO));
        menu.maxHeightProperty().bind(root.heightProperty().multiply(PANEL_HEIGHT_RATIO));
        menu.minWidthProperty().bind(root.widthProperty().multiply(PANEL_WIDTH_RATIO * 0.7));
        menu.minHeightProperty().bind(root.heightProperty().multiply(PANEL_HEIGHT_RATIO * 0.7));
        menu.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(
                        root.getHeight() * PADDING_RATIO,
                        root.getWidth() * PADDING_RATIO,
                        root.getHeight() * PADDING_RATIO,
                        root.getWidth() * PADDING_RATIO),
                root.widthProperty(), root.heightProperty()));
        /* Фон панели задаётся в CSS: .name-input-pane { -fx-background-image: ... } или -fx-background-color (градиент). */

        String labelText = (this.prompt != null && !this.prompt.isEmpty()) ? this.prompt : "Введите значение:";
        Text label = new Text(labelText);
        label.getStyleClass().add(STYLE_CLASS_LABEL);
        label.styleProperty().bind(Bindings.format("-fx-font-size: %.0fpx;",
                Bindings.createDoubleBinding(
                        () -> Math.max(MIN_FONT_SIZE, root.getHeight() * FONT_SIZE_RATIO),
                        root.heightProperty())));

        TextField textField = new TextField();
        textField.getStyleClass().add(STYLE_CLASS_FIELD);
        textField.setPromptText("Введите значение...");
        textField.setText(this.initialValue != null ? this.initialValue : "");
        textField.selectAll();
        textField.styleProperty().bind(Bindings.format("-fx-font-size: %.0fpx;",
                Bindings.createDoubleBinding(
                        () -> Math.max(MIN_FONT_SIZE - 2, root.getHeight() * FONT_SIZE_RATIO - 2),
                        root.heightProperty())));
        textField.prefWidthProperty().bind(menu.widthProperty().multiply(0.9));
        textField.setMaxWidth(Double.MAX_VALUE);

        Text errorText = new Text("");
        errorText.setStyle("-fx-fill: #c47a7a;");
        errorText.styleProperty().bind(Bindings.format("-fx-font-size: %.0fpx; -fx-fill: #c47a7a;",
                Bindings.createDoubleBinding(
                        () -> Math.max(MIN_FONT_SIZE - 2, root.getHeight() * FONT_SIZE_RATIO - 2),
                        root.heightProperty())));
        errorText.setVisible(false);

        Button okButton = new Button("OK");
        okButton.getStyleClass().add(STYLE_CLASS_OK_BUTTON);
        okButton.setOnAction(e -> {
            String value = textField.getText() != null ? textField.getText().trim() : "";
            if (value.isEmpty()) {
                errorText.setText("Поле не может быть пустым");
                errorText.setVisible(true);
                textField.requestFocus();
                return;
            }
            onConfirm.accept(value);
            close();
        });
        okButton.styleProperty().bind(Bindings.format("-fx-font-size: %.0fpx;",
                Bindings.createDoubleBinding(
                        () -> Math.max(MIN_FONT_SIZE, root.getHeight() * FONT_SIZE_RATIO),
                        root.heightProperty())));
        appSettings.ButtonStyle(okButton, menu);

        menu.getChildren().addAll(label, textField, errorText, okButton);
        label.layoutBoundsProperty().addListener((o, a, b) -> updateInnerMargins());
        textField.layoutBoundsProperty().addListener((o, a, b) -> updateInnerMargins());
        updateInnerMargins();

        container.getChildren().add(menu);
        StackPane.setAlignment(menu, Pos.CENTER);
        root.widthProperty().addListener((o, a, b) -> updatePaneMargin());
        root.heightProperty().addListener((o, a, b) -> updatePaneMargin());
        updatePaneMargin();
        root.getChildren().add(container);
        container.toFront();
        /* Блокируем клик по сцене и кнопки панели, чтобы во время ввода нельзя было прокручивать диалог мышкой */
        SceneInfo.disableEventHandler(root);
    }

    private void updateInnerMargins() {
        if (menu == null || menu.getChildren().size() < 3) return;
        double h = root.getHeight() > 0 ? root.getHeight() : 600;
        VBox.setMargin(menu.getChildren().get(0), new Insets(0, 0, h * 0.008, 0));
        VBox.setMargin(menu.getChildren().get(1), new Insets(0, 0, h * 0.012, 0));
    }

    private void updatePaneMargin() {
        if (menu == null) return;
        double w = root.getWidth() > 0 ? root.getWidth() : 800;
        double h = root.getHeight() > 0 ? root.getHeight() : 600;
        StackPane.setMargin(menu, new Insets(h * MARGIN_RATIO, w * MARGIN_RATIO, h * MARGIN_RATIO, w * MARGIN_RATIO));
    }

    private void close() {
        SceneInfo.enableEventHandler(root);
        if (root.getChildren().contains(container)) {
            root.getChildren().remove(container);
        }
    }
}

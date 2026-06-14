package kz.aws.game.scenedetails;

import java.io.File;
import java.net.URL;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import kz.aws.game.animation.ButtonAnimation;
import kz.aws.game.appsettings.AppSettings;

/**
 * FXML-контроллер диалога подтверждения. Заменяет ConfirmActionDialog,
 * убирая дублирование двух идентичных show()-методов и добавляя
 * адаптивные биндинги для корректного масштабирования.
 */
public class ConfirmDialogController extends StackPane {

    private static final String FXML_PATH = "lib/fxml/confirm-dialog.fxml";

    @FXML private ImageView backgroundImage;
    @FXML private VBox contentBox;
    @FXML private Text messageText;
    @FXML private HBox buttonBox;
    @FXML private Button yesButton;
    @FXML private Button noButton;

    private final AppSettings appSettings;

    /**
     * Создаёт контроллер диалога подтверждения.
     *
     * @param appSettings настройки приложения
     */
    public ConfirmDialogController(AppSettings appSettings) {
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
            throw new RuntimeException("Failed to load confirm-dialog.fxml", e);
        }
    }

    /**
     * Отображает диалог подтверждения с изображением, сообщением и кнопками Да/Нет.
     *
     * @param message   сообщение пользователю
     * @param imagePath путь к фоновому изображению (null — без изображения)
     * @param onConfirm действие при нажатии "Да"
     * @param onCancel  действие при нажатии "Нет"
     */
    public void show(String message, String imagePath, Runnable onConfirm, Runnable onCancel) {
        setupImage(imagePath);
        setupMessage(message);
        setupButtons(onConfirm, onCancel);
        bindResponsiveLayout();
        appSettings.getRoot().getChildren().add(this);
    }

    /**
     * Перегрузка show() с параметром buttonsBox для обратной совместимости.
     * Параметр buttonsBox больше не используется — стили применяются через биндинги.
     *
     * @param message   сообщение
     * @param imagePath путь к изображению
     * @param onConfirm действие при подтверждении
     * @param onCancel  действие при отмене
     * @param buttonsBox игнорируется (для совместимости со старым API)
     */
    public void show(String message, String imagePath, Runnable onConfirm,
                     Runnable onCancel, Object buttonsBox) {
        show(message, imagePath, onConfirm, onCancel);
    }

    /**
     * Настраивает фоновое изображение диалога.
     *
     * @param imagePath путь к изображению (null — скрывает ImageView)
     */
    private void setupImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            backgroundImage.setVisible(false);
            return;
        }
        Scene scene = appSettings.getScene();
        backgroundImage.setImage(new Image(imagePath));
        backgroundImage.fitWidthProperty().bind(scene.widthProperty().multiply(0.5));
        backgroundImage.fitHeightProperty().bind(scene.heightProperty().multiply(0.3));
    }

    /**
     * Настраивает текст сообщения с адаптивным шрифтом.
     *
     * @param message текст сообщения
     */
    private void setupMessage(String message) {
        messageText.setText(message);
        messageText.setFill(javafx.scene.paint.Color.WHITE);
        messageText.styleProperty().bind(Bindings.format(
                "-fx-font-size: %.0fpx; -fx-font-weight: bold;",
                appSettings.getRoot().heightProperty().multiply(0.025)));
        messageText.wrappingWidthProperty().bind(
                this.prefWidthProperty().multiply(0.85));
    }

    /**
     * Настраивает кнопки Да/Нет с действиями и адаптивными стилями.
     *
     * @param onConfirm действие при подтверждении
     * @param onCancel  действие при отмене
     */
    private void setupButtons(Runnable onConfirm, Runnable onCancel) {
        yesButton.setOnAction(e -> {
            if (onConfirm != null) onConfirm.run();
            closeDialog();
        });
        noButton.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
            closeDialog();
        });
        styleButton(yesButton);
        styleButton(noButton);
    }

    /**
     * Применяет адаптивный стиль к кнопке.
     *
     * @param button кнопка
     */
    private void styleButton(Button button) {
        button.getStyleClass().add("game-button");
        button.prefWidthProperty().bind(appSettings.getScene().widthProperty().multiply(0.1));
        button.prefHeightProperty().bind(appSettings.getScene().heightProperty().multiply(0.05));
        ButtonAnimation.addButtonHoverAnimation(button);
    }

    /**
     * Привязывает отступы элементов к размерам сцены через listener.
     */
    private void bindResponsiveLayout() {
        Scene scene = appSettings.getScene();
        this.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        this.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        this.prefWidthProperty().bind(scene.widthProperty().multiply(0.4));
        StackPane.setAlignment(this, Pos.CENTER);

        contentBox.spacingProperty().bind(scene.heightProperty().multiply(0.025));
        contentBox.paddingProperty().bind(javafx.beans.binding.Bindings.createObjectBinding(
                () -> new javafx.geometry.Insets(
                        scene.getHeight() * 0.035,
                        scene.getWidth() * 0.03,
                        scene.getHeight() * 0.035,
                        scene.getWidth() * 0.03),
                scene.widthProperty(), scene.heightProperty()));
        buttonBox.spacingProperty().bind(scene.widthProperty().multiply(0.02));
    }

    /**
     * Закрывает диалог, удаляя его из root.
     */
    private void closeDialog() {
        appSettings.getRoot().getChildren().remove(this);
    }
}

package kz.aws.game.scenedetails;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import kz.aws.game.appsettings.AppSettings;

public class ConfirmActionDialog {

    private StackPane container; // Контейнер для отображения диалога
    private AppSettings appSettings;

    public ConfirmActionDialog(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    /**
     * Отображает диалог подтверждения действия.
     *
     * @param message      Сообщение, отображаемое пользователю.
     * @param imagePath    Путь к изображению (или null, если изображение не нужно).
     * @param onConfirm    Действие при нажатии "Да".
     * @param onCancel     Действие при нажатии "Нет".
     */
    public void show(String message, String imagePath, Runnable onConfirm, Runnable onCancel, VBox buttonsBox) {
        container = new StackPane(); // Создаем контейнер для диалога

        // Добавляем изображение, если задан путь
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(appSettings.getScene().getWidth() * 0.5); // Ширина изображения
            imageView.setFitHeight(appSettings.getScene().getHeight() * 0.3); // Высота изображения
            container.getChildren().add(imageView);
        }

        // Сообщение
        Text accessLabel = new Text(message);
        accessLabel.setStyle("-fx-font-size: " + appSettings.getRoot().getHeight() * 0.02 +"4; -fx-font-weight: bold;");

        // Кнопка "Да"
        Button yesButton = new Button("Да");
        yesButton.setOnAction(event -> {
            if (onConfirm != null) {
                onConfirm.run(); // Выполняем действие при подтверждении
            }
            closeDialog();
        });
        appSettings.ButtonStyle(yesButton, buttonsBox);

        // Кнопка "Нет"
        Button noButton = new Button("Нет");
        noButton.setOnAction(event -> {
            if (onCancel != null) {
                onCancel.run(); // Выполняем действие при отмене
            }
            closeDialog();
        });
        appSettings.ButtonStyle(noButton, buttonsBox);

        // Контейнер для кнопок
        HBox buttonBox = new HBox(20);
        buttonBox.getChildren().addAll(yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Размещение элементов в контейнере
        container.getChildren().addAll(buttonBox, accessLabel);
        StackPane.setAlignment(accessLabel, Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        StackPane.setMargin(buttonBox, new Insets(appSettings.getScene().getHeight() * 0.1, appSettings.getScene().getWidth() * 0.30,  0, appSettings.getScene().getWidth() * 0.30));
        StackPane.setMargin(accessLabel, new Insets(0, appSettings.getScene().getWidth() * 0.30,  0, appSettings.getScene().getWidth() * 0.30));
        // Добавляем контейнер в root
        appSettings.getRoot().getChildren().add(container);
    }

    /**
     * Отображает диалог подтверждения действия.
     *
     * @param message      Сообщение, отображаемое пользователю.
     * @param imagePath    Путь к изображению (или null, если изображение не нужно).
     * @param onConfirm    Действие при нажатии "Да".
     * @param onCancel     Действие при нажатии "Нет".
     */
    public void show(String message, String imagePath, Runnable onConfirm, Runnable onCancel) {
        container = new StackPane(); // Создаем контейнер для диалога

        // Добавляем изображение, если задан путь
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = new Image(imagePath);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(appSettings.getScene().getWidth() * 0.5); // Ширина изображения
            imageView.setFitHeight(appSettings.getScene().getHeight() * 0.3); // Высота изображения
            container.getChildren().add(imageView);
        }

        // Сообщение
        Text accessLabel = new Text(message);
        accessLabel.setStyle("-fx-font-size: " + appSettings.getRoot().getHeight() * 0.02 +"4; -fx-font-weight: bold;");

        // Кнопка "Да"
        Button yesButton = new Button("Да");
        yesButton.setOnAction(event -> {
            if (onConfirm != null) {
                onConfirm.run(); // Выполняем действие при подтверждении
            }
            closeDialog();
        });
        appSettings.ButtonStyle(yesButton, appSettings.getScene());

        // Кнопка "Нет"
        Button noButton = new Button("Нет");
        noButton.setOnAction(event -> {
            if (onCancel != null) {
                onCancel.run(); // Выполняем действие при отмене
            }
            closeDialog();
        });
        appSettings.ButtonStyle(noButton,  appSettings.getScene());

        // Контейнер для кнопок
        HBox buttonBox = new HBox(20);
        buttonBox.getChildren().addAll(yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Размещение элементов в контейнере
        container.getChildren().addAll(buttonBox, accessLabel);
        StackPane.setAlignment(accessLabel, Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        StackPane.setMargin(buttonBox, new Insets(appSettings.getScene().getHeight() * 0.1, appSettings.getScene().getWidth() * 0.30,  0, appSettings.getScene().getWidth() * 0.30));
        StackPane.setMargin(accessLabel, new Insets(0, appSettings.getScene().getWidth() * 0.30,  0, appSettings.getScene().getWidth() * 0.30));
        // Добавляем контейнер в root
        appSettings.getRoot().getChildren().add(container);
    }
    
    /**
     * Закрывает диалог подтверждения.
     */
    private void closeDialog() {
        appSettings.getRoot().getChildren().remove(container);
    }
}

package kz.aws.game.scenedetails;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Класс для создания полупрозрачного затемнения поверх существующей сцены
 * и вывода текста на экране.
 */
public class DimOverlay {

    // Основной контейнер, в котором лежит тёмная подложка и текст
    private StackPane overlayPane;

    // Фоновый прямоугольник, закрывающий всё
    private Rectangle dimRect;

    // Текст, который будет показываться поверх затемнения
    private Label textLabel;

    // Опционально: хранить текущий FadeTransition (для управления анимацией)
    private FadeTransition fadeTransition;

    /**
     * @param text      Текст, который будет отображаться поверх.
     * @param dimOpacity Степень затемнения (0.0 – совсем прозрачный, 1.0 – совсем непрозрачный).
     */
    public DimOverlay(String text, double dimOpacity) {
        // 1. Создаём контейнер, чтобы в нём собрать все необходимые элементы
        overlayPane = new StackPane();

        // 2. Тёмный прямоугольник для создания эффекта затемнения
        dimRect = new Rectangle();
        dimRect.setFill(Color.BLACK);
        dimRect.setOpacity(dimOpacity);

        dimRect.widthProperty().bind(overlayPane.widthProperty());
        dimRect.heightProperty().bind(overlayPane.heightProperty());
        
        // 3. Текст, который будет отображаться поверх
        textLabel = new Label(text);
        textLabel.setTextFill(Color.WHITE);
        textLabel.setStyle("-fx-font-size: 50; -fx-alignment: center;");

        // 4. Кладём прямоугольник и текст в overlayPane
        overlayPane.getChildren().addAll(dimRect, textLabel);

        // 5. Создаём заготовку анимации «появления»/«исчезновения»
        fadeTransition = new FadeTransition(Duration.millis(500), overlayPane);
        // Пока не запускаем — запускаем при отображении
    }

    /**
     * Добавляет затемняющий слой в указанный контейнер и плавно показывает его.
     * @param parentContainer любой контейнер, например ваш root (StackPane).
     */
    public void showOverlay(StackPane parentContainer) {
        // Сначала добавляем overlayPane в родитель
        if (!parentContainer.getChildren().contains(overlayPane)) {
            parentContainer.getChildren().add(overlayPane);
        }

        // Ставим overlayPane выше (на случай, если там уже есть другие элементы)
        overlayPane.toFront();

        // Останавливаем текущую анимацию: иначе от прошлого hide остался бы
        // обработчик, удаляющий overlay сразу после появления
        fadeTransition.stop();
        fadeTransition.setOnFinished(null);

        overlayPane.setOpacity(0); // начальное значение
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1.0); // полная видимость
        fadeTransition.play();
    }

    /**
     * Плавно скрывает overlayPane, а после скрытия — удаляет его из контейнера.
     */
    public void hideOverlay(StackPane parentContainer) {
        // Если overlayPane не добавлен — выходим
        if (!parentContainer.getChildren().contains(overlayPane)) {
            return;
        }

        // Останавливаем текущую анимацию появления перед перенастройкой
        fadeTransition.stop();

        // Настраиваем анимацию исчезновения
        fadeTransition.setFromValue(overlayPane.getOpacity());
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            // По окончании анимации убираем overlayPane
            parentContainer.getChildren().remove(overlayPane);
        });

        fadeTransition.play();
    }

    /**
     * Изменить текст уже после создания DimOverlay.
     */
    public void setText(String newText) {
        textLabel.setText(newText);
    }

    /**
     * Изменить степень затемнения после создания DimOverlay.
     */
    public void setDimOpacity(double newOpacity) {
        dimRect.setOpacity(newOpacity);
    }

    /**
     * Получить доступ к корневому контейнеру (если вдруг понадобится).
     */
    public StackPane getOverlayPane() {
        return overlayPane;
    }
}

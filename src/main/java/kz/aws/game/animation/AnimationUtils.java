package kz.aws.game.animation;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class AnimationUtils {

    /**
     * Анимация появления элемента с боку экрана.
     *
     * @param node         Элемент, который нужно анимировать.
     * @param fromX        Начальная позиция по оси X.
     * @param toX          Конечная позиция по оси X.
     * @param durationMs   Длительность анимации в миллисекундах.
     */
    public static void slideInFromSide(Node node, double fromX, double toX, int durationMs) {
        node.setTranslateX(fromX); // Устанавливаем начальную позицию
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(durationMs), node);
        slideIn.setFromX(fromX);
        slideIn.setToX(toX);
        slideIn.play(); // Запускаем анимацию
    }

    /**
     * Анимация скрытия элемента в сторону.
     *
     * @param node         Элемент, который нужно анимировать.
     * @param fromX        Начальная позиция по оси X.
     * @param toX          Конечная позиция по оси X.
     * @param durationMs   Длительность анимации в миллисекундах.
     * @param onFinish     Действие, выполняемое после завершения анимации.
     */
    public static void slideOutToSide(Node node, double fromX, double toX, int durationMs, Runnable onFinish) {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(durationMs), node);
        slideOut.setFromX(fromX);
        slideOut.setToX(toX);
        slideOut.setOnFinished(event -> {
            if (onFinish != null) {
                onFinish.run(); // Выполняем действие после завершения анимации
            }
        });
        slideOut.play(); // Запускаем анимацию
    }
}

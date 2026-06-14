package kz.aws.game.engine.effect;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import kz.aws.game.engine.model.VisualEffectCommand;

/**
 * Эффект тряски экрана — {@code sceneContentLayer} дрожит по горизонтали.
 *
 * <p>Цикл: влево → центр → вправо → центр, повторяется бесконечно.
 * UI (текстовая панель, кнопки) не затрагивается.
 *
 * <p>При остановке слой плавно возвращается в исходное положение.
 */
@VisualEffect("shake")
public class ShakeEffectPlayer implements VisualEffectPlayer {

    private static final double AMPLITUDE = 6.0;
    private static final int CYCLE_MS = 80;

    private Timeline timeline;

    /**
     * Запускает бесконечную тряску слоя контента.
     *
     * @param root              корневой StackPane (не используется)
     * @param sceneContentLayer слой фона и персонажей — трясётся
     * @param command           команда эффекта
     * @param onComplete        не вызывается (эффект бесконечный)
     */
    @Override
    public void play(StackPane root, Pane sceneContentLayer,
                     VisualEffectCommand command, Runnable onComplete) {
        timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(sceneContentLayer.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(CYCLE_MS),
                new KeyValue(sceneContentLayer.translateXProperty(), -AMPLITUDE, Interpolator.LINEAR)),
            new KeyFrame(Duration.millis(CYCLE_MS * 2),
                new KeyValue(sceneContentLayer.translateXProperty(), AMPLITUDE, Interpolator.LINEAR)),
            new KeyFrame(Duration.millis(CYCLE_MS * 3),
                new KeyValue(sceneContentLayer.translateXProperty(), 0, Interpolator.LINEAR))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Останавливает тряску и возвращает слой в исходное положение.
     *
     * @param root              корневой StackPane (не используется)
     * @param sceneContentLayer слой контента — сбрасывается translateX
     */
    @Override
    public void stop(StackPane root, Pane sceneContentLayer) {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (sceneContentLayer != null) {
            sceneContentLayer.setTranslateX(0);
        }
    }
}

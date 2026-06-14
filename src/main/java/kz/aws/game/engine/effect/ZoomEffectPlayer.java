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
 * Эффект плавного приближения (зум) сцены.
 *
 * <p>Масштабирует {@code sceneContentLayer} (фон + персонажи)
 * от 1.0 до 1.3 с замедлением (быстрый старт, плавное торможение).
 *
 * <p>UI (текстовая панель, кнопки) не затрагивается.
 */
@VisualEffect("zoom")
public class ZoomEffectPlayer implements VisualEffectPlayer {

    private static final double START_SCALE = 1.0;
    private static final double END_SCALE = 1.3;
    private static final int DURATION_MS = 3000;

    /** Интерполятор: быстрый старт → плавное торможение. */
    private static final Interpolator DECELERATE = Interpolator.SPLINE(0.0, 0.0, 0.2, 1.0);

    private Timeline timeline;

    /**
     * Запускает анимацию зума на слое контента.
     *
     * @param root              корневой StackPane (не используется для зума)
     * @param sceneContentLayer слой фона и персонажей — масштабируемый
     * @param command           команда эффекта
     * @param onComplete        callback по окончании (может быть null)
     */
    @Override
    public void play(StackPane root, Pane sceneContentLayer, VisualEffectCommand command, Runnable onComplete) {
        sceneContentLayer.setScaleX(START_SCALE);
        sceneContentLayer.setScaleY(START_SCALE);

        timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(sceneContentLayer.scaleXProperty(), START_SCALE),
                new KeyValue(sceneContentLayer.scaleYProperty(), START_SCALE)
            ),
            new KeyFrame(Duration.millis(DURATION_MS),
                new KeyValue(sceneContentLayer.scaleXProperty(), END_SCALE, DECELERATE),
                new KeyValue(sceneContentLayer.scaleYProperty(), END_SCALE, DECELERATE)
            )
        );

        timeline.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });
        timeline.play();
    }

    /**
     * Останавливает зум и сбрасывает масштаб в 1.0.
     *
     * @param root              корневой StackPane (не используется)
     * @param sceneContentLayer слой контента — сбрасывается масштаб
     */
    @Override
    public void stop(StackPane root, Pane sceneContentLayer) {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (sceneContentLayer != null) {
            sceneContentLayer.setScaleX(1.0);
            sceneContentLayer.setScaleY(1.0);
        }
    }
}

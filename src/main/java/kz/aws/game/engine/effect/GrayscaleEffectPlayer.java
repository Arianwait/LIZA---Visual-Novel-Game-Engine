package kz.aws.game.engine.effect;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import kz.aws.game.engine.model.VisualEffectCommand;

/**
 * Эффект обесцвечивания (чёрно-белый фильтр).
 *
 * <p>Плавно применяет {@link ColorAdjust} с полным обесцвечиванием
 * к {@code sceneContentLayer}. Если на слое уже есть эффект,
 * подключается через input-чейн.
 */
@VisualEffect("grayscale")
public class GrayscaleEffectPlayer implements VisualEffectPlayer {

    private static final int DURATION_MS = 1500;

    private Timeline timeline;
    private Effect previousEffect;

    /**
     * Запускает плавное обесцвечивание сцены.
     *
     * @param root              корневой StackPane (не используется)
     * @param sceneContentLayer слой контента — к нему применяется фильтр
     * @param command           команда эффекта
     * @param onComplete        callback по окончании (может быть null)
     */
    @Override
    public void play(StackPane root, Pane sceneContentLayer, VisualEffectCommand command, Runnable onComplete) {
        previousEffect = sceneContentLayer.getEffect();

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(0);
        chainEffect(sceneContentLayer, colorAdjust);

        timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(colorAdjust.saturationProperty(), 0)),
            new KeyFrame(Duration.millis(DURATION_MS),
                new KeyValue(colorAdjust.saturationProperty(), -1.0))
        );
        timeline.setOnFinished(e -> {
            if (onComplete != null) onComplete.run();
        });
        timeline.play();
    }

    /**
     * Останавливает фильтр и восстанавливает предыдущий эффект.
     *
     * @param root              корневой StackPane (не используется)
     * @param sceneContentLayer слой контента — сбрасывается эффект
     */
    @Override
    public void stop(StackPane root, Pane sceneContentLayer) {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        if (sceneContentLayer != null) {
            sceneContentLayer.setEffect(previousEffect);
        }
    }

    /**
     * Подключает новый эффект к слою, сохраняя предыдущий через input-чейн.
     *
     * @param layer  слой контента
     * @param effect новый эффект для применения
     */
    private void chainEffect(Pane layer, ColorAdjust effect) {
        Effect current = layer.getEffect();
        if (current != null) {
            effect.setInput(current);
        }
        layer.setEffect(effect);
    }
}

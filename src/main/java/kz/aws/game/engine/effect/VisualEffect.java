package kz.aws.game.engine.effect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркер реализации визуального эффекта.
 *
 * <p>Класс с этой аннотацией реализует {@link VisualEffectPlayer}
 * и автоматически регистрируется в {@link VisualEffectRegistry}.
 *
 * <p>Пример:
 * <pre>
 * {@literal @}VisualEffect("blink")
 * public class BlinkEffectPlayer implements VisualEffectPlayer {
 *     {@literal @}Override
 *     public void play(StackPane root, VisualEffectCommand cmd, Runnable onComplete) { ... }
 *     {@literal @}Override
 *     public void stop(StackPane root) { ... }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface VisualEffect {

    /**
     * Идентификатор эффекта — совпадает с {@code VisualEffectCommand.getEffectType()}.
     *
     * @return id эффекта (blink, zoom, colorFilter и др.)
     */
    String value();
}

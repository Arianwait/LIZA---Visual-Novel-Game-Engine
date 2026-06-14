package kz.aws.game.panel.puzzle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @deprecated Используй {@link kz.aws.game.panel.GamePanel}.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PuzzlePanel {
    String id();
    String title() default "";
    double widthPercent() default 60.0;
    double heightPercent() default 70.0;
    boolean modal() default true;
}

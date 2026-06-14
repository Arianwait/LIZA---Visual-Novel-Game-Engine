package kz.aws.game.buttonaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркер действия кнопки. Id совпадает с id кнопки в lib/config/UI/Buttons.xml.
 * Класс с этой аннотацией реализует {@link ButtonActionHandler} и регистрируется в {@link ButtonActionRegistry}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ButtonAction {
    /** Id кнопки из Buttons.xml (например "menu-btn-exit", "game-btn-next"). */
    String value();
}

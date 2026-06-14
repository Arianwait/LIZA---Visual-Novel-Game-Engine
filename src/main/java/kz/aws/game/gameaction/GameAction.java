package kz.aws.game.gameaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркер игровой команды (движение персонажа, показ, фон и т.д.).
 * Id совпадает с именем действия в сценарии (например "move_Left", "showPerson").
 * Класс с этой аннотацией реализует {@link GameActionHandler} и регистрируется в {@link GameActionRegistry}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GameAction {
    /** Имя команды из сценария (type="character" action="...", или legacy "character:action:value"). */
    String value();
}

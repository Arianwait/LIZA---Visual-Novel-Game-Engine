package kz.aws.game.panel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркер панели/окна. Id совпадает с id в lib/config/UI/Panels.xml.
 * Класс с этой аннотацией должен иметь конструктор (AppSettings) и регистрируется в {@link PanelRegistry}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Panel {
    /** Id панели из Panels.xml (например "settings", "scene-selection"). */
    String value();
}

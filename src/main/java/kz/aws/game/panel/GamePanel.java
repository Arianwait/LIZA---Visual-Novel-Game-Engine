package kz.aws.game.panel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Маркер игровой панели. Класс с этой аннотацией наследует {@link BaseGamePanel}
 * и автоматически регистрируется в {@link PuzzleRegistry}.
 *
 * <p>Панель — это любой интерактивный оверлей поверх сцены:
 * доска улик, осмотр предмета, загадка, карта, диалог выбора и т.д.
 * По завершению возвращает {@link PanelResult} (успех/провал + данные),
 * который записывается как флаг в {@code SceneController}.
 *
 * <p>Пример:
 * <pre>
 * {@literal @}GamePanel(id = "evidence_board", title = "Доска улик",
 *            widthPercent = 70, heightPercent = 80)
 * public class EvidenceBoard extends BaseGamePanel {
 *     {@literal @}Override
 *     protected void initialize() { ... }
 * }
 * </pre>
 *
 * <p>Вызов из XML:
 * <pre>
 *   &lt;command type="panel" id="evidence_board" flag="board_checked"/&gt;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GamePanel {

    /** Уникальный id панели — используется в XML-команде {@code type="panel"}. */
    String id();

    /** Заголовок панели (отображается в шапке, если используется). */
    String title() default "";

    /** Ширина панели в % от ширины окна. */
    double widthPercent() default 60.0;

    /** Высота панели в % от высоты окна. */
    double heightPercent() default 70.0;

    /**
     * {@code true} — затемняет фон и блокирует клики на сцену пока панель открыта.
     * {@code false} — панель висит поверх, сцена остаётся кликабельной.
     */
    boolean modal() default true;
}

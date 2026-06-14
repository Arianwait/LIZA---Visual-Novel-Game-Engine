package kz.aws.game.engine.model;

/**
 * Команда эффекта моргания (полузакрытые глаза).
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="effect" effect="blink"/&gt;
 * </pre>
 */
public class BlinkEffectCommand extends VisualEffectCommand {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду эффекта моргания.
     */
    public BlinkEffectCommand() {
        super("blink");
    }
}

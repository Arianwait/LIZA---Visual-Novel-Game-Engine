package kz.aws.game.engine.model;

/**
 * Команда эффекта плавного приближения (зум).
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="effect" effect="zoom"/&gt;
 * </pre>
 */
public class ZoomEffectCommand extends VisualEffectCommand {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду зума.
     */
    public ZoomEffectCommand() {
        super("zoom");
    }
}

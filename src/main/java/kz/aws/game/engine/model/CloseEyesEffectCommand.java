package kz.aws.game.engine.model;

/**
 * Команда эффекта засыпания — глаза моргают, закрываясь всё сильнее,
 * и в конце полностью закрываются (экран чёрный).
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="effect" effect="closeEyes"/&gt;
 * </pre>
 */
public class CloseEyesEffectCommand extends VisualEffectCommand {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду эффекта засыпания.
     */
    public CloseEyesEffectCommand() {
        super("closeEyes");
    }
}

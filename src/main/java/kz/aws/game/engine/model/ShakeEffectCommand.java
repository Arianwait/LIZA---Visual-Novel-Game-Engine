package kz.aws.game.engine.model;

/**
 * Команда эффекта тряски экрана.
 *
 * <p>Сцена (фон + персонажи) быстро дрожит из стороны в сторону.
 * Эффект остаётся до явной команды {@code stopEffect("shake")}.
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="effect" effect="shake"/&gt;
 * </pre>
 */
public class ShakeEffectCommand extends VisualEffectCommand {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду эффекта тряски.
     */
    public ShakeEffectCommand() {
        super("shake");
    }
}

package kz.aws.game.engine.model;

/**
 * Команда визуального эффекта сцены.
 *
 * <p>Эффект включается и живёт до явной команды {@link StopEffectCommand}.
 * Без параметров — всё задаётся внутри реализации эффекта.
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="effect" effect="blink"/&gt;
 *   &lt;command type="effect" effect="stopEffect" target="blink"/&gt;
 * </pre>
 */
public class VisualEffectCommand implements AnimationCommand {

    private static final long serialVersionUID = 1L;

    private final String effectType;

    /**
     * Создаёт команду визуального эффекта.
     *
     * @param effectType идентификатор типа эффекта (blink, zoom, colorFilter и др.)
     */
    public VisualEffectCommand(String effectType) {
        this.effectType = effectType;
    }

    /**
     * Возвращает тип команды для системы анимаций.
     *
     * @return всегда {@code "effect"}
     */
    @Override
    public String getType() {
        return "effect";
    }

    /**
     * Возвращает идентификатор визуального эффекта.
     *
     * @return тип эффекта (blink, zoom, colorFilter и др.)
     */
    public String getEffectType() {
        return effectType;
    }
}

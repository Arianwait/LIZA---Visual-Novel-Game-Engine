package kz.aws.game.engine.model;

/**
 * Команда остановки визуального эффекта.
 *
 * <p>Позволяет явно выключить активный эффект (например, снять grayscale).
 * Если {@code targetEffect} пустой — останавливаются все активные эффекты.
 *
 * <p>XML:
 * <pre>
 *   &lt;!-- Остановить конкретный эффект --&gt;
 *   &lt;command type="effect" effect="stopEffect" target="blink"/&gt;
 *
 *   &lt;!-- Остановить все эффекты --&gt;
 *   &lt;command type="effect" effect="clearEffects"/&gt;
 * </pre>
 */
public class StopEffectCommand extends VisualEffectCommand {

    private static final long serialVersionUID = 1L;

    private final String targetEffect;

    /**
     * Создаёт команду остановки эффекта.
     *
     * @param targetEffect id эффекта для остановки (пустая строка = все эффекты)
     */
    public StopEffectCommand(String targetEffect) {
        super("stopEffect");
        this.targetEffect = targetEffect != null ? targetEffect : "";
    }

    /**
     * Возвращает id эффекта для остановки.
     *
     * @return id эффекта или пустая строка (остановить все)
     */
    public String getTargetEffect() {
        return targetEffect;
    }

    /**
     * Проверяет, нужно ли остановить все эффекты.
     *
     * @return {@code true} если команда останавливает все эффекты
     */
    public boolean isClearAll() {
        return targetEffect.isEmpty();
    }
}

package kz.aws.game.engine.model;

/**
 * Команда эффекта цветового фильтра (чёрно-белый, сепия и др.).
 *
 * <p>Тип фильтра задаётся через {@code filter} атрибут в XML.
 *
 * <p>XML:
 * <pre>
 *   &lt;command type="effect" effect="grayscale"/&gt;
 *   &lt;command type="effect" effect="sepia"/&gt;
 * </pre>
 */
public class ColorFilterEffectCommand extends VisualEffectCommand {

    private static final long serialVersionUID = 1L;

    /**
     * Создаёт команду цветового фильтра.
     *
     * @param filterName имя фильтра (grayscale, sepia)
     */
    public ColorFilterEffectCommand(String filterName) {
        super(filterName);
    }
}

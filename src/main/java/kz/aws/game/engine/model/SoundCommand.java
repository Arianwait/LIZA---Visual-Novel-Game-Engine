package kz.aws.game.engine.model;

/**
 * Команда воспроизведения разового звукового эффекта из сценария.
 *
 * <p>Разбирается {@code SceneXmlParser} из legacy-команды
 * {@code Scene:SoundEffect:путь/к/звуку.mp3}. В новом синтаксисе для этого
 * есть атрибут кадра {@code sound="..."}.
 */
public class SoundCommand implements AnimationCommand {

    private static final long serialVersionUID = 1L;

    private final String soundPath;

    /**
     * Создаёт команду воспроизведения звука.
     *
     * @param soundPath путь к аудиофайлу
     */
    public SoundCommand(String soundPath) {
        this.soundPath = soundPath != null ? soundPath : "";
    }

    @Override
    public String getType() {
        return "sound";
    }

    /** Путь к аудиофайлу эффекта. */
    public String getSoundPath() {
        return soundPath;
    }
}

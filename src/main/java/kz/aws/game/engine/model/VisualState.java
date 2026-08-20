package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Визуальное состояние кадра: фон, музыка и персонажи на сцене.
 */
public class VisualState implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private String backgroundPath;
    private String musicPath;
    private Map<String, CharacterState> characters;

    public VisualState() {
        this.characters = new HashMap<>();
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public Map<String, CharacterState> getCharacters() {
        return characters;
    }

    public void setCharacters(Map<String, CharacterState> characters) {
        this.characters = characters;
    }
    
    public CharacterState getCharacter(String name) {
        return characters.computeIfAbsent(name, CharacterState::new);
    }

    public void updateCharacter(String name, CharacterState state) {
        characters.put(name, state);
    }

    /**
     * Собирает runtime-состояние на границе кадров/сцен: значения script-кадра
     * приоритетны; незаданные фон/музыка и отсутствующие в script видимые
     * персонажи наследуются из previous. Работает только на клонах —
     * не мутирует ни script, ни previous.
     *
     * @param script   состояние из XML-кадра (null допустим)
     * @param previous предыдущее runtime-состояние (null допустим)
     * @return новое объединённое состояние
     */
    public static VisualState merge(VisualState script, VisualState previous) {
        VisualState out = script != null ? script.clone() : new VisualState();
        if (previous == null) return out;

        if (isBlank(out.backgroundPath)) out.backgroundPath = previous.backgroundPath;
        if (isBlank(out.musicPath)) out.musicPath = previous.musicPath;

        for (Map.Entry<String, CharacterState> e : previous.characters.entrySet()) {
            if (e.getValue().isVisible() && !out.characters.containsKey(e.getKey())) {
                out.characters.put(e.getKey(), e.getValue().clone());
            }
        }
        return out;
    }

    /**
     * Проверяет, что строка не задана.
     *
     * @param s проверяемая строка
     * @return true — null или пустая
     */
    private static boolean isBlank(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VisualState other)) return false;
        return Objects.equals(backgroundPath, other.backgroundPath)
                && Objects.equals(musicPath, other.musicPath)
                && characters.equals(other.characters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backgroundPath, musicPath, characters);
    }

    @Override
    public VisualState clone() {
        try {
            VisualState cloned = (VisualState) super.clone();
            cloned.characters = new HashMap<>();
            for (Map.Entry<String, CharacterState> entry : this.characters.entrySet()) {
                cloned.characters.put(entry.getKey(), entry.getValue().clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            // Fallback manually if clone fails
            VisualState manual = new VisualState();
            manual.backgroundPath = this.backgroundPath;
            manual.musicPath = this.musicPath;
            for (Map.Entry<String, CharacterState> entry : this.characters.entrySet()) {
                manual.characters.put(entry.getKey(), entry.getValue().clone());
            }
            return manual;
        }
    }
}

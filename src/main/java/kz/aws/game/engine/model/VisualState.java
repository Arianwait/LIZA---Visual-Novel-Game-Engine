package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Состояние персонажа на сцене: поза, позиция и видимость.
 */
public class CharacterState implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public enum Position {
        LEFT, CENTER, RIGHT, OUT
    }

    private String name;
    private String pose;
    private Position position;
    private boolean isVisible;

    public CharacterState(String name) {
        this.name = name;
        this.position = Position.OUT;
        this.isVisible = false;
        this.pose = "default";
    }

    public CharacterState(String name, String pose, Position position, boolean isVisible) {
        this.name = name;
        this.pose = pose;
        this.position = position;
        this.isVisible = isVisible;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPose() { return pose; }
    public void setPose(String pose) { this.pose = pose; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean visible) { isVisible = visible; }

    @Override
    public CharacterState clone() {
        try {
            return (CharacterState) super.clone();
        } catch (CloneNotSupportedException e) {
            return new CharacterState(name, pose, position, isVisible);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CharacterState other)) return false;
        return isVisible == other.isVisible
                && Objects.equals(name, other.name)
                && Objects.equals(pose, other.pose)
                && position == other.position;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pose, position, isVisible);
    }

    @Override
    public String toString() {
        return "CharacterState{name='" + name + "', pose='" + pose + "', pos=" + position + ", visible=" + isVisible + "}";
    }
}

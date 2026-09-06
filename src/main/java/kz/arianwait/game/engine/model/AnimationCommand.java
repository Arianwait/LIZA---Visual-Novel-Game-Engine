package kz.arianwait.game.engine.model;

import java.io.Serializable;

// Marker interface or abstract base class for animations
public interface AnimationCommand extends Serializable {
    String getType();
}

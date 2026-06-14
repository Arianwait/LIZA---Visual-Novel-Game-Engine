package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Lightweight history step — stores route (sceneId + frameIndex)
 * and mutable state snapshots only when they changed (null = no change).
 */
public class HistoryStep implements Serializable {
    private static final long serialVersionUID = 4L;

    private int sceneId;
    private int frameIndex;
    /** null = no change since previous step; non-null = snapshot at this point. */
    private Map<String, Object> gameVariables;
    /** null = no change since previous step; non-null = snapshot at this point. */
    private Map<String, String> playerChoicesSnapshot;

    public HistoryStep(int sceneId, int frameIndex, Map<String, Object> gameVariables, Map<String, String> playerChoices) {
        this.sceneId = sceneId;
        this.frameIndex = frameIndex;
        this.gameVariables = gameVariables;
        this.playerChoicesSnapshot = playerChoices;
    }

    public int getSceneId() { return sceneId; }
    public int getFrameIndex() { return frameIndex; }
    /** May return null if state didn't change at this step. */
    public Map<String, Object> getGameVariables() { return gameVariables; }
    /** May return null if choices didn't change at this step. */
    public Map<String, String> getPlayerChoicesSnapshot() { return playerChoicesSnapshot; }
}

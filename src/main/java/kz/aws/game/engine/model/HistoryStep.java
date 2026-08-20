package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Lightweight history step — stores route (sceneId + frameIndex)
 * and mutable state snapshots only when they changed (null = no change).
 */
public class HistoryStep implements Serializable {
    // 4L сохранён намеренно: добавление полей не ломает чтение старых сейвов
    // (новые поля читаются как null), смена UID — ломает.
    private static final long serialVersionUID = 4L;

    private int sceneId;
    private int frameIndex;
    /** null = no change since previous step; non-null = snapshot at this point. */
    private Map<String, Object> gameVariables;
    /** null = no change since previous step; non-null = snapshot at this point. */
    private Map<String, String> playerChoicesSnapshot;
    /** null = no change since previous step; non-null = runtime visual snapshot. */
    private VisualState visualSnapshot;
    /** null = no change since previous step; non-null = SceneInfo.choiceList snapshot. */
    private List<String> choiceListSnapshot;
    /** null = no change since previous step; non-null = SceneController flags snapshot. */
    private Map<String, Boolean> gameFlagsSnapshot;
    /** null = no change since previous step; non-null = SceneController reputation snapshot. */
    private Map<String, Integer> reputationSnapshot;

    /**
     * Создаёт шаг истории. Любой снимок может быть null — это значит
     * «с прошлого шага не менялось».
     *
     * @param sceneId    id сцены
     * @param frameIndex индекс кадра в сцене
     */
    public HistoryStep(int sceneId, int frameIndex) {
        this.sceneId = sceneId;
        this.frameIndex = frameIndex;
    }

    public int getSceneId() { return sceneId; }
    public int getFrameIndex() { return frameIndex; }

    /** May return null if state didn't change at this step. */
    public Map<String, Object> getGameVariables() { return gameVariables; }
    public void setGameVariables(Map<String, Object> gameVariables) {
        this.gameVariables = gameVariables;
    }

    /** May return null if choices didn't change at this step. */
    public Map<String, String> getPlayerChoicesSnapshot() { return playerChoicesSnapshot; }
    public void setPlayerChoicesSnapshot(Map<String, String> playerChoices) {
        this.playerChoicesSnapshot = playerChoices;
    }

    /** May return null if visual didn't change at this step (or for old saves). */
    public VisualState getVisualSnapshot() { return visualSnapshot; }
    public void setVisualSnapshot(VisualState visualSnapshot) {
        this.visualSnapshot = visualSnapshot;
    }

    /** May return null if the choice list didn't change (or for old saves). */
    public List<String> getChoiceListSnapshot() { return choiceListSnapshot; }
    public void setChoiceListSnapshot(List<String> choiceListSnapshot) {
        this.choiceListSnapshot = choiceListSnapshot;
    }

    /** May return null if flags didn't change (or for old saves). */
    public Map<String, Boolean> getGameFlagsSnapshot() { return gameFlagsSnapshot; }
    public void setGameFlagsSnapshot(Map<String, Boolean> gameFlagsSnapshot) {
        this.gameFlagsSnapshot = gameFlagsSnapshot;
    }

    /** May return null if reputation didn't change (or for old saves). */
    public Map<String, Integer> getReputationSnapshot() { return reputationSnapshot; }
    public void setReputationSnapshot(Map<String, Integer> reputationSnapshot) {
        this.reputationSnapshot = reputationSnapshot;
    }
}

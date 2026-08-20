package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Lightweight history step — stores route (sceneId + frameIndex)
 * and mutable state snapshots only when they changed (null = no change).
 */
public class HistoryStep implements Serializable {
    // 4L сохранён намеренно: добавление поля не ломает чтение старых сейвов
    // (visualSnapshot читается как null), смена UID — ломает.
    private static final long serialVersionUID = 4L;

    private int sceneId;
    private int frameIndex;
    /** null = no change since previous step; non-null = snapshot at this point. */
    private Map<String, Object> gameVariables;
    /** null = no change since previous step; non-null = snapshot at this point. */
    private Map<String, String> playerChoicesSnapshot;
    /** null = no change since previous step; non-null = runtime visual snapshot. */
    private VisualState visualSnapshot;

    /**
     * Создаёт шаг истории.
     *
     * @param sceneId        id сцены
     * @param frameIndex     индекс кадра в сцене
     * @param gameVariables  снимок переменных (null — без изменений)
     * @param playerChoices  снимок выборов (null — без изменений)
     * @param visualSnapshot снимок runtime-визуала (null — без изменений)
     */
    public HistoryStep(int sceneId, int frameIndex, Map<String, Object> gameVariables,
                       Map<String, String> playerChoices, VisualState visualSnapshot) {
        this.sceneId = sceneId;
        this.frameIndex = frameIndex;
        this.gameVariables = gameVariables;
        this.playerChoicesSnapshot = playerChoices;
        this.visualSnapshot = visualSnapshot;
    }

    public int getSceneId() { return sceneId; }
    public int getFrameIndex() { return frameIndex; }
    /** May return null if state didn't change at this step. */
    public Map<String, Object> getGameVariables() { return gameVariables; }
    /** May return null if choices didn't change at this step. */
    public Map<String, String> getPlayerChoicesSnapshot() { return playerChoicesSnapshot; }
    /** May return null if visual didn't change at this step (or for old saves). */
    public VisualState getVisualSnapshot() { return visualSnapshot; }
}

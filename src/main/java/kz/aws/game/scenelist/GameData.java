package kz.aws.game.scenelist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.aws.game.engine.model.HistoryStep;

/**
 * Сериализуемое состояние игры для файла сохранения: позиция в сценарии,
 * история, переменные, глобальное состояние (флаги, репутация, улики) и тема UI.
 */
public class GameData implements Serializable {
    // 2L сохранён намеренно: новые поля читаются из старых сейвов как null
    // и подставляются пустыми; смена UID сломала бы совместимость.
    private static final long serialVersionUID = 2L;

    // Legacy fields (kept for compatibility or removal later)
    private List<String> choiceList;
    private int clicker = 0;

    // New Engine Fields
    private int currentSceneId;
    private int currentFrameIndex;
    private List<HistoryStep> history;
    private Map<String, Object> gameVariables;
    /** Словарь подстановок: {ключ} в тексте/имени заменяется на значение (имя игрока и др.). Сохраняется в сейв. */
    private Map<String, String> playerVariables;
    /** Тема интерфейса при сохранении (hitech, classic, walk). При загрузке восстанавливается. */
    private String uiTheme;
    /** Флаги прогресса из SceneController. null — сейв старого формата. */
    private Map<String, Boolean> gameFlags;
    /** Репутация персонажей из SceneController. null — сейв старого формата. */
    private Map<String, Integer> characterReputation;
    /** Собранные улики и данные паззлов из SceneController. null — сейв старого формата. */
    private Map<String, String> playerChoices;

    public GameData() {
        choiceList = new ArrayList<>();
        clicker = 0;
        history = new ArrayList<>();
        gameVariables = new HashMap<>();
        playerVariables = new HashMap<>();
        gameFlags = new HashMap<>();
        characterReputation = new HashMap<>();
        playerChoices = new HashMap<>();
    }

    public void setChoice(List<String> choice) {
    	choiceList = choice;
    }

    public List<String> getChoiceList() {
        return new ArrayList<>(choiceList); 
    }

    public void setClicker(int clicker) {
        this.clicker = clicker;
    }

    public int getClicker() {
        return clicker;
    }
    
    // New Getters/Setters
    public int getCurrentSceneId() { return currentSceneId; }
    public void setCurrentSceneId(int currentSceneId) { this.currentSceneId = currentSceneId; }

    public int getCurrentFrameIndex() { return currentFrameIndex; }
    public void setCurrentFrameIndex(int currentFrameIndex) { this.currentFrameIndex = currentFrameIndex; }

    public List<HistoryStep> getHistory() { return history; }
    public void setHistory(List<HistoryStep> history) { this.history = history; }

    public Map<String, Object> getGameVariables() { return gameVariables; }
    public void setGameVariables(Map<String, Object> gameVariables) { this.gameVariables = gameVariables; }

    public Map<String, String> getPlayerVariables() { return playerVariables; }
    public void setPlayerVariables(Map<String, String> playerVariables) { this.playerVariables = playerVariables; }

    public String getUiTheme() { return uiTheme; }
    public void setUiTheme(String uiTheme) { this.uiTheme = uiTheme; }

    /** May return null for saves created before flags were persisted. */
    public Map<String, Boolean> getGameFlags() { return gameFlags; }
    public void setGameFlags(Map<String, Boolean> gameFlags) { this.gameFlags = gameFlags; }

    /** May return null for saves created before reputation was persisted. */
    public Map<String, Integer> getCharacterReputation() { return characterReputation; }
    public void setCharacterReputation(Map<String, Integer> characterReputation) {
        this.characterReputation = characterReputation;
    }

    /** May return null for saves created before clues were persisted. */
    public Map<String, String> getPlayerChoices() { return playerChoices; }
    public void setPlayerChoices(Map<String, String> playerChoices) { this.playerChoices = playerChoices; }
}

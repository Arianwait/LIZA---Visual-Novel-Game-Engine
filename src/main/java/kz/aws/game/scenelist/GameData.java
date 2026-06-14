package kz.aws.game.scenelist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kz.aws.game.engine.model.HistoryStep;

public class GameData implements Serializable {
    private static final long serialVersionUID = 2L; // Updated version
    
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

    public GameData() {
        choiceList = new ArrayList<>();
        clicker = 0;
        history = new ArrayList<>();
        gameVariables = new HashMap<>();
        playerVariables = new HashMap<>();
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
}

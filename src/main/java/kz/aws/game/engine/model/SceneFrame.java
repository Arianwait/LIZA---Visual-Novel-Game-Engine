package kz.aws.game.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SceneFrame implements Serializable {
    private static final long serialVersionUID = 1L;

    private String text;
    private String speakerName;
    private String speakerColor; // Hex or name
    private String textStyle; // CSS style class name
    private String voicePath; // Path to voiceover audio file
    /** Текст для полноэкранного затемнения (DimOverlay); null — без overlay. */
    private String overlayText;
    /** Ключ переменной для запроса ввода (имя игрока и др.); null — без запроса. */
    private String inputRequestKey;
    /** Подсказка для диалога ввода (например, "Введите имя:"). */
    private String inputRequestPrompt;
    /** При показе кадра переключить тему интерфейса (hitech, classic, walk). null — не менять. */
    private String switchToTheme;
    private VisualState visualState;

    public String getTextStyle() { return textStyle; }
    public void setTextStyle(String textStyle) { this.textStyle = textStyle; }
    
    public String getVoicePath() { return voicePath; }
    public void setVoicePath(String voicePath) { this.voicePath = voicePath; }
    
    private List<ClueData> clues;
    private List<ChoiceOption> choices;
    private List<AnimationCommand> entryAnimations;
    private int nextSceneId = -1; // -1 means no next scene or end of game

    public SceneFrame(String speakerName, String text, VisualState visualState) {
        this.speakerName = speakerName;
        this.text = text;
        this.visualState = visualState;
        this.entryAnimations = new ArrayList<>();
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
    
    public String getSpeakerColor() { return speakerColor; }
    public void setSpeakerColor(String speakerColor) { this.speakerColor = speakerColor; }

    public VisualState getVisualState() { return visualState; }
    public void setVisualState(VisualState visualState) { this.visualState = visualState; }

    public List<AnimationCommand> getEntryAnimations() { return entryAnimations; }
    public void setEntryAnimations(List<AnimationCommand> entryAnimations) { this.entryAnimations = entryAnimations; }
    
    public void addAnimation(AnimationCommand animation) {
        this.entryAnimations.add(animation);
    }

    public int getNextSceneId() { return nextSceneId; }
    public void setNextSceneId(int nextSceneId) { this.nextSceneId = nextSceneId; }

    public String getOverlayText() { return overlayText; }
    public void setOverlayText(String overlayText) { this.overlayText = overlayText; }

    public String getInputRequestKey() { return inputRequestKey; }
    public void setInputRequestKey(String inputRequestKey) { this.inputRequestKey = inputRequestKey; }
    public String getInputRequestPrompt() { return inputRequestPrompt; }
    public void setInputRequestPrompt(String inputRequestPrompt) { this.inputRequestPrompt = inputRequestPrompt; }

    public String getSwitchToTheme() { return switchToTheme; }
    public void setSwitchToTheme(String switchToTheme) { this.switchToTheme = switchToTheme; }

    public List<ClueData> getClues() { return clues; }
    public void setClues(List<ClueData> clues) { this.clues = clues; }
    public boolean hasClues() { return clues != null && !clues.isEmpty(); }

    public List<ChoiceOption> getChoices() { return choices; }
    public void setChoices(List<ChoiceOption> choices) { this.choices = choices; }
    public boolean hasChoices() { return choices != null && !choices.isEmpty(); }
}

package kz.aws.game.engine.model;

import java.io.Serializable;

/**
 * Один вариант выбора в диалоге: текст, id целевой сцены и условия доступности.
 */
public class ChoiceOption implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String text;
    private final int targetSceneId;
    private final String addChoice;
    private final String removeChoice;
    private final String characterChoice;
    private final int minRep;
    private final int maxRep;

    /**
     * @param text            текст варианта
     * @param targetSceneId   id сцены для перехода
     * @param addChoice       требуемые выборы (через ;)
     * @param removeChoice    запрещённые выборы (через ;)
     * @param characterChoice имя персонажа для проверки репутации
     * @param minRep          минимальная репутация
     * @param maxRep          максимальная репутация
     */
    public ChoiceOption(String text, int targetSceneId, String addChoice,
                        String removeChoice, String characterChoice, int minRep, int maxRep) {
        this.text = text;
        this.targetSceneId = targetSceneId;
        this.addChoice = addChoice != null ? addChoice : "";
        this.removeChoice = removeChoice != null ? removeChoice : "";
        this.characterChoice = characterChoice != null ? characterChoice : "";
        this.minRep = minRep;
        this.maxRep = maxRep;
    }

    public String getText() { return text; }
    public int getTargetSceneId() { return targetSceneId; }
    public String getAddChoice() { return addChoice; }
    public String getRemoveChoice() { return removeChoice; }
    public String getCharacterChoice() { return characterChoice; }
    public int getMinRep() { return minRep; }
    public int getMaxRep() { return maxRep; }
}

package kz.aws.game.engine.model;

import java.io.Serializable;

/**
 * Данные кликабельной улики внутри реплики персонажа.
 *
 * <p>Парсится из XML:
 * <pre>
 *   &lt;clue key="evidence_3" value="Маркус видел разбитое окно"&gt;разбитое окно&lt;/clue&gt;
 * </pre>
 */
public class ClueData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String key;
    private final String value;
    private final String displayText;

    public ClueData(String key, String value, String displayText) {
        this.key = key;
        this.value = value;
        this.displayText = displayText;
    }

    /** Ключ для {@code SceneController.setPlayerChoice(key, value)}. */
    public String getKey() { return key; }

    /** Описание улики (сохраняется в playerChoices). */
    public String getValue() { return value; }

    /** Отображаемый текст (часть реплики, на которую можно кликнуть). */
    public String getDisplayText() { return displayText; }
}

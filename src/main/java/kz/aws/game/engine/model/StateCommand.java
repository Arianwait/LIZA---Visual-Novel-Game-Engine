package kz.aws.game.engine.model;

/**
 * Команда изменения игрового состояния из сценария: установка флага
 * и изменение репутации персонажа.
 *
 * <p>Разбирается {@code SceneXmlParser} из команд вида:
 * <pre>
 *   &lt;command type="character" action="SetFlag" target="knows_fact" value="true"/&gt;
 *   &lt;command type="character" action="ReduceReputathion" target="Маркус" value="10"/&gt;
 * </pre>
 */
public class StateCommand implements AnimationCommand {

    private static final long serialVersionUID = 1L;

    /** Вид изменения состояния. */
    public enum Kind {
        /** Установить флаг прогресса. */
        SET_FLAG,
        /** Задать репутацию персонажа абсолютным значением. */
        SET_REPUTATION,
        /** Увеличить репутацию персонажа. */
        ADD_REPUTATION,
        /** Уменьшить репутацию персонажа. */
        REDUCE_REPUTATION,
        /** Записать выбор игрока (улику, ответ) в playerChoices. */
        SET_CHOICE
    }

    private final Kind kind;
    private final String target;
    private final String value;

    /**
     * Создаёт команду изменения состояния.
     *
     * @param kind   вид изменения
     * @param target имя флага или персонажа
     * @param value  значение (true/false для флага, число для репутации)
     */
    public StateCommand(Kind kind, String target, String value) {
        this.kind = kind;
        this.target = target != null ? target : "";
        this.value = value != null ? value : "";
    }

    @Override
    public String getType() {
        return "state";
    }

    public Kind getKind() { return kind; }
    public String getTarget() { return target; }
    public String getValue() { return value; }
}

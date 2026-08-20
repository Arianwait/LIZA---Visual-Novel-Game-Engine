package kz.aws.game.gameaction;

import kz.aws.game.scenelist.SceneController;

/**
 * Команда установки репутации персонажа (target=имя персонажа, value=число или через parameters).
 */
@GameAction("SetReputation")
public final class SetReputationGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] args = ctx.getParameters();
        if (args.length >= 2) {
            applyReputation(args[0], args[1]);
        } else if (ctx.getTarget() != null && ctx.getValue() != null && !ctx.getValue().isEmpty()) {
            applyReputation(ctx.getTarget(), ctx.getValue());
        } else {
            System.err.println("Недостаточно аргументов для команды SetReputation");
        }
    }

    /**
     * Устанавливает репутацию персонажа, игнорируя нечисловое значение.
     *
     * @param characterName имя персонажа
     * @param rawValue      значение репутации из сценария
     */
    private void applyReputation(String characterName, String rawValue) {
        try {
            SceneController.setCharacterReputation(characterName, Integer.parseInt(rawValue.trim()));
        } catch (NumberFormatException e) {
            System.err.println("SetReputation: некорректное значение \"" + rawValue
                    + "\" для персонажа " + characterName);
        }
    }
}

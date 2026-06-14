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
            String characterName = args[0];
            int reputation = Integer.parseInt(args[1]);
            SceneController.setCharacterReputation(characterName, reputation);
        } else if (ctx.getTarget() != null && ctx.getValue() != null && !ctx.getValue().isEmpty()) {
            SceneController.setCharacterReputation(ctx.getTarget(), Integer.parseInt(ctx.getValue()));
        } else {
            System.err.println("Недостаточно аргументов для команды SetReputation");
        }
    }
}

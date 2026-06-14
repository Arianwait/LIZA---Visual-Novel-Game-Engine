package kz.aws.game.gameaction;

import kz.aws.game.scenelist.SceneController;

/**
 * Команда установки выбора игрока (parameters: choiceId, choiceValue).
 */
@GameAction("SetChoice")
public final class SetChoiceGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] args = ctx.getParameters();
        if (args.length >= 2) {
            String choiceId = args[0];
            String choiceValue = args[1];
            SceneController.setPlayerChoice(choiceId, choiceValue);
        } else {
            System.err.println("Недостаточно аргументов для команды SetChoice");
        }
    }
}

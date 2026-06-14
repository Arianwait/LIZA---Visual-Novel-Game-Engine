package kz.aws.game.gameaction;

import kz.aws.game.scenelist.SceneController;

/**
 * Команда установки флага игры (target=имя флага, value=true/false или через parameters).
 */
@GameAction("SetFlag")
public final class SetFlagGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] args = ctx.getParameters();
        if (args.length >= 2) {
            String flagName = args[0];
            boolean flagValue = Boolean.parseBoolean(args[1]);
            SceneController.setFlag(flagName, flagValue);
        } else if (ctx.getTarget() != null && !ctx.getTarget().isEmpty() && ctx.getValue() != null && !ctx.getValue().isEmpty()) {
            SceneController.setFlag(ctx.getTarget(), Boolean.parseBoolean(ctx.getValue()));
        } else {
            System.err.println("Недостаточно аргументов для команды SetFlag");
        }
    }
}

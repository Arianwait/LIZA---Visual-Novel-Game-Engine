package kz.aws.game.gameaction;

import kz.aws.game.character.ICharacter;

/**
 * Команда уменьшения репутации персонажа.
 */
@GameAction("ReduceReputathion")
public final class ReduceReputationGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        if (character == null || ctx.getParameters().length == 0) return;
        try {
            int sub = Integer.parseInt(ctx.getParameters()[0]);
            character.setReputathion(character.getReputathion() - sub);
        } catch (NumberFormatException e) {
            // ignore
        }
    }
}

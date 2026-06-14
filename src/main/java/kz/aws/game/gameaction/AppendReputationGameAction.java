package kz.aws.game.gameaction;

import kz.aws.game.character.ICharacter;

/**
 * Команда увеличения репутации персонажа.
 */
@GameAction("AppendReputathion")
public final class AppendReputationGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        if (character == null || ctx.getParameters().length == 0) return;
        try {
            int add = Integer.parseInt(ctx.getParameters()[0]);
            character.setReputathion(character.getReputathion() + add);
        } catch (NumberFormatException e) {
            // ignore
        }
    }
}

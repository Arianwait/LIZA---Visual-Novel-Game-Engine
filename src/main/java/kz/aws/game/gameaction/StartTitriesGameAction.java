package kz.aws.game.gameaction;

import kz.aws.game.mainscene.TitresAnimation;

/**
 * Команда запуска титров.
 */
@GameAction("StartTitries")
public final class StartTitriesGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        if (ctx.getAppSettings() != null) {
            TitresAnimation titresAnimation = new TitresAnimation();
            titresAnimation.start(ctx.getAppSettings());
        }
    }
}

package kz.aws.game.gameaction;

import kz.aws.game.soundtrack.SoundEffect;

/**
 * Команда воспроизведения звукового эффекта.
 */
@GameAction("SoundEffect")
public final class SoundEffectGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] params = ctx.getParameters();
        if (params.length > 0 && ctx.getAppSettings() != null) {
            SoundEffect.startSound(ctx.getAppSettings(), params[0]).play();
        }
    }
}

package kz.aws.game.gameaction;

import javafx.scene.media.MediaPlayer;
import kz.aws.game.soundtrack.SoundEffect;

/**
 * Команда воспроизведения звукового эффекта.
 */
@GameAction("SoundEffect")
public final class SoundEffectGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] params = ctx.getParameters();
        if (params.length == 0 || ctx.getAppSettings() == null) return;

        MediaPlayer player = SoundEffect.startSound(ctx.getAppSettings(), params[0]);
        if (player != null) {
            player.play();
        }
    }
}

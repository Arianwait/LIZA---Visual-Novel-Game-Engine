package kz.aws.game.gameaction;

import javafx.animation.ScaleTransition;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import kz.aws.game.character.ICharacter;

/**
 * Команда эффекта появления персонажа.
 */
@GameAction("AppearEffect")
public final class AppearEffectGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        ImageView view = character != null ? character.getCharacterImageView() : null;
        if (view == null) return;
        ScaleTransition t = new ScaleTransition(Duration.millis(30), view);
        t.setFromX(0.5);
        t.setToX(1.0);
        t.setFromY(0.5);
        t.setToY(1.0);
        t.play();
    }
}

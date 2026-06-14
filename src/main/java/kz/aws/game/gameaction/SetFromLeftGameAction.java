package kz.aws.game.gameaction;

import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import kz.aws.game.character.ICharacter;

/**
 * Команда установки персонажа слева без анимации движения.
 */
@GameAction("setFromLeft")
public final class SetFromLeftGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        ImageView view = character != null ? character.getCharacterImageView() : null;
        if (view == null || ctx.getPrimaryStage() == null) return;
        TranslateTransition t = new TranslateTransition(Duration.seconds(1), view);
        t.setFromX(-(ctx.getPrimaryStage().getWidth() / 2) * 0.4);
        t.play();
    }
}

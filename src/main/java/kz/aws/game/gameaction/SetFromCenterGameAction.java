package kz.aws.game.gameaction;

import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import kz.aws.game.character.ICharacter;

/**
 * Команда установки персонажа в центр без анимации движения.
 */
@GameAction("setFromCenter")
public final class SetFromCenterGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        ImageView view = character != null ? character.getCharacterImageView() : null;
        if (view == null) return;
        TranslateTransition t = new TranslateTransition(Duration.seconds(1), view);
        t.setFromX(0);
        t.play();
    }
}

package kz.aws.game.gameaction;

import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import kz.aws.game.character.ICharacter;

/**
 * Команда движения персонажа в центр.
 */
@GameAction("move_Center")
public final class MoveCenterGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        ImageView view = character != null ? character.getCharacterImageView() : null;
        if (view == null) return;
        TranslateTransition t = new TranslateTransition(Duration.seconds(1), view);
        t.setToX(0);
        t.play();
    }
}

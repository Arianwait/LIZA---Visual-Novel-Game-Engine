package kz.aws.game.gameaction;

import javafx.scene.image.ImageView;
import kz.aws.game.character.ICharacter;

/**
 * Команда удаления персонажа со сцены.
 */
@GameAction("removeFromScene")
public final class RemoveFromSceneGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        ICharacter character = ctx.findCharacter();
        if (character == null || ctx.getRoot() == null) return;
        ImageView view = character.getCharacterImageView();
        if (view != null) ctx.getRoot().getChildren().remove(view);
    }
}

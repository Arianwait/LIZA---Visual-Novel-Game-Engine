package kz.aws.game.gameaction;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import kz.aws.game.scenedetails.DimOverlay;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Команда показа затемнённого текста с паузой.
 */
@GameAction("OpenDarkText")
public final class OpenDarkTextGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] params = ctx.getParameters();
        if (params.length == 0 || ctx.getRoot() == null) return;
        String text = params[0];
        DimOverlay dimOverlay = new DimOverlay(text, 0.5);
        dimOverlay.showOverlay(ctx.getRoot());

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            dimOverlay.hideOverlay(ctx.getRoot());
            if (SceneInfo.getTableDatail() != null) {
                SceneInfo.getTableDatail().addToScene(ctx.getRoot());
            }
            SceneInfo.enableEventHandler(ctx.getRoot());
        });
        pause.play();

        SceneInfo.disableEventHandler(ctx.getRoot());
        if (SceneInfo.getTableDatail() != null) {
            SceneInfo.getTableDatail().removeFromScene(ctx.getRoot());
        }
    }
}

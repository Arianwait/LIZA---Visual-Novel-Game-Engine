package kz.aws.game.gameaction;

import kz.aws.game.background.BackgroundDisetche;
import kz.aws.game.background.SceneBackground;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Команда смены фона сцены.
 */
@GameAction("changebackground")
public final class ChangeBackgroundGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String[] params = ctx.getParameters();
        if (params.length > 0 && ctx.getAppSettings() != null) {
            String background = params[0];
            if (!background.isEmpty() && !background.equals(SceneInfo.getBackground())) {
                SceneBackground sceneBackground = new BackgroundDisetche();
                SceneInfo.setBackground(background);
                ctx.getAppSettings().getRoot().setBackground(sceneBackground.getBackground(background));
            }
        }
    }
}

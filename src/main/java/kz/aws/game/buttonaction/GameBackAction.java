package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Действие кнопки «Назад» в игровой панели: откат на предыдущий кадр.
 */
@ButtonAction("game-btn-back")
public final class GameBackAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (SceneInfo.getGameEngine() != null) {
            SceneInfo.getGameEngine().back();
        }
    }
}

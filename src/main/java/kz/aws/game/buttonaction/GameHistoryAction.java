package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenedetails.DialogTreePane;
import kz.aws.game.scenelist.GameData;
import kz.aws.game.scenelist.SceneInfo;

/**
 * Действие кнопки «История»: открывает панель истории диалогов.
 */
@ButtonAction("game-btn-history")
public final class GameHistoryAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        int sceneId = 0;
        if (SceneInfo.getGameEngine() != null) {
            GameData data = SceneInfo.getGameEngine().getSaveData();
            if (data != null) {
                sceneId = data.getCurrentSceneId();
            }
        }
        int clicker = SceneInfo.getCliker();
        DialogTreePane dialogTree = new DialogTreePane(appSettings, sceneId, clicker);
        dialogTree.addInScene(appSettings.getRoot());
    }
}

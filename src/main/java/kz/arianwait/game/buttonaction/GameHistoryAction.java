package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.scenedetails.DialogTreePane;
import kz.arianwait.game.scenelist.GameData;
import kz.arianwait.game.scenelist.SceneInfo;

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
        int clicker = SceneInfo.getClicker();
        DialogTreePane dialogTree = new DialogTreePane(appSettings, sceneId, clicker);
        dialogTree.addInScene(appSettings.getRoot());
    }
}

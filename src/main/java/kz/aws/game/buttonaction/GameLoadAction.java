package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenedetails.SaveLoadPanelController;
import kz.aws.game.scenedetails.SaveLoadPanelController.Mode;

/**
 * Действие кнопки «Загрузить игру»: открывает экран загрузки сохранений.
 */
@ButtonAction("game-btn-load")
public final class GameLoadAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        SaveLoadPanelController panel = new SaveLoadPanelController(appSettings, Mode.LOAD, true);
        panel.showPanel(appSettings.getRoot());
    }
}

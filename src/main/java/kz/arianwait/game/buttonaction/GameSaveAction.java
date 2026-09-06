package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.scenedetails.SaveLoadPanelController;
import kz.arianwait.game.scenedetails.SaveLoadPanelController.Mode;

/**
 * Действие кнопки «Сохранить игру»: открывает экран сохранения.
 */
@ButtonAction("game-btn-save")
public final class GameSaveAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        SaveLoadPanelController panel = new SaveLoadPanelController(appSettings, Mode.SAVE, false);
        panel.showPanel(appSettings.getRoot());
    }
}

package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.scenedetails.SaveLoadPanelController;
import kz.arianwait.game.scenedetails.SaveLoadPanelController.Mode;

/**
 * Действие кнопки «Продолжить»: открывает экран загрузки сохранений.
 */
@ButtonAction("menu-btn-continue")
public final class MenuContinueAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        SaveLoadPanelController panel = new SaveLoadPanelController(appSettings, Mode.LOAD, false);
        panel.showPanel(appSettings.getRoot());
    }
}

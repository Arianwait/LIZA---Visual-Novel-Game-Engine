package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.scenedetails.SaveLoadPanelController;
import kz.aws.game.scenedetails.SaveLoadPanelController.Mode;

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

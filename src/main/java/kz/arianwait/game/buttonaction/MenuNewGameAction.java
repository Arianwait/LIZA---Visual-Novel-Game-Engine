package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.panel.PanelRegistry;
import kz.arianwait.game.scenelist.SceneInfo;

/**
 * Действие кнопки «Начать новую игру»: сбрасывает выборы и открывает панель выбора главы по id из Panels.xml.
 */
@ButtonAction("menu-btn-new-game")
public final class MenuNewGameAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        SceneInfo.clearChoices();
        PanelRegistry.show("scene-selection", appSettings);
    }
}

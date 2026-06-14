package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.PanelRegistry;
import kz.aws.game.scenelist.SceneInfo;

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

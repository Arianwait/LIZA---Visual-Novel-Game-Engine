package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.panel.PanelRegistry;

/**
 * Действие кнопки «Кредиты»: открывает окно кредитов (панель credits).
 */
@ButtonAction("menu-btn-credits")
public final class MenuCreditsAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        PanelRegistry.show("credits", appSettings);
    }
}

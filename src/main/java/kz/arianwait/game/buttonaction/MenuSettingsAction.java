package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.panel.PanelRegistry;

/**
 * Действие кнопки «Настройки»: открывает панель настроек по id из Panels.xml (PanelRegistry.show).
 */
@ButtonAction("menu-btn-settings")
public final class MenuSettingsAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        PanelRegistry.show("settings", appSettings);
    }
}

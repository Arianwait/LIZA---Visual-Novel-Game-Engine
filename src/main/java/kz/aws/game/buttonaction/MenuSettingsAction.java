package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.PanelRegistry;

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

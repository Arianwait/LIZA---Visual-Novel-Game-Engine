package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.PanelRegistry;

/**
 * Действие кнопки «Начать новеллу»: показывает панель выбора главы по id из Panels.xml (PanelRegistry.show).
 */
@ButtonAction("menu-btn-start")
public final class MenuStartAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        PanelRegistry.show("scene-selection", appSettings);
    }
}

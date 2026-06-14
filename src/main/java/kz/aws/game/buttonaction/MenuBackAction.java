package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.panel.PanelRegistry;

/**
 * Действие кнопки «Назад» в подменю (настройки, выбор главы): показывает панель main-menu по id из Panels.xml.
 */
@ButtonAction("menu-btn-back")
public final class MenuBackAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        if (appSettings == null) return;
        PanelRegistry.show("main-menu", appSettings);
    }
}

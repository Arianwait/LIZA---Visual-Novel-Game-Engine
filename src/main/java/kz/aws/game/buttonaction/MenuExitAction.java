package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;

/**
 * Действие кнопки «Выход»: завершает приложение.
 */
@ButtonAction("menu-btn-exit")
public final class MenuExitAction implements ButtonActionHandler {

    @Override
    public void run(AppSettings appSettings) {
        System.exit(0);
    }
}

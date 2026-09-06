package kz.arianwait.game.buttonaction;

import kz.arianwait.game.appsettings.AppSettings;

/**
 * Действие, выполняемое при нажатии кнопки.
 * Реализации помечаются аннотацией {@link ButtonAction} и регистрируются через {@link ButtonActionRegistry}.
 */
@FunctionalInterface
public interface ButtonActionHandler {
    void run(AppSettings appSettings);
}

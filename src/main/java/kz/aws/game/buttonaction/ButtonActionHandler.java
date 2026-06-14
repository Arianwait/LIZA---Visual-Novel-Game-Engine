package kz.aws.game.buttonaction;

import kz.aws.game.appsettings.AppSettings;

/**
 * Действие, выполняемое при нажатии кнопки.
 * Реализации помечаются аннотацией {@link ButtonAction} и регистрируются через {@link ButtonActionRegistry}.
 */
@FunctionalInterface
public interface ButtonActionHandler {
    void run(AppSettings appSettings);
}

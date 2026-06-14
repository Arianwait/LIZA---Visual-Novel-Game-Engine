package kz.aws.game.gameaction;

/**
 * Обработчик игровой команды (движение персонажа, показ, фон, звук и т.д.).
 * Реализации помечаются аннотацией {@link GameAction} и регистрируются в {@link GameActionRegistry}.
 */
@FunctionalInterface
public interface GameActionHandler {
    void run(GameActionContext ctx);
}

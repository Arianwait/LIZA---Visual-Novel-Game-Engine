package kz.aws.game.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.gameaction.GameActionContext;
import kz.aws.game.gameaction.GameActionRegistry;
import kz.aws.game.scenedetails.DialogPanel;
import kz.aws.game.scenelist.SceneController;

/**
 * Точка входа для выполнения игровых команд. Парсит строку команды (target:action:param1:param2)
 * и передаёт выполнение в {@link GameActionRegistry}. Обработчики команд — классы с {@link kz.aws.game.gameaction.GameAction} в пакете gameaction (автопоиск).
 */
public class CommandProcessor {

    /**
     * Выполняет команду: парсит строку, строит контекст и вызывает соответствующий обработчик из {@link GameActionRegistry}.
     */
    public static void executeCommand(String commandString, AppSettings appSettings,
            StackPane root, List<ICharacter> characterList,
            Stage primaryStage, DialogPanel tableDatail) {
        if (commandString == null || commandString.trim().isEmpty()) {
            System.err.println("Пустая команда");
            return;
        }

        try {
            Command command = parseCommand(commandString);
            String value = command.getParameters().length > 0 ? command.getParameters()[0] : "";
            GameActionContext ctx = new GameActionContext(
                    command.getCharacter(),
                    value,
                    command.getParameters(),
                    appSettings,
                    root,
                    characterList,
                    primaryStage,
                    tableDatail);

            if (GameActionRegistry.get(command.getAction()) != null) {
                GameActionRegistry.run(command.getAction(), ctx);
            } else {
                System.err.println("Неизвестная команда: " + command.getAction());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка парсинга команды: " + e.getMessage());
        }
    }

    /**
     * Парсит строку команды в формате target:action:param1:param2...
     */
    public static Command parseCommand(String commandString) {
        String[] parts = commandString.split(":");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Неверный формат команды: " + commandString);
        }

        String character = parts[0];
        String action = parts[1];
        String[] parameters = parts.length > 2 ? Arrays.copyOfRange(parts, 2, parts.length) : new String[0];

        return new Command(character, action, parameters);
    }

    /**
     * Возвращает статистику игры (флаги, репутация, выборы) для отладки или сохранений.
     */
    public static Map<String, Object> getGameStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("flags", SceneController.saveGameData().get("flags"));
        stats.put("reputation", SceneController.saveGameData().get("reputation"));
        stats.put("choices", SceneController.saveGameData().get("choices"));
        return stats;
    }
}

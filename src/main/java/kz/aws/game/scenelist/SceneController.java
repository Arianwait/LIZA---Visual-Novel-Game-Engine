package kz.aws.game.scenelist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.scenedetails.DialogPanel;

/**
 * Контроллер отображения одного кадра диалоговой сцены (legacy-путь: имя, текст,
 * команды). Также хранит глобальное состояние игры: флаги, репутацию и выборы игрока.
 */
public class SceneController {

	// Система флагов для отслеживания прогресса
	private static final Map<String, Boolean> gameFlags = new HashMap<>();

	// Система репутации персонажей
	private static final Map<String, Integer> characterReputation = new HashMap<>();

	// Система выборов игрока
	private static final Map<String, String> playerChoices = new HashMap<>();
	/**
	 * Запускает отображение одного кадра диалога: имя, текст, варианты выбора и команды.
	 *
	 * @param tableDatail   панель диалога
	 * @param characterList список элементов персонажей из XML
	 * @param appSettings   настройки приложения
	 * @param ICharacterList список персонажей
	 * @param root          корневой StackPane
	 * @param Clicker       индекс текущего элемента
	 * @param primaryStage  главное окно
	 */
	public void startScene(DialogPanel tableDatail, NodeList characterList, AppSettings appSettings,
			List<ICharacter> ICharacterList, StackPane root, int Clicker, Stage primaryStage) {
		Element characterElement = (Element) characterList.item(Clicker);
		String player = System.getProperty("user.name");

		displayDialogText(tableDatail, characterElement, player);
		executeCommands(characterElement, appSettings, root, ICharacterList, primaryStage, tableDatail);
	}

	/**
	 * Отображает имя персонажа и текст диалога на панели.
	 *
	 * @param tableDatail      панель диалога
	 * @param characterElement XML-элемент персонажа
	 * @param player           имя игрока (для подстановки [player])
	 */
	private void displayDialogText(DialogPanel tableDatail, Element characterElement, String player) {
		String playerName = resolvePlayerName(characterElement.getAttribute("name"), player);
		tableDatail.changeNameText(playerName);
		tableDatail.resetNameColor();

		String color = characterElement.getAttribute("color");
		String dialogText = resolvePlayerName(characterElement.getFirstChild().getNodeValue(), player);
		tableDatail.changeDialogText(dialogText, true);
		DialogList.addDialog(playerName, color, dialogText, color);
	}

	/**
	 * Заменяет [player] на имя системного пользователя.
	 *
	 * @param text   исходный текст
	 * @param player имя игрока
	 * @return текст с подставленным именем
	 */
	private String resolvePlayerName(String text, String player) {
		if (text.contains("[player]")) {
			return text.replace("[player]", player);
		}
		return text;
	}

	/**
	 * Выполняет все команды из XML-элемента персонажа.
	 * Поддерживает оба формата: атрибуты (структурированный) и текст (старый).
	 *
	 * @param characterElement XML-элемент персонажа
	 * @param appSettings      настройки
	 * @param root             корневой StackPane
	 * @param ICharacterList   список персонажей
	 * @param primaryStage     главное окно
	 * @param tableDatail      панель диалога
	 */
	private void executeCommands(Element characterElement, AppSettings appSettings,
			StackPane root, List<ICharacter> ICharacterList, Stage primaryStage, DialogPanel tableDatail) {
		NodeList commandList = characterElement.getElementsByTagName("command");
		for (int j = 0; j < commandList.getLength(); j++) {
			executeOneCommand((Element) commandList.item(j), appSettings, root, ICharacterList, primaryStage, tableDatail);
		}
	}

	/**
	 * Выполняет одну команду из XML. Поддерживает атрибутный и текстовый формат.
	 *
	 * @param commandElement XML-элемент command
	 * @param appSettings    настройки
	 * @param root           корневой StackPane
	 * @param ICharacterList список персонажей
	 * @param primaryStage   главное окно
	 * @param tableDatail    панель диалога
	 */
	private void executeOneCommand(Element commandElement, AppSettings appSettings,
			StackPane root, List<ICharacter> ICharacterList, Stage primaryStage, DialogPanel tableDatail) {
		String action = commandElement.getAttribute("action");
		if (action != null && !action.isEmpty()) {
			String target = commandElement.getAttribute("target");
			String value = commandElement.getAttribute("value");
			String command = buildStructuredCommand(target, action, value);
			SceneCommand.setActions(command, appSettings, root, ICharacterList, primaryStage, tableDatail);
		} else {
			String command = commandElement.getTextContent();
			if (command != null && !command.trim().isEmpty()) {
				SceneCommand.setActions(command.trim(), appSettings, root, ICharacterList, primaryStage, tableDatail);
			}
		}
	}

	/**
	 * Собирает строку команды из атрибутов target, action, value.
	 *
	 * @param target цель команды (может быть пустой)
	 * @param action действие
	 * @param value  параметр (может быть пустым)
	 * @return строка команды в формате "target:action:value"
	 */
	private String buildStructuredCommand(String target, String action, String value) {
		boolean hasTarget = target != null && !target.isEmpty();
		boolean hasValue = value != null && !value.isEmpty();
		if (hasTarget) {
			return target + ":" + action + (hasValue ? ":" + value : "");
		}
		return action + (hasValue ? ":" + value : "");
	}

	// ========== НОВЫЕ МЕТОДЫ ДЛЯ УЛУЧШЕННОЙ СИСТЕМЫ ==========

	/**
	 * Установка флага игры
	 */
	public static void setFlag(String flagName, boolean value) {
		gameFlags.put(flagName, value);
	}

	/**
	 * Получение флага игры
	 */
	public static boolean getFlag(String flagName) {
		return gameFlags.getOrDefault(flagName, false);
	}

	/**
	 * Установка репутации персонажа
	 */
	public static void setCharacterReputation(String characterName, int reputation) {
		characterReputation.put(characterName, Math.max(0, Math.min(100, reputation)));
	}

	/**
	 * Получение репутации персонажа
	 */
	public static int getCharacterReputation(String characterName) {
		return characterReputation.getOrDefault(characterName, 50);
	}

	/**
	 * Установка выбора игрока
	 */
	public static void setPlayerChoice(String choiceId, String choiceValue) {
		playerChoices.put(choiceId, choiceValue);
	}

	/**
	 * Получение выбора игрока
	 */
	public static String getPlayerChoice(String choiceId) {
		return playerChoices.get(choiceId);
	}

	/**
	 * Снимок текущих выборов игрока (для истории/отката).
	 */
	public static Map<String, String> getPlayerChoicesSnapshot() {
		return new HashMap<>(playerChoices);
	}

	/**
	 * Полная замена выборов игрока (восстановление из истории).
	 */
	public static void restorePlayerChoices(Map<String, String> snapshot) {
		playerChoices.clear();
		if (snapshot != null) {
			playerChoices.putAll(snapshot);
		}
	}

	/**
	 * Сброс всех данных игры
	 */
	public static void resetGameData() {
		gameFlags.clear();
		characterReputation.clear();
		playerChoices.clear();
	}

	/**
	 * Сохранение данных игры
	 */
	public static Map<String, Object> saveGameData() {
		Map<String, Object> saveData = new HashMap<>();
		saveData.put("flags", new HashMap<>(gameFlags));
		saveData.put("reputation", new HashMap<>(characterReputation));
		saveData.put("choices", new HashMap<>(playerChoices));
		return saveData;
	}

	/**
	 * Загрузка данных игры
	 */
	@SuppressWarnings("unchecked")
	public static void loadGameData(Map<String, Object> saveData) {
		if (saveData.containsKey("flags")) {
			gameFlags.putAll((Map<String, Boolean>) saveData.get("flags"));
		}
		if (saveData.containsKey("reputation")) {
			characterReputation.putAll((Map<String, Integer>) saveData.get("reputation"));
		}
		if (saveData.containsKey("choices")) {
			playerChoices.putAll((Map<String, String>) saveData.get("choices"));
		}
	}
}

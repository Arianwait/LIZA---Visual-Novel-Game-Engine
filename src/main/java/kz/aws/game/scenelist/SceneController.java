package kz.aws.game.scenelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import kz.aws.game.appsettings.AppSettings;
import kz.aws.game.character.ICharacter;
import kz.aws.game.fileutils.ListParser;
import kz.aws.game.scenedetails.DialogChoicesController;
import kz.aws.game.scenedetails.DialogPanel;

/**
 * Контроллер отображения одного кадра диалоговой сцены: имя, текст, варианты выбора,
 * команды. Также хранит глобальное состояние игры: флаги, репутацию и выборы игрока.
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
		processChoices(characterElement, appSettings, ICharacterList, root);
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
	 * Обрабатывает элемент choice: парсит опции и показывает панель выбора.
	 *
	 * @param characterElement XML-элемент персонажа
	 * @param appSettings      настройки
	 * @param ICharacterList   список персонажей
	 * @param root             корневой StackPane
	 */
	private void processChoices(Element characterElement, AppSettings appSettings,
			List<ICharacter> ICharacterList, StackPane root) {
		Element choiceElement = (Element) characterElement.getElementsByTagName("choice").item(0);
		if (choiceElement == null) return;

		NodeList optionList = choiceElement.getElementsByTagName("option");
		List<String> options = new ArrayList<>();
		List<Boolean> trust = new ArrayList<>();
		List<Integer> requestIds = new ArrayList<>();

		for (int k = 0; k < optionList.getLength(); k++) {
			parseOption((Element) optionList.item(k), options, requestIds, trust, ICharacterList);
		}

		DialogChoicesController buttonPane = new DialogChoicesController(appSettings);
		buttonPane.createButtons(options, requestIds, trust, appSettings);
		buttonPane.showDialogButtonPane(root);
	}

	/**
	 * Парсит одну опцию выбора из XML и добавляет данные в списки.
	 *
	 * @param optionElement XML-элемент option
	 * @param options       список текстов опций
	 * @param requestIds    список id сцен для перехода
	 * @param trust         список доступности опций
	 * @param ICharacterList список персонажей
	 */
	private void parseOption(Element optionElement, List<String> options,
			List<Integer> requestIds, List<Boolean> trust, List<ICharacter> ICharacterList) {
		requestIds.add(Integer.parseInt(optionElement.getAttribute("requestsID")));
		options.add(optionElement.getAttribute("text"));

		String choiceWriteTrue = optionElement.getAttribute("addChoice");
		String choiceWriteFalse = optionElement.getAttribute("removeChoice");
		ICharacter character = findCharacterForChoice(optionElement, ICharacterList);
		int minRep = parseOrDefault("minRep", 0, optionElement);
		int maxRep = parseOrDefault("maxRep", 100, optionElement);

		trust.add(choiceHandler(choiceWriteTrue, choiceWriteFalse, character, minRep, maxRep));
	}

	/**
	 * Ищет персонажа по атрибуту CharacterChoice в списке.
	 *
	 * @param optionElement  XML-элемент option
	 * @param ICharacterList список персонажей
	 * @return найденный персонаж или null
	 */
	private ICharacter findCharacterForChoice(Element optionElement, List<ICharacter> ICharacterList) {
		String characterChoice = optionElement.getAttribute("CharacterChoice");
		if (characterChoice == null || characterChoice.isEmpty()) return null;

		int personId = ListParser.findIdInList(characterChoice, ICharacterList);
		if (personId >= 0 && personId < ICharacterList.size()) {
			return ICharacterList.get(personId);
		}
		return null;
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

	/**
	 * Определяет доступность варианта выбора по условиям (choices + reputation).
	 *
	 * @param choiceWriteTrue  требуемые выборы (через ;)
	 * @param choiceWriteFalse запрещённые выборы (через ;)
	 * @param iCharacter       персонаж для проверки репутации (может быть null)
	 * @param minRep           минимальная репутация
	 * @param maxRep           максимальная репутация
	 * @return true — вариант доступен
	 */
	private boolean choiceHandler(String choiceWriteTrue, String choiceWriteFalse,
			ICharacter iCharacter, int minRep, int maxRep) {
		if (!matchesRequiredChoices(choiceWriteTrue, choiceWriteFalse)) return false;
		if (isReputationOutOfRange(iCharacter, minRep, maxRep)) return false;
		return true;
	}

	/**
	 * Проверяет, находится ли репутация персонажа вне допустимого диапазона.
	 *
	 * @param iCharacter персонаж (null — пропуск проверки)
	 * @param minRep     минимальная репутация
	 * @param maxRep     максимальная репутация
	 * @return true — репутация вне диапазона (вариант недоступен)
	 */
	private boolean isReputationOutOfRange(ICharacter iCharacter, int minRep, int maxRep) {
		if (iCharacter == null) return false;
		int rep = iCharacter.getReputathion();
		return rep < minRep || rep > maxRep;
	}

	/**
	 * Проверяет соответствие текущих выборов игрока требованиям опции.
	 *
	 * @param requiredChoices  требуемые выборы (через ;)
	 * @param forbiddenChoices запрещённые выборы (через ;)
	 * @return true — все условия выполнены
	 */
	private boolean matchesRequiredChoices(String requiredChoices, String forbiddenChoices) {
		for (String choice : requiredChoices.split(";")) {
			if (choice != null && !choice.isEmpty() && !SceneInfo.containsChoice(choice)) {
				return false;
			}
		}
		for (String choice : forbiddenChoices.split(";")) {
			if (choice != null && !choice.isEmpty() && SceneInfo.containsChoice(choice)) {
				return false;
			}
		}
		return true;
	}

	private static int parseOrDefault(String value, int defaultValue, Element optionElement) {
	    if (value == null || value.trim().isEmpty()) {
	        return defaultValue;
	    }
	    try {
	        return Integer.parseInt(optionElement.getAttribute(value));
	    } catch (NumberFormatException e) {
	        // Если строка содержит не-число — тоже возвращаем значение по умолчанию
	        return defaultValue;
	    }
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

package kz.aws.game.scenelist;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальное состояние игры: флаги прогресса, репутация персонажей
 * и выборы игрока (включая собранные улики). Хранится статически,
 * сохраняется и восстанавливается через {@code GameData}.
 */
public class SceneController {

	// Система флагов для отслеживания прогресса
	private static final Map<String, Boolean> gameFlags = new HashMap<>();

	// Система репутации персонажей
	private static final Map<String, Integer> characterReputation = new HashMap<>();

	// Система выборов игрока
	private static final Map<String, String> playerChoices = new HashMap<>();


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
	 * Снимок флагов прогресса (для сохранения игры).
	 *
	 * @return копия карты флагов
	 */
	public static Map<String, Boolean> getFlagsSnapshot() {
		return new HashMap<>(gameFlags);
	}

	/**
	 * Снимок репутации персонажей (для сохранения игры).
	 *
	 * @return копия карты репутации
	 */
	public static Map<String, Integer> getReputationSnapshot() {
		return new HashMap<>(characterReputation);
	}

	/**
	 * Полная замена флагов прогресса (восстановление из сохранения).
	 *
	 * @param snapshot снимок флагов (null — очистка)
	 */
	public static void restoreFlags(Map<String, Boolean> snapshot) {
		gameFlags.clear();
		if (snapshot != null) {
			gameFlags.putAll(snapshot);
		}
	}

	/**
	 * Полная замена репутации персонажей (восстановление из сохранения).
	 *
	 * @param snapshot снимок репутации (null — очистка)
	 */
	public static void restoreReputation(Map<String, Integer> snapshot) {
		characterReputation.clear();
		if (snapshot != null) {
			characterReputation.putAll(snapshot);
		}
	}
}

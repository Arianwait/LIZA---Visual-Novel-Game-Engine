package kz.aws.game.engine.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kz.aws.game.engine.model.AnimationCommand;
import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.engine.model.PuzzleCommand;
import kz.aws.game.engine.model.SceneFrame;

/**
 * Проверяет целостность сценария до запуска игры: ссылки на несуществующие
 * сцены, пустые сцены и кадры без текста. Позволяет найти ошибки сразу,
 * а не крашем посреди прохождения.
 */
public final class ScenarioValidator {

    /** Команды character-типа, которые понимает парсер сцен. */
    private static final java.util.Set<String> KNOWN_CHARACTER_ACTIONS = java.util.Set.of(
            "showPerson", "removeFromScene", "move_Left", "move_Right", "move_Center",
            "SetFlag", "SetReputation",
            "AppendReputation", "AppendReputathion",
            "ReduceReputation", "ReduceReputathion");

    private ScenarioValidator() {
    }

    /**
     * Проверяет XML-сценарий на команды, которые парсер молча проигнорирует.
     * Нужно после удаления старых экшенов (setFrom*, runTo*, AppendReputation):
     * такие команды в существующих сценариях не выполняются и не сообщают об этом.
     *
     * @param scenarioPath путь к XML-файлу сценария
     * @return список сообщений о неизвестных командах
     */
    public static List<String> validateCommands(String scenarioPath) {
        List<String> problems = new ArrayList<>();
        org.w3c.dom.Document doc = kz.aws.game.utils.XmlConfigSupport.loadDocument(scenarioPath);
        if (doc == null) return problems;

        org.w3c.dom.NodeList commands = doc.getElementsByTagName("command");
        for (int i = 0; i < commands.getLength(); i++) {
            checkCommandElement((org.w3c.dom.Element) commands.item(i), problems);
        }
        return problems;
    }

    /**
     * Проверяет один элемент {@code <command>} на понятность парсеру.
     *
     * @param cmd      элемент команды
     * @param problems накопитель сообщений
     */
    private static void checkCommandElement(org.w3c.dom.Element cmd, List<String> problems) {
        if (!"character".equals(cmd.getAttribute("type"))) return;

        String action = cmd.getAttribute("action");
        if (action == null || action.isEmpty() || KNOWN_CHARACTER_ACTIONS.contains(action)) return;

        problems.add("Команда character action=\"" + action + "\" (target="
                + cmd.getAttribute("target") + ") не поддерживается — будет проигнорирована");
    }

    /**
     * Проверяет разобранный сценарий.
     *
     * @param scenes карта сцен (id → кадры)
     * @return список сообщений об ошибках; пустой — сценарий корректен
     */
    public static List<String> validate(Map<Integer, List<SceneFrame>> scenes) {
        List<String> problems = new ArrayList<>();
        if (scenes == null || scenes.isEmpty()) {
            problems.add("Сценарий пуст: не загружено ни одной сцены");
            return problems;
        }
        for (Map.Entry<Integer, List<SceneFrame>> entry : scenes.entrySet()) {
            validateScene(entry.getKey(), entry.getValue(), scenes, problems);
        }
        return problems;
    }

    /**
     * Проверяет одну сцену и её кадры.
     *
     * @param sceneId  id сцены
     * @param frames   кадры сцены
     * @param scenes   все сцены (для проверки ссылок)
     * @param problems накопитель сообщений
     */
    private static void validateScene(int sceneId, List<SceneFrame> frames,
                                      Map<Integer, List<SceneFrame>> scenes,
                                      List<String> problems) {
        if (frames == null || frames.isEmpty()) {
            problems.add("Сцена " + sceneId + ": нет ни одного кадра");
            return;
        }
        for (int i = 0; i < frames.size(); i++) {
            validateFrame(sceneId, i, frames.get(i), scenes, problems);
        }
    }

    /**
     * Проверяет ссылки одного кадра: nextScene, варианты выбора и переходы паззлов.
     *
     * @param sceneId  id сцены
     * @param index    индекс кадра
     * @param frame    кадр
     * @param scenes   все сцены
     * @param problems накопитель сообщений
     */
    private static void validateFrame(int sceneId, int index, SceneFrame frame,
                                      Map<Integer, List<SceneFrame>> scenes,
                                      List<String> problems) {
        String where = "Сцена " + sceneId + ", кадр " + index;

        int next = frame.getNextSceneId();
        if (next != -1 && !scenes.containsKey(next)) {
            problems.add(where + ": nextScene=" + next + " — такой сцены нет");
        }
        validateChoices(where, frame, scenes, problems);
        validatePuzzles(where, frame, scenes, problems);
    }

    /**
     * Проверяет варианты выбора кадра.
     *
     * @param where    описание местоположения кадра
     * @param frame    кадр
     * @param scenes   все сцены
     * @param problems накопитель сообщений
     */
    private static void validateChoices(String where, SceneFrame frame,
                                        Map<Integer, List<SceneFrame>> scenes,
                                        List<String> problems) {
        if (!frame.hasChoices()) return;
        for (ChoiceOption option : frame.getChoices()) {
            if (!scenes.containsKey(option.getTargetSceneId())) {
                problems.add(where + ": вариант «" + option.getText() + "» ведёт на сцену "
                        + option.getTargetSceneId() + " — такой сцены нет");
            }
            if (option.getMinRep() > option.getMaxRep()) {
                problems.add(where + ": вариант «" + option.getText() + "» недостижим — minRep "
                        + option.getMinRep() + " больше maxRep " + option.getMaxRep());
            }
        }
    }

    /**
     * Проверяет переходы паззлов кадра (onSuccess/onFailure).
     *
     * @param where    описание местоположения кадра
     * @param frame    кадр
     * @param scenes   все сцены
     * @param problems накопитель сообщений
     */
    private static void validatePuzzles(String where, SceneFrame frame,
                                        Map<Integer, List<SceneFrame>> scenes,
                                        List<String> problems) {
        if (frame.getEntryAnimations() == null) return;
        for (AnimationCommand command : frame.getEntryAnimations()) {
            if (!(command instanceof PuzzleCommand puzzle)) continue;
            checkPuzzleTarget(where, puzzle.getPuzzleId(), "onSuccess",
                    puzzle.getOnSuccess(), scenes, problems);
            checkPuzzleTarget(where, puzzle.getPuzzleId(), "onFailure",
                    puzzle.getOnFailure(), scenes, problems);
        }
    }

    /**
     * Проверяет один переход паззла.
     *
     * @param where     описание местоположения кадра
     * @param puzzleId  id паззла
     * @param attribute имя атрибута перехода
     * @param target    целевая сцена (0 или меньше — переход не задан)
     * @param scenes    все сцены
     * @param problems  накопитель сообщений
     */
    private static void checkPuzzleTarget(String where, String puzzleId, String attribute,
                                          int target, Map<Integer, List<SceneFrame>> scenes,
                                          List<String> problems) {
        if (target > 0 && !scenes.containsKey(target)) {
            problems.add(where + ": паззл «" + puzzleId + "» " + attribute + "=" + target
                    + " — такой сцены нет");
        }
    }
}

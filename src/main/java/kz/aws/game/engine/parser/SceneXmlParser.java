package kz.aws.game.engine.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import kz.aws.game.engine.model.AnimationCommand;
import kz.aws.game.engine.model.BlinkEffectCommand;
import kz.aws.game.engine.model.CharacterState;
import kz.aws.game.engine.model.CharacterState.Position;
import kz.aws.game.engine.model.ClueData;
import kz.aws.game.engine.model.CloseEyesEffectCommand;
import kz.aws.game.engine.model.ColorFilterEffectCommand;
import kz.aws.game.engine.model.PuzzleCommand;
import kz.aws.game.engine.model.ShakeEffectCommand;
import kz.aws.game.engine.model.ChoiceOption;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.StopEffectCommand;
import kz.aws.game.engine.model.VisualEffectCommand;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.model.ZoomEffectCommand;
import kz.aws.game.engine.resources.CharacterLibrary;

public class SceneXmlParser {

    public static Map<Integer, List<SceneFrame>> parseAllScenes() {
        Map<Integer, List<SceneFrame>> allScenes = new HashMap<>();

        try {
            File xmlFile = new File("lib/Scene/Dialog_Structured.xml");
            if (!xmlFile.exists()) {
                 xmlFile = new File("lib/Scene/Dialog.xml");
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList dialogList = doc.getElementsByTagName("dialog");
            for (int i = 0; i < dialogList.getLength(); i++) {
                Node node = dialogList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element dialogElement = (Element) node;
                    String idStr = dialogElement.getAttribute("id");
                    if (idStr != null && !idStr.isEmpty()) {
                        int id = Integer.parseInt(idStr);
                        List<SceneFrame> frames = parseDialogElement(dialogElement);
                        allScenes.put(id, frames);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allScenes;
    }

    public static List<SceneFrame> parseScene(int sceneId) {
        // Fallback or legacy usage
        return parseAllScenes().getOrDefault(sceneId, new ArrayList<>());
    }

    private static List<SceneFrame> parseDialogElement(Element dialogElement) {
        List<SceneFrame> frames = new ArrayList<>();

        // Parse nextScene attribute
        int nextSceneId = -1;
        if (dialogElement.hasAttribute("nextScene")) {
            try {
                nextSceneId = Integer.parseInt(dialogElement.getAttribute("nextScene"));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        // Initial state
        VisualState currentState = new VisualState();

        if (dialogElement.hasAttribute("background")) {
            currentState.setBackgroundPath(dialogElement.getAttribute("background"));
        }
        if (dialogElement.hasAttribute("music")) {
            currentState.setMusicPath(dialogElement.getAttribute("music"));
        }

        NodeList childNodes = dialogElement.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            String tagName = node.getNodeName();

            if ("overlay".equals(tagName)) {
                Element overlayElement = (Element) node;
                String overlayText = overlayElement.getAttribute("text");
                if (overlayText == null) overlayText = "";
                SceneFrame frame = new SceneFrame("", "", currentState.clone());
                frame.setOverlayText(overlayText.trim().isEmpty() ? null : overlayText.trim());
                frame.setNextSceneId(nextSceneId);
                applyDarkBackgroundForOverlay(frame);
                frames.add(frame);
                continue;
            }

            if ("character".equals(tagName)) {
                Element charElement = (Element) node;

                List<AnimationCommand> stepAnimations = processCommands(charElement, currentState);

                String name = charElement.getAttribute("name");
                String color = charElement.getAttribute("color");
                if (color == null || color.isEmpty()) {
                    color = CharacterLibrary.getCharacterColor(name);
                }
                String style = charElement.getAttribute("style"); // Read style attribute
                String voice = charElement.getAttribute("voice"); // Read voice attribute
                String overlayAttr = charElement.getAttribute("overlay");
                TextParseResult parsed = getTextContentWithClues(charElement);

                SceneFrame frame = new SceneFrame(name, parsed.flatText, currentState.clone());
                if (!parsed.clues.isEmpty()) {
                    frame.setClues(parsed.clues);
                }
                frame.setSpeakerColor(color);
                frame.setTextStyle(style); // Set style
                frame.setVoicePath(voice); // Set voice path
                if (overlayAttr != null && !overlayAttr.trim().isEmpty()) {
                    frame.setOverlayText(overlayAttr.trim());
                    applyDarkBackgroundForOverlay(frame);
                }
                parseInputRequest(charElement, frame);
                parseThemeSwitch(charElement, frame);
                parseChoices(charElement, frame);
                frame.setEntryAnimations(stepAnimations);
                frame.setNextSceneId(nextSceneId);
                frames.add(frame);
            }
        }
        return frames;
    }

    /** Путь к чёрному фону для кадров с overlay: начинаем с тёмного экрана. */
    private static final String DARK_BACKGROUND_PATH = "lib/Scene/Scene1/BlackScene.png";

    /**
     * Если кадр показывается с overlay, под ним ставим тёмный фон,
     * чтобы не просвечивал фон следующей сцены и начало было с тёмного экрана.
     */
    private static void applyDarkBackgroundForOverlay(SceneFrame frame) {
        if (frame.getOverlayText() != null && !frame.getOverlayText().isEmpty()) {
            frame.getVisualState().setBackgroundPath(DARK_BACKGROUND_PATH);
        }
    }

    /**
     * Ищет в элементе &lt;character&gt; команду type="input" и задаёт на кадре запрос ввода (key, prompt).
     */
    private static void parseInputRequest(Element charElement, SceneFrame frame) {
        NodeList cmdList = charElement.getElementsByTagName("command");
        for (int k = 0; k < cmdList.getLength(); k++) {
            Element cmd = (Element) cmdList.item(k);
            if (!"input".equals(cmd.getAttribute("type"))) continue;
            String key = cmd.getAttribute("key");
            if (key == null || key.trim().isEmpty()) continue;
            String prompt = cmd.getAttribute("prompt");
            frame.setInputRequestKey(key.trim());
            frame.setInputRequestPrompt(prompt != null && !prompt.isEmpty() ? prompt.trim() : "Введите значение:");
            break;
        }
    }

    /**
     * Ищет в элементе &lt;character&gt; команду type="theme" value="hitech|classic|walk" и задаёт переключение темы на кадре.
     */
    private static void parseThemeSwitch(Element charElement, SceneFrame frame) {
        NodeList cmdList = charElement.getElementsByTagName("command");
        for (int k = 0; k < cmdList.getLength(); k++) {
            Element cmd = (Element) cmdList.item(k);
            if (!"theme".equals(cmd.getAttribute("type"))) continue;
            String value = cmd.getAttribute("value");
            if (value != null && !value.trim().isEmpty()) {
                frame.setSwitchToTheme(value.trim());
            }
            break;
        }
    }

    /**
     * Парсит блок {@code <choice>} с вариантами {@code <option>} внутри элемента персонажа.
     *
     * @param charElement элемент {@code <character>}
     * @param frame       кадр, в который записываются варианты
     */
    private static void parseChoices(Element charElement, SceneFrame frame) {
        NodeList choiceList = charElement.getElementsByTagName("choice");
        if (choiceList.getLength() == 0) return;

        Element choiceElement = (Element) choiceList.item(0);
        NodeList optionList = choiceElement.getElementsByTagName("option");
        if (optionList.getLength() == 0) return;

        List<ChoiceOption> choices = new ArrayList<>();
        for (int i = 0; i < optionList.getLength(); i++) {
            Element opt = (Element) optionList.item(i);
            String text = opt.getAttribute("text");
            int targetId = parseAttrInt(opt, "requestsID");
            String addChoice = opt.getAttribute("addChoice");
            String removeChoice = opt.getAttribute("removeChoice");
            String charChoice = opt.getAttribute("CharacterChoice");
            int minRep = parseAttrInt(opt, "minRep");
            int maxRep = parseAttrInt(opt, "maxRep");
            if (minRep < 0) minRep = 0;
            if (maxRep < 0) maxRep = 100;
            choices.add(new ChoiceOption(text, targetId, addChoice, removeChoice, charChoice, minRep, maxRep));
        }
        frame.setChoices(choices);
    }

    private static List<AnimationCommand> processCommands(Element charElement, VisualState currentState) {
        List<AnimationCommand> anims = new ArrayList<>();

        NodeList commandsList = charElement.getElementsByTagName("command");

        for (int i = 0; i < commandsList.getLength(); i++) {
             Element cmd = (Element) commandsList.item(i);
             String type = cmd.getAttribute("type");
             String action = cmd.getAttribute("action");
             String value = cmd.getAttribute("value");
             String target = cmd.getAttribute("target");

             if ((type == null || type.isEmpty()) && cmd.getTextContent().contains(":")) {
                 parseLegacyCommand(cmd.getTextContent(), currentState, anims);
                 continue;
             }

             if ("panel".equals(type) || "puzzle".equals(type)) {
                 String puzzleId  = cmd.getAttribute("id");
                 String flag      = cmd.getAttribute("flag");
                 int onSuccess    = parseAttrInt(cmd, "onSuccess");
                 int onFailure    = parseAttrInt(cmd, "onFailure");
                 if (puzzleId != null && !puzzleId.isEmpty()) {
                     anims.add(new PuzzleCommand(puzzleId, flag, onSuccess, onFailure));
                 }
             } else if ("effect".equals(type)) {
                 String effect = cmd.getAttribute("effect");
                 VisualEffectCommand vfx = createVisualEffectCommand(effect, target);
                 if (vfx != null) anims.add(vfx);
             } else if ("background".equals(type) && "changebackground".equals(action)) {
                 currentState.setBackgroundPath(value);
             } else if ("character".equals(type)) {
                 if ("showPerson".equals(action)) {
                     CharacterState charState = currentState.getCharacter(target);
                     charState.setVisible(true);
                     charState.setPose(value);
                     if (charState.getPosition() == Position.OUT) charState.setPosition(Position.CENTER);
                     currentState.updateCharacter(target, charState);
                 } else if ("removeFromScene".equals(action)) {
                     CharacterState charState = currentState.getCharacter(target);
                     charState.setVisible(false);
                     currentState.updateCharacter(target, charState);
                 } else if ("move_Left".equals(action)) {
                     CharacterState charState = currentState.getCharacter(target);
                     charState.setPosition(Position.LEFT);
                     currentState.updateCharacter(target, charState);
                 } else if ("move_Right".equals(action)) {
                     CharacterState charState = currentState.getCharacter(target);
                     charState.setPosition(Position.RIGHT);
                     currentState.updateCharacter(target, charState);
                 } else if ("move_Center".equals(action)) {
                     CharacterState charState = currentState.getCharacter(target);
                     charState.setPosition(Position.CENTER);
                     currentState.updateCharacter(target, charState);
                 }
             }
        }

        return anims;
    }

    private static void parseLegacyCommand(String cmdText, VisualState state, List<AnimationCommand> anims) {
        String[] parts = cmdText.split(":");
        if (parts.length < 2) return;

        String target = parts[0];
        String action = parts[1];
        String value = parts.length > 2 ? parts[2] : "";

        if ("background".equals(target) && "changebackground".equals(action)) {
            state.setBackgroundPath(value);
        } else if ("showPerson".equals(action)) {
             CharacterState cs = state.getCharacter(target);
             cs.setVisible(true);
             cs.setPose(value);
             if (cs.getPosition() == Position.OUT) cs.setPosition(Position.CENTER);
        } else if ("removeFromScene".equals(action)) {
             CharacterState cs = state.getCharacter(target);
             cs.setVisible(false);
        } else if ("move_Left".equals(action)) {
             CharacterState cs = state.getCharacter(target);
             cs.setPosition(Position.LEFT);
        } else if ("move_Right".equals(action)) {
             CharacterState cs = state.getCharacter(target);
             cs.setPosition(Position.RIGHT);
        } else if ("move_Center".equals(action)) {
             CharacterState cs = state.getCharacter(target);
             cs.setPosition(Position.CENTER);
        }
    }

    /**
     * Создаёт команду визуального эффекта по имени.
     *
     * @param effect имя эффекта (blink, zoom, grayscale, sepia, stopEffect, clearEffects)
     * @param target цель для stopEffect (id эффекта для остановки)
     * @return команда эффекта или {@code null} если тип не распознан
     */
    private static VisualEffectCommand createVisualEffectCommand(String effect, String target) {
        if (effect == null || effect.isEmpty()) return null;

        return switch (effect) {
            case "blink" -> new BlinkEffectCommand();
            case "closeEyes" -> new CloseEyesEffectCommand();
            case "shake" -> new ShakeEffectCommand();
            case "zoom" -> new ZoomEffectCommand();
            case "grayscale", "sepia" -> new ColorFilterEffectCommand(effect);
            case "stopEffect" -> new StopEffectCommand(target);
            case "clearEffects" -> new StopEffectCommand("");
            default -> null;
        };
    }

    private static int parseAttrInt(Element element, String attr) {
        String v = element.getAttribute(attr);
        if (v == null || v.isEmpty()) return -1;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException e) { return -1; }
    }

    private static String getTextContent(Element element) {
        return getTextContentWithClues(element).flatText;
    }

    private static class TextParseResult {
        String flatText;
        List<ClueData> clues;
    }

    private static TextParseResult getTextContentWithClues(Element element) {
        StringBuilder sb = new StringBuilder();
        List<ClueData> clues = new ArrayList<>();
        NodeList childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                String chunk = node.getNodeValue().trim();
                if (!chunk.isEmpty()) {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                        sb.append(" ");
                    }
                    sb.append(chunk);
                }
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                if ("clue".equals(child.getNodeName())) {
                    String key = child.getAttribute("key");
                    String value = child.getAttribute("value");
                    String displayText = child.getTextContent().trim();

                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                        sb.append(" ");
                    }
                    sb.append(displayText);

                    clues.add(new ClueData(key, value, displayText));
                }
                // <command> и другие элементы — пропускаем (как раньше)
            }
        }

        TextParseResult result = new TextParseResult();
        result.flatText = sb.toString().trim();
        result.clues = clues;
        return result;
    }
}

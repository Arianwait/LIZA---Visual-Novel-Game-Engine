package kz.aws.game.parsers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import kz.aws.game.scenelist.SceneController;

/**
 * Парсер для структурированных диалогов
 */
public class StructuredDialogParser {
    
    /**
     * Обработка структурированных команд
     */
    public static void processStructuredCommands(Element characterElement) {
        NodeList commandList = characterElement.getElementsByTagName("command");
        
        for (int i = 0; i < commandList.getLength(); i++) {
            Element commandElement = (Element) commandList.item(i);
            String type = commandElement.getAttribute("type");
            String action = commandElement.getAttribute("action");
            String target = commandElement.getAttribute("target");
            String value = commandElement.getAttribute("value");
            
            processCommand(type, action, target, value);
        }
    }
    
    /**
     * Обработка структурированных эффектов
     */
    public static void processEffects(Element optionElement) {
        NodeList effectsList = optionElement.getElementsByTagName("effects");
        
        if (effectsList.getLength() > 0) {
            Element effectsElement = (Element) effectsList.item(0);
            
            // Обработка репутации
            NodeList reputationList = effectsElement.getElementsByTagName("reputation");
            for (int i = 0; i < reputationList.getLength(); i++) {
                Element reputationElement = (Element) reputationList.item(i);
                String character = reputationElement.getAttribute("character");
                String change = reputationElement.getAttribute("change");
                
                if (!character.isEmpty() && !change.isEmpty()) {
                    int currentRep = SceneController.getCharacterReputation(character);
                    int changeValue = Integer.parseInt(change.replace("+", ""));
                    SceneController.setCharacterReputation(character, currentRep + changeValue);
                }
            }
            
            // Обработка флагов
            NodeList flagList = effectsElement.getElementsByTagName("flag");
            for (int i = 0; i < flagList.getLength(); i++) {
                Element flagElement = (Element) flagList.item(i);
                String name = flagElement.getAttribute("name");
                String value = flagElement.getAttribute("value");
                
                if (!name.isEmpty() && !value.isEmpty()) {
                    SceneController.setFlag(name, Boolean.parseBoolean(value));
                }
            }
            
            // Обработка выборов
            NodeList choiceList = effectsElement.getElementsByTagName("choice");
            for (int i = 0; i < choiceList.getLength(); i++) {
                Element choiceElement = (Element) choiceList.item(i);
                String name = choiceElement.getAttribute("name");
                String value = choiceElement.getAttribute("value");
                
                if (!name.isEmpty() && !value.isEmpty()) {
                    SceneController.setPlayerChoice(name, value);
                }
            }
        }
    }
    
    /**
     * Обработка команды по типу
     */
    private static void processCommand(String type, String action, String target, String value) {
        switch (type) {
            case "character":
                processCharacterCommand(action, target, value);
                break;
            case "background":
                processBackgroundCommand(action, value);
                break;
            case "scene":
                processSceneCommand(action, value);
                break;
            case "sound":
                processSoundCommand(action, value);
                break;
            case "system":
                processSystemCommand(action, target, value);
                break;
            default:
                System.out.println("Неизвестный тип команды: " + type);
                break;
        }
    }
    
    /**
     * Обработка команд персонажей
     */
    private static void processCharacterCommand(String action, String target, String value) {
        System.out.println("Персонаж: " + target + " -> " + action + " -> " + value);
        // Здесь можно добавить логику для работы с персонажами
    }
    
    /**
     * Обработка команд фона
     */
    private static void processBackgroundCommand(String action, String value) {
        System.out.println("Фон: " + action + " -> " + value);
        // Здесь можно добавить логику для смены фона
    }
    
    /**
     * Обработка команд сцены
     */
    private static void processSceneCommand(String action, String value) {
        System.out.println("Сцена: " + action + " -> " + value);
        // Здесь можно добавить логику для работы со сценами
    }
    
    /**
     * Обработка звуковых команд
     */
    private static void processSoundCommand(String action, String value) {
        System.out.println("Звук: " + action + " -> " + value);
        // Здесь можно добавить логику для воспроизведения звуков
    }
    
    /**
     * Обработка системных команд
     */
    private static void processSystemCommand(String action, String target, String value) {
        switch (action) {
            case "SetFlag":
                SceneController.setFlag(target, Boolean.parseBoolean(value));
                break;
            case "SetReputation":
                SceneController.setCharacterReputation(target, Integer.parseInt(value));
                break;
            case "SetChoice":
                SceneController.setPlayerChoice(target, value);
                break;
            default:
                System.out.println("Неизвестная системная команда: " + action);
                break;
        }
    }
    
    /**
     * Получение структурированных опций
     */
    public static List<StructuredOption> getStructuredOptions(Element choiceElement) {
        List<StructuredOption> options = new ArrayList<>();
        NodeList optionList = choiceElement.getElementsByTagName("option");
        
        for (int i = 0; i < optionList.getLength(); i++) {
            Element optionElement = (Element) optionList.item(i);
            StructuredOption option = new StructuredOption();
            
            option.setId(optionElement.getAttribute("id"));
            option.setText(optionElement.getAttribute("text"));
            option.setNextDialog(optionElement.getAttribute("nextDialog"));
            
            // Получение описания
            NodeList descriptionList = optionElement.getElementsByTagName("description");
            if (descriptionList.getLength() > 0) {
                option.setDescription(descriptionList.item(0).getTextContent());
            }
            
            options.add(option);
        }
        
        return options;
    }
    
    /**
     * Класс для структурированной опции
     */
    public static class StructuredOption {
        private String id;
        private String text;
        private String nextDialog;
        private String description;
        
        // Геттеры и сеттеры
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getNextDialog() { return nextDialog; }
        public void setNextDialog(String nextDialog) { this.nextDialog = nextDialog; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

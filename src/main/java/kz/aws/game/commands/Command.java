package kz.aws.game.commands;

/**
 * Класс для представления команды
 */
public class Command {
    private final String character;
    private final String action;
    private final String[] parameters;
    
    public Command(String character, String action, String[] parameters) {
        this.character = character;
        this.action = action;
        this.parameters = parameters;
    }
    
    public String getCharacter() {
        return character;
    }
    
    public String getAction() {
        return action;
    }
    
    public String[] getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(character).append(":").append(action);
        for (String param : parameters) {
            sb.append(":").append(param);
        }
        return sb.toString();
    }
}

package kz.aws.game.scenelist;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialogList {

    private static final Logger LOG = LoggerFactory.getLogger(DialogList.class);

    private static List<DialogEntry> dialogList = new ArrayList<>(); // Список диалогов
    private static final int MAX_SIZE = 7; // Максимальное количество элементов

    // Статический вложенный класс для хранения информации о персонаже и его диалоге
    public static class DialogEntry {
        private final String characterName;  // Имя персонажа
        private final String characterColor; // Цвет имени персонажа
        private final String dialogText;     // Текст диалога
        private final String dialogColor;    // Цвет текста диалога

        public DialogEntry(String characterName, String characterColor, String dialogText, String dialogColor) {
            this.characterName = characterName;
            this.characterColor = characterColor;
            this.dialogText = dialogText;
            this.dialogColor = dialogColor;
        }

        public String getCharacterName() {
            return characterName;
        }

        public String getCharacterColor() {
            return characterColor;
        }

        public String getDialogText() {
            return dialogText;
        }

        public String getDialogColor() {
            return dialogColor;
        }
    }

    // Добавление нового диалога
    public static void addDialog(String name, String nameColor, String text, String textColor) {
        // Удаляем первый элемент, если размер списка превышает MAX_SIZE
        if (dialogList.size() >= MAX_SIZE) {
            dialogList.remove(0);
        }

        // Добавляем новый диалог
        dialogList.add(new DialogEntry(name, nameColor, text, textColor));
    }

    // Получение всех диалогов
    public static List<DialogEntry> getAllDialogs() {
        return dialogList;
    }

    // Печать всех диалогов
    public static void printDialogs() {
        for (DialogEntry entry : dialogList) {
            LOG.info("Имя: " + entry.getCharacterName() + " (Цвет: " + entry.getCharacterColor() + ")");
            LOG.info("  Диалог: " + entry.getDialogText() + " (Цвет: " + entry.getDialogColor() + ")");
        }
    }

    // Получение всех диалогов конкретного персонажа
    public static List<DialogEntry> getDialogsByName(String name) {
        List<DialogEntry> result = new ArrayList<>();
        for (DialogEntry entry : dialogList) {
            if (entry.getCharacterName().equals(name)) {
                result.add(entry);
            }
        }
        return result;
    }

	public static void clearDialogs() {
		dialogList.clear(); 
	}
}


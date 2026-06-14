package kz.aws.game.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Подстановка переменных в текст: {ключ} заменяется на значение из словаря.
 * Используется для имён игрока и других введённых значений в репликах и именах персонажей.
 */
public final class VariableSubstitution {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^}]+)}");

    private VariableSubstitution() {
    }

    /**
     * Заменяет в строке все вхождения {ключ} на значение из словаря.
     * Если ключа нет — подставляется пустая строка.
     *
     * @param text Исходный текст (реплика, имя персонажа, overlay).
     * @param variables Словарь ключ → значение (например, playerName → "Алексей").
     * @return Текст с подставленными значениями; если variables == null — возвращается text без замен.
     */
    public static String replaceVariables(String text, Map<String, String> variables) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (variables == null || variables.isEmpty()) {
            return text;
        }
        Matcher m = PLACEHOLDER.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1).trim();
            String value = variables.getOrDefault(key, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

package kz.aws.game.mainscene;

import java.util.ArrayList;
import java.util.List;

/**
 * Разбирает текст титров ({@code lib/titries.txt}) в строки для окна кредитов.
 * Первая непустая строка считается названием игры, строки с двоеточием
 * на конце — заголовками разделов, строки с адресом — ссылками.
 */
public final class CreditsParser {

    /** Вид строки титров. */
    public enum Kind {
        /** Название игры (первая непустая строка файла). */
        TITLE,
        /** Заголовок раздела («Благодарности:»). */
        HEADER,
        /** Обычный текст. */
        TEXT,
        /** Ссылка, открывается в браузере. */
        LINK,
        /** Пустая строка — отступ между блоками. */
        BLANK
    }

    /**
     * Одна строка титров.
     *
     * @param kind вид строки
     * @param text текст (для LINK — адрес)
     */
    public record Line(Kind kind, String text) {
    }

    private CreditsParser() {
    }

    /**
     * Разбирает текст титров построчно.
     *
     * @param content содержимое файла титров
     * @return строки с определённым видом; пустой список для null
     */
    public static List<Line> parse(String content) {
        List<Line> lines = new ArrayList<>();
        if (content == null) return lines;

        boolean titleSeen = false;
        for (String raw : content.split("\\R")) {
            String text = raw.strip();
            Kind kind = classify(text, titleSeen);
            if (kind == Kind.TITLE) titleSeen = true;
            lines.add(new Line(kind, text));
        }
        return lines;
    }

    /**
     * Определяет вид строки.
     *
     * @param text      строка без окружающих пробелов
     * @param titleSeen встречалось ли уже название игры
     * @return вид строки
     */
    private static Kind classify(String text, boolean titleSeen) {
        if (text.isEmpty()) return Kind.BLANK;
        if (!titleSeen) return Kind.TITLE;
        if (text.startsWith("http://") || text.startsWith("https://")) return Kind.LINK;
        if (text.endsWith(":")) return Kind.HEADER;
        return Kind.TEXT;
    }
}

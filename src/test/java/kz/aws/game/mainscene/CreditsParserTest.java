package kz.aws.game.mainscene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import kz.aws.game.mainscene.CreditsParser.Kind;
import kz.aws.game.mainscene.CreditsParser.Line;

/**
 * Проверяет разбор титров для окна кредитов: название, заголовки,
 * ссылки, обычный текст и пустые строки.
 */
class CreditsParserTest {

    @Test
    void firstNonEmptyLineIsTitle() {
        List<Line> lines = CreditsParser.parse("\nLookQuest\nРазработчик: ArianwaitStudio");
        assertEquals(Kind.BLANK, lines.get(0).kind());
        assertEquals(Kind.TITLE, lines.get(1).kind());
        assertEquals("LookQuest", lines.get(1).text());
        assertEquals(Kind.TEXT, lines.get(2).kind());
    }

    @Test
    void lineEndingWithColonIsHeader() {
        List<Line> lines = CreditsParser.parse("Игра\nБлагодарности:\nhaleminihaha");
        assertEquals(Kind.HEADER, lines.get(1).kind());
        assertEquals(Kind.TEXT, lines.get(2).kind());
    }

    @Test
    void urlsBecomeLinks() {
        List<Line> lines = CreditsParser.parse(
                "Игра\nhttps://pixabay.com/\nhttp://example.org\nне ссылка");
        assertEquals(Kind.LINK, lines.get(1).kind());
        assertEquals(Kind.LINK, lines.get(2).kind());
        assertEquals(Kind.TEXT, lines.get(3).kind());
    }

    @Test
    void whitespaceIsTrimmed() {
        List<Line> lines = CreditsParser.parse("Игра\n   Музыка:   ");
        assertEquals("Музыка:", lines.get(1).text());
        assertEquals(Kind.HEADER, lines.get(1).kind());
    }

    @Test
    void windowsLineEndingsAreSupported() {
        List<Line> lines = CreditsParser.parse("Игра\r\nАвтор\r\n");
        // завершающий перевод строки не даёт лишней пустой строки
        assertEquals(2, lines.size());
        assertEquals("Автор", lines.get(1).text());
    }

    @Test
    void nullAndEmptyGiveEmptyList() {
        assertTrue(CreditsParser.parse(null).isEmpty());
        assertTrue(CreditsParser.parse("").size() <= 1);
    }
}

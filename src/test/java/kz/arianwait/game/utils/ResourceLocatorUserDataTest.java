package kz.arianwait.game.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Проверяет папку данных пользователя: сохранения и логи должны лежать
 * в профиле, а не рядом с exe (после установки туда писать нельзя).
 */
class ResourceLocatorUserDataTest {

    private static final String PROPERTY = "liza.userdata";

    @TempDir
    Path tempDir;

    @AfterEach
    void clearOverride() {
        System.clearProperty(PROPERTY);
    }

    @Test
    void userDataUsesOverrideAndCreatesSubfolder() {
        System.setProperty(PROPERTY, tempDir.toString());

        Path save = ResourceLocator.userData("save");

        assertEquals(tempDir.resolve("save"), save);
        assertTrue(Files.isDirectory(save), "подпапка создаётся при первом обращении");
    }

    @Test
    void userDataIsOutsideGameRoot() {
        System.setProperty(PROPERTY, tempDir.toString());

        Path logs = ResourceLocator.userData("logs");

        assertTrue(!logs.startsWith(ResourceLocator.getGameRoot()),
                "данные пользователя не должны лежать в папке игры");
    }
}

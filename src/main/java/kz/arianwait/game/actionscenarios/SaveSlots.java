package kz.arianwait.game.actionscenarios;

import java.io.File;

/**
 * Проверки состояния папки сохранений.
 */
public final class SaveSlots {

    /** Расширение файла сохранения. */
    private static final String SAVE_EXTENSION = ".ser";

    private SaveSlots() {
    }

    /**
     * Проверяет, есть ли хотя бы одно сохранение.
     * Используется, чтобы не показывать «Продолжить» на чистой установке.
     *
     * @return true — найден хотя бы один файл сохранения
     */
    public static boolean hasAnySave() {
        File directory = SaveManager.getSaveDirectory();
        if (!directory.isDirectory()) return false;

        File[] files = directory.listFiles();
        if (files == null) return false;

        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(SAVE_EXTENSION)) {
                return true;
            }
        }
        return false;
    }
}

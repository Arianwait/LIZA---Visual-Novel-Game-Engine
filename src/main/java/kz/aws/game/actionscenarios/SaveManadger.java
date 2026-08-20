package kz.aws.game.actionscenarios;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import kz.aws.game.scenelist.GameData;

/**
 * Сохранение и загрузка игры через Java-сериализацию в папку {@value #SAVE_DIRECTORY}.
 */
public class SaveManadger {

    /** Папка с файлами сохранений (относительно рабочей директории). */
    public static final String SAVE_DIRECTORY = "save";

    /**
     * Сохраняет состояние игры в файл. Папка сохранений создаётся при необходимости.
     *
     * @param gameInfo состояние игры
     * @param filePath путь к файлу сохранения
     * @return true — сохранение удалось; false — файл не записан
     */
    public static boolean serializeClicker(GameData gameInfo, String filePath) {
        if (!ensureSaveDirectory(filePath)) return false;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gameInfo);
            return true;
        } catch (IOException e) {
            System.err.println("Сохранение не удалось (" + filePath + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Создаёт родительскую папку файла сохранения, если её нет.
     *
     * @param filePath путь к файлу сохранения
     * @return true — папка существует или создана
     */
    private static boolean ensureSaveDirectory(String filePath) {
        File parent = new File(filePath).getAbsoluteFile().getParentFile();
        if (parent == null || parent.isDirectory()) return true;
        if (parent.mkdirs()) return true;

        System.err.println("Не удалось создать папку сохранений: " + parent);
        return false;
    }

    /**
     * Загружает состояние игры из файла в папке сохранений.
     *
     * @param filePath имя файла сохранения внутри папки {@value #SAVE_DIRECTORY}
     * @return состояние игры или null, если файл отсутствует либо повреждён
     */
    public static GameData deserializeClicker(String filePath) {
        if (filePath == null) return null;

        File file = new File(SAVE_DIRECTORY, filePath);
        if (!file.isFile()) {
            System.err.println("Файл сохранения не найден: " + file);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Файл сохранения повреждён или несовместим (" + file
                    + "): " + e.getMessage());
            return null;
        }
    }
}

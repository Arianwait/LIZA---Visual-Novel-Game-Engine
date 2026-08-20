package kz.aws.game.actionscenarios;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import kz.aws.game.scenelist.GameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Сохранение и загрузка игры через Java-сериализацию в папку {@value #SAVE_DIRECTORY}.
 */
public class SaveManager {

    private static final Logger LOG = LoggerFactory.getLogger(SaveManager.class);

    /** Папка с файлами сохранений (относительно рабочей директории). */
    public static final String SAVE_DIRECTORY = "save";

    /**
     * Сохраняет состояние игры в файл внутри папки {@value #SAVE_DIRECTORY}.
     * Папка создаётся при необходимости.
     *
     * @param gameInfo состояние игры
     * @param fileName имя файла сохранения (без пути)
     * @return true — сохранение удалось; false — файл не записан
     */
    public static boolean serializeClicker(GameData gameInfo, String fileName) {
        if (fileName == null) return false;
        File file = new File(SAVE_DIRECTORY, fileName);
        if (!ensureSaveDirectory(file)) return false;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(gameInfo);
            return true;
        } catch (IOException e) {
            LOG.error("Сохранение не удалось (" + file + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Создаёт родительскую папку файла сохранения, если её нет.
     *
     * @param file файл сохранения
     * @return true — папка существует или создана
     */
    private static boolean ensureSaveDirectory(File file) {
        File parent = file.getAbsoluteFile().getParentFile();
        if (parent == null || parent.isDirectory()) return true;
        if (parent.mkdirs()) return true;

        LOG.error("Не удалось создать папку сохранений: " + parent);
        return false;
    }

    /**
     * Загружает состояние игры из файла в папке {@value #SAVE_DIRECTORY}.
     *
     * @param fileName имя файла сохранения (без пути)
     * @return состояние игры или null, если файл отсутствует либо повреждён
     */
    public static GameData deserializeClicker(String fileName) {
        if (fileName == null) return null;

        File file = new File(SAVE_DIRECTORY, fileName);
        if (!file.isFile()) {
            LOG.error("Файл сохранения не найден: " + file);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOG.error("Файл сохранения повреждён или несовместим (" + file
                    + "): " + e.getMessage());
            return null;
        }
    }
}

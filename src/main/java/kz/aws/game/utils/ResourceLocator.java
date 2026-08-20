package kz.aws.game.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Единая точка разрешения путей к ресурсам игры (папка {@code lib/}).
 *
 * <p>Пути в коде и конфигах записаны относительно корня игры. Раньше они
 * разрешались относительно текущей рабочей директории, поэтому запуск .exe
 * не из корня давал чёрный экран без объяснений. Локатор ищет корень
 * рядом с исполняемым jar и падает обратно на рабочую директорию.
 */
public final class ResourceLocator {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceLocator.class);

    /** Папка с ресурсами игры внутри корня. */
    private static final String LIB_DIRECTORY = "lib";
    /** Системное свойство для явного указания корня игры. */
    private static final String ROOT_PROPERTY = "liza.home";

    private static Path gameRoot;

    private ResourceLocator() {
    }

    /**
     * Корень игры — папка, содержащая {@code lib/}.
     * Порядок поиска: системное свойство {@code liza.home}, папка рядом с jar,
     * её родитель (запуск из {@code target/}), текущая рабочая директория.
     *
     * @return путь к корню игры
     */
    public static synchronized Path getGameRoot() {
        if (gameRoot == null) {
            gameRoot = detectGameRoot();
        }
        return gameRoot;
    }

    /**
     * Определяет корень игры перебором кандидатов.
     *
     * @return первый каталог, содержащий {@code lib/}; иначе рабочая директория
     */
    private static Path detectGameRoot() {
        for (Path candidate : candidateRoots()) {
            Path found = findRootUpwards(candidate);
            if (found != null) return found;
        }
        return Paths.get("").toAbsolutePath();
    }

    /**
     * Ищет каталог с {@code lib/} в самом пути и у его родителей.
     * Нужно, когда код запущен из {@code target/classes}, а ресурсы лежат
     * в корне проекта на пару уровней выше.
     *
     * @param start каталог, с которого начинается подъём (может быть null)
     * @return найденный корень или null
     */
    private static Path findRootUpwards(Path start) {
        for (Path dir = start; dir != null; dir = dir.getParent()) {
            if (Files.isDirectory(dir.resolve(LIB_DIRECTORY))) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Кандидаты на корень игры в порядке приоритета.
     *
     * @return массив путей (могут содержать null)
     */
    private static Path[] candidateRoots() {
        String configured = System.getProperty(ROOT_PROPERTY);
        return new Path[] {
            configured != null && !configured.isEmpty() ? Paths.get(configured) : null,
            Paths.get("").toAbsolutePath(),
            locateCodeDirectory()
        };
    }

    /**
     * Папка, из которой запущен код (рядом с jar или classes).
     *
     * @return путь или null, если определить не удалось
     */
    private static Path locateCodeDirectory() {
        try {
            Path location = Paths.get(ResourceLocator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return Files.isDirectory(location) ? location : location.getParent();
        } catch (URISyntaxException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Разрешает путь к ресурсу относительно корня игры.
     *
     * @param relativePath путь вида {@code lib/config/style.css}
     * @return абсолютный путь к файлу
     */
    public static Path resolve(String relativePath) {
        return getGameRoot().resolve(relativePath);
    }

    /**
     * Разрешает путь к ресурсу и возвращает файл.
     *
     * @param relativePath путь вида {@code lib/Scene/Person.xml}
     * @return файл (может не существовать)
     */
    public static File file(String relativePath) {
        return resolve(relativePath).toFile();
    }

    /**
     * Проверяет наличие ресурса и сообщает понятную ошибку, если его нет.
     *
     * @param relativePath путь к ресурсу
     * @return true — ресурс найден
     */
    public static boolean exists(String relativePath) {
        if (Files.exists(resolve(relativePath))) return true;
        LOG.error("Ресурс не найден: " + relativePath
                + " (искали в " + getGameRoot() + "). Запускайте игру из папки,"
                + " содержащей lib/, либо задайте -D" + ROOT_PROPERTY + "=путь");
        return false;
    }

    /**
     * URL-строка ресурса для JavaFX ({@code Image}, стили, FXML).
     *
     * @param relativePath путь вида {@code lib/Logo/logo.png}
     * @return строка вида {@code file:/...}
     */
    public static String url(String relativePath) {
        return resolve(relativePath).toUri().toString();
    }
}

package kz.aws.game.soundtrack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import kz.aws.game.appsettings.AppSettings;

/**
 * Разовые звуковые эффекты (выстрел, скрип двери и т.п.).
 * В отличие от {@link Soundtrack} эффект проигрывается ОДИН раз и сам
 * освобождает ресурсы по завершении. Несколько эффектов могут звучать
 * одновременно — новый не обрывает предыдущий.
 */
public final class SoundEffect {

    /** Активные эффекты — держим ссылки, чтобы GC не собрал плеер во время игры. */
    private static final List<MediaPlayer> activePlayers = new ArrayList<>();

    private SoundEffect() {
    }

    /**
     * Создаёт плеер разового звукового эффекта.
     * Плеер освобождается автоматически после окончания воспроизведения.
     *
     * @param appSettings   настройки приложения (громкость)
     * @param audioFilePath путь к аудиофайлу
     * @return плеер, готовый к play(); null — если файл не найден или не читается
     */
    public static MediaPlayer startSound(AppSettings appSettings, String audioFilePath) {
        File file = new File(audioFilePath);
        if (!file.isFile()) {
            System.err.println("SoundEffect: файл не найден — " + audioFilePath);
            return null;
        }
        try {
            return createPlayer(appSettings, file);
        } catch (RuntimeException e) {
            System.err.println("SoundEffect: не удалось воспроизвести " + audioFilePath
                    + " — " + e.getMessage());
            return null;
        }
    }

    /**
     * Создаёт и регистрирует плеер с автоочисткой после окончания.
     *
     * @param appSettings настройки приложения (громкость)
     * @param file        аудиофайл
     * @return настроенный плеер
     */
    private static MediaPlayer createPlayer(AppSettings appSettings, File file) {
        Media media = new Media(file.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        player.setVolume(appSettings.getVolumeValue());
        player.setOnEndOfMedia(() -> release(player));
        player.setOnError(() -> release(player));
        activePlayers.add(player);
        return player;
    }

    /**
     * Останавливает плеер и снимает его с учёта.
     *
     * @param player плеер эффекта
     */
    private static void release(MediaPlayer player) {
        activePlayers.remove(player);
        player.dispose();
    }

    /** Останавливает все звучащие эффекты (смена сцены, выход в меню). */
    public static void stopAll() {
        for (MediaPlayer player : new ArrayList<>(activePlayers)) {
            player.stop();
            release(player);
        }
    }
}

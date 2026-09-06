package kz.arianwait.game.soundtrack;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import kz.arianwait.game.appsettings.AppSettings;
import kz.arianwait.game.utils.ResourceLocator;

import java.io.File;

public class Soundtrack {

    private static MediaPlayer mediaPlayer;

    public static MediaPlayer startSound(AppSettings appSettings, String audioFilePath) {

        // Проверяем, создан ли mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        Media media = new Media(ResourceLocator.media(audioFilePath));
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        mediaPlayer.setVolume(appSettings.getVolumeValue());
        // Устанавливаем prefetch в продолжительность файла
        mediaPlayer.setStartTime(Duration.ZERO);
        mediaPlayer.setStopTime(media.getDuration());

        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));

        return mediaPlayer;
    }
}


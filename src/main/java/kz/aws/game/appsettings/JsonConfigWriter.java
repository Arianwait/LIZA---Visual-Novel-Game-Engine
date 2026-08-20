package kz.aws.game.appsettings;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import kz.aws.game.utils.ResourceLocator;

@SuppressWarnings("deprecation")
public class JsonConfigWriter {
	private static String filePath = "lib/config/SettingsConfig.json";
    @SuppressWarnings("unchecked")
	public static void writeConfig(AppSettings appSettings) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("windowWidth", appSettings.getWindowWidth());
        jsonObject.put("windowHeight", appSettings.getWindowHeight());
        jsonObject.put("fullscreen", appSettings.isFullscreen());
        jsonObject.put("volumeValue", appSettings.getVolumeValue());
        jsonObject.put("uiTheme", appSettings.getUiTheme());

        try (Writer writer = Files.newBufferedWriter(ResourceLocator.resolve(filePath), StandardCharsets.UTF_8)) {
            writer.write(jsonObject.toJSONString());
        } catch (IOException e) {
            System.err.println("Настройки не сохранены (" + filePath + "): " + e.getMessage());
        }
    }
}

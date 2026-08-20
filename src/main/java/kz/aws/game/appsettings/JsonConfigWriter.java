package kz.aws.game.appsettings;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import kz.aws.game.utils.ResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class JsonConfigWriter {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConfigWriter.class);
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
            LOG.error("Настройки не сохранены (" + filePath + "): " + e.getMessage());
        }
    }
}

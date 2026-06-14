package kz.aws.game.appsettings;

import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

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

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
            // Обработка ошибок при записи в JSON
        }
    }
}

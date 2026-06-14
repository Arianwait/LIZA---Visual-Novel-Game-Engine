package kz.aws.game.appsettings;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;


@SuppressWarnings("deprecation")
public class JsonParser  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3180711135097913085L;
	private static String filePath = "lib/config/SettingsConfig.json";
    public static AppSettings readConfig() {
        JSONParser parser = new JSONParser();
        AppSettings appSettings = new DefaultAppSettings();

        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            appSettings.updateSettings(jsonObject);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            // Обработка ошибок при чтении JSON
        }

        return appSettings;
    }
}
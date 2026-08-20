package kz.aws.game.appsettings;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import kz.aws.game.utils.ResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("deprecation")
public class JsonParser  implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -3180711135097913085L;
	private static String filePath = "lib/config/SettingsConfig.json";
    public static AppSettings readConfig() {
        JSONParser parser = new JSONParser();
        AppSettings appSettings = new DefaultAppSettings();

        // явная UTF-8: кодировка платформы ломала кириллицу в теме/путях
        try (Reader reader = Files.newBufferedReader(ResourceLocator.resolve(filePath), StandardCharsets.UTF_8)) {
            Object obj = parser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;
            appSettings.updateSettings(jsonObject);
        } catch (IOException | ParseException | RuntimeException e) {
            LOG.error("Настройки не прочитаны (" + filePath + "): " + e.getMessage()
                    + " — используются значения по умолчанию");
        }

        return appSettings;
    }
}

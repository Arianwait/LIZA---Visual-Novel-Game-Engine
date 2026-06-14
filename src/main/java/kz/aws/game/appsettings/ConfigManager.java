package kz.aws.game.appsettings;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
 private final Properties properties = new Properties();
 private final String configFilePath;

 /**
  * Конструктор ConfigManager.
  *
  * @param filePath Путь к конфигурационному файлу.
  */
 public ConfigManager(String filePath) {
     this.configFilePath = filePath;
     loadProperties();
 }

 /**
  * Загружает свойства из конфигурационного файла.
  */
 private void loadProperties() {
     try (FileInputStream fis = new FileInputStream(configFilePath)) {
         properties.load(fis);
     } catch (FileNotFoundException e) {
         System.out.println("Конфигурационный файл не найден, будет создан новый.");
     } catch (IOException e) {
         e.printStackTrace();
     }
 }

 /**
  * Сохраняет значение true/false в конфигурационный файл.
  *
  * @param key   Ключ, по которому будет сохранено значение.
  * @param value Значение для сохранения.
  */
 public void setBoolean(String key, boolean value) {
     properties.setProperty(key, Boolean.toString(value));
     saveProperties();
 }

 /**
  * Читает значение true/false из конфигурационного файла.
  *
  * @param key          Ключ для чтения значения.
  * @param defaultValue Значение по умолчанию, если ключ отсутствует.
  * @return Значение из файла или значение по умолчанию.
  */
 public boolean getBoolean(String key, boolean defaultValue) {
     return Boolean.parseBoolean(properties.getProperty(key, Boolean.toString(defaultValue)));
 }

 /**
  * Сохраняет свойства в конфигурационный файл.
  */
 private void saveProperties() {
     try (FileOutputStream fos = new FileOutputStream(configFilePath)) {
         properties.store(fos, "Configuration File");
     } catch (IOException e) {
         e.printStackTrace();
     }
 }
}


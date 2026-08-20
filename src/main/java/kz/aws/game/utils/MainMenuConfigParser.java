package kz.aws.game.utils;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Парсер для конфигурации главного меню из XML
 */
public class MainMenuConfigParser {
    private static MainMenuConfig config;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        
        try {
            File xmlFile = new File("lib/config/UI/MainMenu.xml");
            if (!xmlFile.exists()) {
                System.err.println("MainMenu config not found: " + xmlFile.getAbsolutePath());
                // Используем значения по умолчанию
                config = new MainMenuConfig();
                initialized = true;
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            config = new MainMenuConfig();
            
            // Парсим ресурсы
            NodeList resourcesList = document.getElementsByTagName("resources");
            if (resourcesList.getLength() > 0) {
                Element resources = (Element) resourcesList.item(0);
                String bg = getTagValue(resources, "background-image");
                if (!bg.isEmpty()) config.backgroundImagePath = bg;
                String table = getTagValue(resources, "table-image");
                if (!table.isEmpty()) config.tableImagePath = table;
                String music = getTagValue(resources, "music");
                if (!music.isEmpty()) config.musicPath = music;
            }
            
            // Парсим позиционирование
            NodeList positioningList = document.getElementsByTagName("positioning");
            if (positioningList.getLength() > 0) {
                Element positioning = (Element) positioningList.item(0);
                config.marginRight = parseDouble(getTagValue(positioning, "margin-right"), 0.7);
                config.marginTop = parseDouble(getTagValue(positioning, "margin-top"), 0.2);
                config.marginLeft = parseDouble(getTagValue(positioning, "margin-left"), 0.05);
            }
            
            // Парсим шрифт
            NodeList fontList = document.getElementsByTagName("font");
            if (fontList.getLength() > 0) {
                Element font = (Element) fontList.item(0);
                config.titleFontSizeMultiplier = parseDouble(getTagValue(font, "title-size-multiplier"), 0.03);
            }
            
            // Парсим отступы
            NodeList spacingList = document.getElementsByTagName("spacing");
            if (spacingList.getLength() > 0) {
                Element spacing = (Element) spacingList.item(0);
                config.menuPadding = parseDouble(getTagValue(spacing, "menu-padding"), 50.0);
                config.menuSpacing = parseDouble(getTagValue(spacing, "menu-spacing"), 10.0);
            }
            
            // Парсим настройки предзагрузки
            NodeList preloadList = document.getElementsByTagName("preload");
            if (preloadList.getLength() > 0) {
                Element preload = (Element) preloadList.item(0);
                config.preloadTimeoutMs = parseLong(getTagValue(preload, "timeout-ms"), 5000);
                config.preloadCheckIntervalMs = parseLong(getTagValue(preload, "check-interval-ms"), 50);
            }
            
            // Парсим настройки заголовка
            NodeList titleList = document.getElementsByTagName("title");
            if (titleList.getLength() > 0) {
                Element title = (Element) titleList.item(0);
                config.titleText = getTagValue(title, "text");
                config.titleColor = getTagValue(title, "color");
                config.titleWeight = getTagValue(title, "weight");
            }
            
            initialized = true;
        } catch (Exception e) {
            System.err.println("Error parsing MainMenu config: " + e.getMessage());
            e.printStackTrace();
            // Используем значения по умолчанию
            config = new MainMenuConfig();
            initialized = true;
        }
    }

    /**
     * Возвращает текст вложенного тега.
     *
     * @param element родительский элемент
     * @param tagName имя тега
     * @return текст или null
     */
    private static String getTagValue(Element element, String tagName) {
        return XmlConfigSupport.getTagValue(element, tagName);
    }

    /**
     * Разбирает дробное число со значением по умолчанию.
     *
     * @param value        строка значения
     * @param defaultValue значение по умолчанию
     * @return разобранное число или defaultValue
     */
    private static double parseDouble(String value, double defaultValue) {
        return XmlConfigSupport.parseDouble(value, defaultValue);
    }

    /**
     * Разбирает длинное целое со значением по умолчанию.
     *
     * @param value        строка значения
     * @param defaultValue значение по умолчанию
     * @return разобранное число или defaultValue
     */
    private static long parseLong(String value, long defaultValue) {
        return XmlConfigSupport.parseLong(value, defaultValue);
    }

    public static MainMenuConfig getConfig() {
        if (!initialized) initialize();
        return config;
    }

    /**
     * Класс конфигурации главного меню
     */
    public static class MainMenuConfig {
        // Ресурсы
        public String backgroundImagePath = "file:lib/MainMenuImg/Back.png";
        public String tableImagePath = "file:lib/MainMenuImg/table/MenuTable.png";
        public String musicPath = "lib/sound/Palace of Roses - Aakash Gandhi.mp3";
        
        // Позиционирование
        public double marginRight = 0.7;
        public double marginTop = 0.2;
        public double marginLeft = 0.05;
        
        // Шрифт
        public double titleFontSizeMultiplier = 0.03;
        
        // Отступы
        public double menuPadding = 50.0;
        public double menuSpacing = 10.0;
        
        // Предзагрузка
        public long preloadTimeoutMs = 5000;
        public long preloadCheckIntervalMs = 50;
        
        // Заголовок
        public String titleText = "LookQuest";
        public String titleColor = "brown";
        public String titleWeight = "bold";
    }
}

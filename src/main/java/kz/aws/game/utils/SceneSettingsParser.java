package kz.aws.game.utils;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Парсер для настроек сцен из XML
 */
public class SceneSettingsParser {
    private static SceneSettings settings;
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        
        try {
            File xmlFile = new File("lib/config/UI/SceneSettings.xml");
            if (!xmlFile.exists()) {
                System.err.println("SceneSettings config not found: " + xmlFile.getAbsolutePath());
                // Используем значения по умолчанию
                settings = new SceneSettings();
                initialized = true;
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            settings = new SceneSettings();
            
            // Парсим настройки TableDetail
            NodeList tableDetailList = document.getElementsByTagName("table-detail");
            if (tableDetailList.getLength() > 0) {
                Element tableDetail = (Element) tableDetailList.item(0);
                
                // Путь к изображению
                settings.tableImagePath = getTagValue(tableDetail, "table-image");
                
                // Размеры таблички
                NodeList sizeList = tableDetail.getElementsByTagName("table-size");
                if (sizeList.getLength() > 0) {
                    Element size = (Element) sizeList.item(0);
                    settings.tableWidthMultiplier = parseDouble(getTagValue(size, "width-multiplier"), 1.45);
                    settings.tableHeightMultiplier = parseDouble(getTagValue(size, "height-multiplier"), 0.115);
                }
                
                // Позиционирование таблички
                NodeList positionList = tableDetail.getElementsByTagName("table-position");
                if (positionList.getLength() > 0) {
                    Element position = (Element) positionList.item(0);
                    settings.tableBottomMargin = parseDouble(getTagValue(position, "bottom-margin"), 0.01);
                }
                
                // Настройки текста
                NodeList textList = tableDetail.getElementsByTagName("text");
                if (textList.getLength() > 0) {
                    Element text = (Element) textList.item(0);
                    settings.textWrappingWidthMultiplier = parseDouble(getTagValue(text, "wrapping-width-multiplier"), 0.65);
                    settings.dialogFontSizeMultiplier = parseDouble(getTagValue(text, "dialog-font-size-multiplier"), 0.025);
                    settings.nameFontSizeMultiplier = parseDouble(getTagValue(text, "name-font-size-multiplier"), 0.030);
                    
                    settings.textPaddingTop = parseDouble(getTagValue(text, "text-padding-top"), 0.03);
                    settings.textPaddingLeft = parseDouble(getTagValue(text, "text-padding-left"), 0.09);
                    settings.textContentWidth = parseDouble(getTagValue(text, "text-content-width"), 0.60);
                }

                // Настройки градиентной панели
                NodeList gradientList = document.getElementsByTagName("gradient-panel");
                if (gradientList.getLength() > 0) {
                    Element gradient = (Element) gradientList.item(0);
                    String useVal = getTagValue(gradient, "use-gradient");
                    settings.useGradientPanel = Boolean.parseBoolean(useVal);
                    settings.gradientPanelHeightMultiplier = parseDouble(getTagValue(gradient, "height-multiplier"), 0.25);
                    settings.gradientStartColor = getTagValue(gradient, "color-start");
                    settings.gradientEndColor = getTagValue(gradient, "color-end");
                }
                
                // Настройки кнопок
                NodeList buttonsList = tableDetail.getElementsByTagName("buttons");
                if (buttonsList.getLength() > 0) {
                    Element buttons = (Element) buttonsList.item(0);
                    settings.buttonSpacingMultiplier = parseDouble(getTagValue(buttons, "spacing-multiplier"), 0.01);
                    settings.buttonPaddingMultiplier = parseDouble(getTagValue(buttons, "padding-multiplier"), 0.015);
                    settings.buttonWidthMultiplier = parseDouble(getTagValue(buttons, "width-multiplier"), 0.07);
                    settings.buttonHeightMultiplier = parseDouble(getTagValue(buttons, "height-multiplier"), 0.05);
                    settings.buttonFontSizeMultiplier = parseDouble(getTagValue(buttons, "font-size-multiplier"), 0.015);
                    settings.menuBottomMargin = parseDouble(getTagValue(buttons, "menu-bottom-margin"), 0.027);
                }
            }
            
            // Парсим настройки DialogChoices
            NodeList choicesList = document.getElementsByTagName("dialog-choices");
            if (choicesList.getLength() > 0) {
                Element choices = (Element) choicesList.item(0);
                settings.choicesXPosition = parseDouble(getTagValue(choices, "x-position"), 0.65);
                settings.choicesYPosition = parseDouble(getTagValue(choices, "y-position"), 0.35);
                settings.choicesWidthMultiplier = parseDouble(getTagValue(choices, "width-multiplier"), 0.30);
                settings.choicesSpacing = parseDouble(getTagValue(choices, "spacing"), 8);
                settings.choicesFontSizeMultiplier = parseDouble(getTagValue(choices, "font-size-multiplier"), 0.020);
                settings.choicesButtonHeightMultiplier = parseDouble(getTagValue(choices, "button-height-multiplier"), 0.055);
            }

            // Парсим настройки SceneSelection
            NodeList sceneSelectionList = document.getElementsByTagName("scene-selection");
            if (sceneSelectionList.getLength() > 0) {
                Element sceneSelection = (Element) sceneSelectionList.item(0);
                NodeList scrollPaneList = sceneSelection.getElementsByTagName("scroll-pane");
                if (scrollPaneList.getLength() > 0) {
                    Element scrollPane = (Element) scrollPaneList.item(0);
                    settings.scrollPaneMaxHeightMultiplier = parseDouble(getTagValue(scrollPane, "max-height-multiplier"), 0.5);
                }
            }
            
            initialized = true;
        } catch (Exception e) {
            System.err.println("Error parsing SceneSettings config: " + e.getMessage());
            e.printStackTrace();
            // Используем значения по умолчанию
            settings = new SceneSettings();
            initialized = true;
        }
    }

    private static String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent().trim();
        }
        return null;
    }

    private static double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static SceneSettings getSettings() {
        if (!initialized) initialize();
        return settings;
    }

    /**
     * Класс настроек сцен
     */
    public static class SceneSettings {
        // TableDetail
        public String tableImagePath = "file:lib/Scene/table 8.png";
        public double tableWidthMultiplier = 1.45;
        public double tableHeightMultiplier = 0.115;
        public double tableBottomMargin = 0.01;

        // Градиентная панель
        public boolean useGradientPanel = false;
        public double gradientPanelHeightMultiplier = 0.25;
        public String gradientStartColor = "rgba(0,0,0,0)";
        public String gradientEndColor = "rgba(0,0,0,0.9)";
        
        // Текст
        public double textWrappingWidthMultiplier = 0.65;
        public double dialogFontSizeMultiplier = 0.025;
        public double nameFontSizeMultiplier = 0.030;
        
        // Новые настройки
        public double textPaddingTop = 0.03;
        public double textPaddingLeft = 0.09;
        public double textContentWidth = 0.60;
        
        // Кнопки
        public double buttonSpacingMultiplier = 0.01;
        public double buttonPaddingMultiplier = 0.015;
        public double buttonWidthMultiplier = 0.07;
        public double buttonHeightMultiplier = 0.05;
        public double buttonFontSizeMultiplier = 0.015; // Размер шрифта кнопок
        public double menuBottomMargin = 0.027;
        
        // DialogChoices (панель выбора)
        public double choicesXPosition = 0.65;
        public double choicesYPosition = 0.35;
        public double choicesWidthMultiplier = 0.30;
        public double choicesSpacing = 8;
        public double choicesFontSizeMultiplier = 0.020;
        public double choicesButtonHeightMultiplier = 0.055;

        // SceneSelection
        public double scrollPaneMaxHeightMultiplier = 0.5;
    }
}

package kz.aws.game.utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Общие операции чтения XML-конфигураций: загрузка документа,
 * извлечение значений тегов и безопасный разбор чисел.
 * Используется парсерами конфигов в {@code kz.aws.game.utils}.
 */
public final class XmlConfigSupport {

    private XmlConfigSupport() {
    }

    /**
     * Загружает и нормализует XML-документ.
     *
     * @param path путь к файлу конфигурации
     * @return документ или null, если файл отсутствует либо не разбирается
     */
    public static Document loadDocument(String path) {
        File file = ResourceLocator.file(path);
        if (!file.isFile()) {
            System.err.println("Конфигурация не найдена: " + path);
            return null;
        }
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception e) {
            System.err.println("Конфигурация не разобрана (" + path + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Возвращает текст первого вложенного тега с указанным именем.
     *
     * @param element родительский элемент
     * @param tagName имя вложенного тега
     * @return текст без окружающих пробелов или null, если тега нет
     */
    public static String getTagValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) return null;
        Node node = nodeList.item(0);
        return node.getTextContent().trim();
    }

    /**
     * Возвращает первый элемент с указанным именем тега.
     *
     * @param document документ
     * @param tagName  имя тега
     * @return элемент или null, если тега нет
     */
    public static Element firstElement(Document document, String tagName) {
        NodeList list = document.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;
        return (Element) list.item(0);
    }

    /**
     * Разбирает дробное число со значением по умолчанию.
     *
     * @param value        строка значения (может быть null)
     * @param defaultValue значение по умолчанию
     * @return разобранное число или defaultValue
     */
    public static double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Разбирает целое число со значением по умолчанию.
     *
     * @param value        строка значения (может быть null)
     * @param defaultValue значение по умолчанию
     * @return разобранное число или defaultValue
     */
    public static int parseInt(String value, int defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Разбирает длинное целое число со значением по умолчанию.
     *
     * @param value        строка значения (может быть null)
     * @param defaultValue значение по умолчанию
     * @return разобранное число или defaultValue
     */
    public static long parseLong(String value, long defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

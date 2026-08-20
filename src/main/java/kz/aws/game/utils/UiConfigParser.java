package kz.aws.game.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiConfigParser {

    private static final Logger LOG = LoggerFactory.getLogger(UiConfigParser.class);
    private static final Map<String, ButtonConfig> buttons = new HashMap<>();
    /** Порядок кнопок по контексту (как в XML). */
    private static final List<ButtonConfig> buttonsByContextOrder = new ArrayList<>();
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        
        try {
            // Updated path to match structure: lib/config/UI/Buttons.xml
            File xmlFile = ResourceLocator.file("lib/config/UI/Buttons.xml");
            if (!xmlFile.exists()) {
                LOG.error("UI Config not found: " + xmlFile.getAbsolutePath());
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // Читаем дефолтные значения из <defaults> (если есть)
            String defaultHoverSound = null;
            String defaultClickSound = null;
            String defaultStyleClass = null;
            NodeList defaultsList = document.getElementsByTagName("defaults");
            if (defaultsList.getLength() > 0) {
                Element defaults = (Element) defaultsList.item(0);
                defaultHoverSound = getTagValue(defaults, "sound-hover");
                defaultClickSound = getTagValue(defaults, "sound-click");
                defaultStyleClass = getTagValue(defaults, "style-class");
            }

            NodeList list = document.getElementsByTagName("button");

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String id = elem.getAttribute("id");
                    String text = elem.getAttribute("text");
                    String context = elem.getAttribute("context");
                    String visibleWhen = elem.getAttribute("visible-when");
                    String excludeIn = elem.getAttribute("exclude-in");

                    String hoverSound = getTagValue(elem, "sound-hover");
                    String clickSound = getTagValue(elem, "sound-click");
                    String styleClass = getTagValue(elem, "style-class");

                    // Подставляем дефолты, если у кнопки нет своих значений
                    if (hoverSound == null) hoverSound = defaultHoverSound;
                    if (clickSound == null) clickSound = defaultClickSound;
                    if (styleClass == null) styleClass = defaultStyleClass;

                    ButtonConfig config = new ButtonConfig(id, text, context, visibleWhen, excludeIn, hoverSound, clickSound, styleClass);
                    buttons.put(id, config);
                    buttonsByContextOrder.add(config);
                }
            }
            initialized = true;
        } catch (Exception e) {
        }
    }

    /**
     * Возвращает текст вложенного тега.
     * Значение обрезается по краям — раньше здесь терялся trim,
     * и пробелы в конфиге попадали в пути к ресурсам и стили.
     *
     * @param element родительский элемент
     * @param tagName имя тега
     * @return текст или null
     */
    private static String getTagValue(Element element, String tagName) {
        return XmlConfigSupport.getTagValue(element, tagName);
    }

    public static ButtonConfig getButtonConfig(String id) {
        if (!initialized) initialize();
        return buttons.get(id);
    }

    /**
     * Возвращает кнопки для заданного контекста в порядке, как в XML.
     */
    public static List<ButtonConfig> getButtonsByContext(String context) {
        if (!initialized) initialize();
        List<ButtonConfig> result = new ArrayList<>();
        for (ButtonConfig cfg : buttonsByContextOrder) {
            if (context != null && context.equals(cfg.context)) result.add(cfg);
        }
        return result;
    }

    /**
     * Кнопки для контекста и экрана с учётом exclude-in и visible-when.
     * viewId — экран (например "main-menu-root"); visibleWhenResolver — проверка условия по имени (например "hasSaveFiles" → true/false).
     */
    public static List<ButtonConfig> getButtonsForView(String context, String viewId, Function<String, Boolean> visibleWhenResolver) {
        if (!initialized) initialize();
        List<ButtonConfig> result = new ArrayList<>();
        for (ButtonConfig cfg : buttonsByContextOrder) {
            if (context != null && !context.equals(cfg.context)) continue;
            if (viewId != null && cfg.isExcludedIn(viewId)) continue;
            if (cfg.visibleWhen != null && !cfg.visibleWhen.isEmpty() && visibleWhenResolver != null && !Boolean.TRUE.equals(visibleWhenResolver.apply(cfg.visibleWhen))) continue;
            result.add(cfg);
        }
        return result;
    }

    public static class ButtonConfig {
        public final String id;
        public final String text;
        public final String context;
        /** Условие показа, например "hasSaveFiles". Пусто — всегда показывать. */
        public final String visibleWhen;
        /** Не показывать в экране (через запятую), например "main-menu-root". Пусто — показывать везде в контексте. */
        public final String excludeIn;
        public final String hoverSound;
        public final String clickSound;
        public final String styleClass;

        public ButtonConfig(String id, String text, String context, String visibleWhen, String excludeIn,
                String hoverSound, String clickSound, String styleClass) {
            this.id = id;
            this.text = text;
            this.context = context == null ? "" : context;
            this.visibleWhen = visibleWhen == null ? "" : visibleWhen.trim();
            this.excludeIn = excludeIn == null ? "" : excludeIn.trim();
            this.hoverSound = hoverSound;
            this.clickSound = clickSound;
            this.styleClass = styleClass;
        }

        /** Показывать ли кнопку в экране viewId. viewId = "main-menu-root" для корневого главного меню. */
        public boolean isExcludedIn(String viewId) {
            if (excludeIn.isEmpty() || viewId == null) return false;
            for (String part : excludeIn.split(",")) {
                if (part.trim().equals(viewId)) return true;
            }
            return false;
        }
    }
}

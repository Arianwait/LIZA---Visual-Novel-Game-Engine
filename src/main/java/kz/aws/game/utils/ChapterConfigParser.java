package kz.aws.game.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChapterConfigParser {
    private static List<Chapter> chapters = new ArrayList<>();
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        
        try {
            // Path updated to match file location: lib/config/Chapters.xml
            File xmlFile = new File("lib/config/Chapters.xml");
            if (!xmlFile.exists()) {
                System.err.println("Chapter Config not found: " + xmlFile.getAbsolutePath());
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            NodeList list = document.getElementsByTagName("chapter");

            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    int id = Integer.parseInt(elem.getAttribute("id"));
                    String name = elem.getAttribute("name");
                    int sceneId = Integer.parseInt(elem.getAttribute("sceneId"));
                    
                    Chapter chapter = new Chapter(id, name, sceneId);
                    
                    // Parse random choices
                    NodeList choicesNode = elem.getElementsByTagName("random-choices");
                    if (choicesNode.getLength() > 0) {
                        Element choicesElem = (Element) choicesNode.item(0);
                        NodeList groups = choicesElem.getElementsByTagName("choice-group");
                        
                        for (int j = 0; j < groups.getLength(); j++) {
                            Element groupElem = (Element) groups.item(j);
                            NodeList options = groupElem.getElementsByTagName("option");
                            List<String> groupOptions = new ArrayList<>();
                            
                            for (int k = 0; k < options.getLength(); k++) {
                                groupOptions.add(options.item(k).getTextContent());
                            }
                            chapter.choiceGroups.add(groupOptions);
                        }
                    }
                    
                    chapters.add(chapter);
                }
            }
            initialized = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Chapter> getChapters() {
        if (!initialized) initialize();
        return chapters;
    }

    public static class Chapter {
        public int id;
        public String name;
        public int sceneId;
        public List<List<String>> choiceGroups = new ArrayList<>();

        public Chapter(int id, String name, int sceneId) {
            this.id = id;
            this.name = name;
            this.sceneId = sceneId;
        }
        
        public List<String> generateRandomChoices() {
            List<String> selected = new ArrayList<>();
            Random random = new Random();
            for (List<String> group : choiceGroups) {
                if (!group.isEmpty()) {
                    selected.add(group.get(random.nextInt(group.size())));
                }
            }
            return selected;
        }
    }
}

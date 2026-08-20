package kz.aws.game.engine.resources;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import kz.aws.game.utils.ResourceLocator;

public class CharacterLibrary {
    // Map<CharacterName, Map<PoseName, FilePath>>
    private static final Map<String, Map<String, String>> characterPoses = new HashMap<>();
    /** Map<CharacterName, ColorString> — цвет имени из Person.xml (атрибут Color). */
    private static final Map<String, String> characterColors = new HashMap<>();
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        
        try {
            File xmlFile = ResourceLocator.file("lib/Scene/Person.xml");
            if (!xmlFile.exists()) {
                System.err.println("Person.xml not found at " + xmlFile.getAbsolutePath());
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            NodeList personList = document.getElementsByTagName("Person");

            for (int i = 0; i < personList.getLength(); i++) {
                Node personNode = personList.item(i);

                if (personNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element personElement = (Element) personNode;
                    String personName = personElement.getAttribute("name");
                    String personColor = personElement.getAttribute("Color");
                    if (personColor != null && !personColor.isEmpty()) {
                        characterColors.put(personName, personColor);
                    }
                    
                    Map<String, String> poses = new HashMap<>();
                    NodeList poseList = personElement.getElementsByTagName("Pose");
                    
                    for (int j = 0; j < poseList.getLength(); j++) {
                         Element poseElement = (Element) poseList.item(j);
                         String poseName = poseElement.getAttribute("name");
                         String src = poseElement.getAttribute("src");
                         poses.put(poseName, src);
                    }
                    
                    characterPoses.put(personName, poses);
                }
            }
            initialized = true;
            System.out.println("CharacterLibrary initialized with " + characterPoses.size() + " characters.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPosePath(String characterName, String poseName) {
        if (!initialized) initialize();
        
        Map<String, String> poses = characterPoses.get(characterName);
        if (poses != null) {
            String path = poses.get(poseName);
            if (path != null) {
                return "file:" + path;
            }
        }
        return null;
    }
    
    public static boolean hasCharacter(String name) {
        if (!initialized) initialize();
        return characterPoses.containsKey(name);
    }

    /** Цвет имени персонажа из Person.xml; null, если не задан. */
    public static String getCharacterColor(String name) {
        if (!initialized) initialize();
        return characterColors.get(name);
    }
}

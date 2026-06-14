package kz.aws.game.character;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PersonCreaterList {
	public static List <ICharacter> listCreator(){
		List <ICharacter> iCharacterList = new ArrayList<ICharacter>();
        try {
            File xmlFile = new File("lib/Scene/Person.xml");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            NodeList personList = document.getElementsByTagName("Person");

            for (int i = 0; i < personList.getLength(); i++) {
                Node personNode = personList.item(i);

                if (personNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element personElement = (Element) personNode;
                    String personName = personElement.getAttribute("name");
                    Elein elein = new Elein(personName);
                    iCharacterList.add(elein);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		return iCharacterList;
	}
}

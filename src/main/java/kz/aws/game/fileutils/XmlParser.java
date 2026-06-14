package kz.aws.game.fileutils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XmlParser {
	public static Document parseXML() {
		Document doc = null;
		try {
            File file = new File("lib/Scene/Dialog.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();


		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return doc;

	}
	
	   public static Element findDialogById(Document doc, int id) {
	        NodeList dialogList = doc.getElementsByTagName("dialog");

	        for (int i = 0; i < dialogList.getLength(); i++) {
	            Node node = dialogList.item(i);

	            if (node.getNodeType() == Node.ELEMENT_NODE) {
	                Element dialogElement = (Element) node;
	                int dialogId = Integer.parseInt(dialogElement.getAttribute("id"));

	                if (dialogId == id) {
	                    return dialogElement; // Нашли элемент с нужным ID
	                }
	            }
	        }

	        return null; // Элемент с нужным ID не найден
	    }
}

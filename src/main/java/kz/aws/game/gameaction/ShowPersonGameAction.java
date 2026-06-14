package kz.aws.game.gameaction;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import kz.aws.game.character.ICharacter;

/**
 * Команда показа персонажа на сцене с указанной позой (загрузка из Person.xml).
 */
@GameAction("showPerson")
public final class ShowPersonGameAction implements GameActionHandler {

    @Override
    public void run(GameActionContext ctx) {
        String poseName = ctx.getValue();
        if (poseName == null || poseName.isEmpty() || ctx.getAppSettings() == null
                || ctx.getAppSettings().getScene() == null || ctx.getRoot() == null) return;
        ICharacter character = ctx.findCharacter();
        if (character == null) return;

        String imagePath = findPoseImagePath(ctx.getTarget(), poseName);
        if (imagePath == null) return;

        StackPane root = ctx.getRoot();
        ImageView oldView = character.getCharacterImageView();
        if (oldView != null) root.getChildren().remove(oldView);

        Image image = new Image(imagePath);
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(ctx.getAppSettings().getScene().getWidth() * 0.8);
        view.setFitHeight(ctx.getAppSettings().getScene().getHeight() * 0.8);

        character.setCharacterImageView(view);
        root.getChildren().add(view);
        StackPane.setAlignment(view, Pos.BOTTOM_CENTER);
        view.toBack();
    }

    private static String findPoseImagePath(String characterName, String poseName) {
        try {
            File inputFile = new File("lib/Scene/Person.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            XPath xpath = XPathFactory.newInstance().newXPath();
            String expr = "/Persons/Person[@name='" + characterName + "']";
            Node personNode = (Node) xpath.compile(expr).evaluate(doc, XPathConstants.NODE);
            if (personNode == null) return null;

            NodeList poseList = ((Element) personNode).getElementsByTagName("Pose");
            for (int i = 0; i < poseList.getLength(); i++) {
                Node poseNode = poseList.item(i);
                if (poseNode.getNodeType() != Node.ELEMENT_NODE) continue;
                Element poseElement = (Element) poseNode;
                if (poseName.equals(poseElement.getAttribute("name"))) {
                    String src = poseElement.getAttribute("src");
                    return src != null && !src.isEmpty() ? "file:" + src : null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

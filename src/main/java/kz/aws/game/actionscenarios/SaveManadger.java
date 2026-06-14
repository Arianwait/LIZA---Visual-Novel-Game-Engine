package kz.aws.game.actionscenarios;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import kz.aws.game.scenelist.GameData;

public class SaveManadger {
	  // Метод для сериализации значения Clicker
    public static void serializeClicker(GameData gameInfo, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(gameInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для десериализации значения Clicker
    public static GameData deserializeClicker(String filePath) {
        	if(filePath != null) {
        		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("save/" + filePath))) {
        			return (GameData) ois.readObject();
        		} catch (IOException | ClassNotFoundException e) {
        			e.printStackTrace();
        		}
        	}
        return null;
    }
}

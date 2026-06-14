package kz.aws.game.filereader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileReading  implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -7207300461082592948L;
    private static String encoding = "UTF-8";
    public static List<String> readLinesFromFile(String filePath) {
    	
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}


package kz.aws.game.soundtrack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;
import kz.aws.game.utils.ResourceLocator;

public class SoundManager {
    private static final Map<String, AudioClip> clips = new HashMap<>();

    // Preload common sounds
    static {
        // You can add default sounds here if file paths are known
        // loadSound("hover", "lib/sound/hover.wav"); 
        // loadSound("click", "lib/sound/click.wav");
    }

    public static void loadSound(String name, String path) {
        try {
            File file = ResourceLocator.file(path);
            if (file.exists()) {
                AudioClip clip = new AudioClip(file.toURI().toString());
                clips.put(name, clip);
            } else {
                System.err.println("Sound file not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void playSound(String name) {
        AudioClip clip = clips.get(name);
        if (clip != null) {
            // Stop logic if needed, but for SFX overlapping is usually fine
            clip.play();
        }
    }
    
    public static void playHover() {
        playSound("hover");
    }
    
    public static void playClick() {
        playSound("click");
    }
    
    // Voiceover support
    private static AudioClip currentVoiceClip;
    
    public static void playVoice(String path) {
        if (path == null || path.isEmpty()) return;
        
        // Stop previous voice if playing
        if (currentVoiceClip != null) {
            currentVoiceClip.stop();
        }
        
        try {
            // Normalize path
            String normalizedPath = path.replace("\\", "/");
            String fullPath = normalizedPath.startsWith("file:") ? normalizedPath : "file:" + normalizedPath;
            
            // Check if file exists (optional, AudioClip handles it but throws)
            // Ideally we cache voices too if they are reused, but usually they are unique per line.
            // We can use a separate cache or just load on demand.
            // Given the potentially large number of unique voice lines, maybe just load on demand.
            
            currentVoiceClip = new AudioClip(fullPath);
            currentVoiceClip.play();
            
        } catch (Exception e) {
            System.err.println("Failed to play voice: " + path + " (" + e.getMessage() + ")");
        }
    }
    
    public static void stopVoice() {
        if (currentVoiceClip != null) {
            currentVoiceClip.stop();
            currentVoiceClip = null;
        }
    }
}

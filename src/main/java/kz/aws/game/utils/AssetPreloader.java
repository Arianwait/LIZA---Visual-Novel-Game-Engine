package kz.aws.game.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import kz.aws.game.engine.model.CharacterState;
import kz.aws.game.engine.model.SceneFrame;
import kz.aws.game.engine.model.VisualState;
import kz.aws.game.engine.parser.SceneXmlParser;
import kz.aws.game.engine.render.SceneRenderer;
import kz.aws.game.engine.resources.CharacterLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to preload assets for game scenes.
 * Scans parsed scenes for image paths and loads them into SceneRenderer's cache.
 */
public class AssetPreloader extends Task<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(AssetPreloader.class);

    private Map<Integer, List<SceneFrame>> allScenes; // Not final anymore
    //private final int startSceneId;
    // Optional: Limit how many scenes to look ahead. -1 for all.
    //private final int lookAheadLimit; 

    public AssetPreloader(int startSceneId) {
        // Move parsing to call()
        //this.startSceneId = startSceneId;
        //this.lookAheadLimit = -1; // Load everything by default for now
    }

    @Override
    protected Void call() throws Exception {
        updateMessage("Parsing scenes...");
        // HEAVY OPERATION MOVED HERE
        this.allScenes = SceneXmlParser.parseAllScenes(); 
        
        updateMessage("Analyzing assets...");
        Set<String> pathsToLoad = new HashSet<>();
        
        // For optimization: Let's iterate ALL loaded scenes
        for (Map.Entry<Integer, List<SceneFrame>> entry : allScenes.entrySet()) {
            for (SceneFrame frame : entry.getValue()) {
                VisualState vs = frame.getVisualState();
                
                // Background
                if (vs.getBackgroundPath() != null) {
                    pathsToLoad.add(formatPath(vs.getBackgroundPath()));
                }
                
                // Characters
                if (vs.getCharacters() != null) {
                    for (CharacterState cs : vs.getCharacters().values()) {
                        String posePath = CharacterLibrary.getPosePath(cs.getName(), cs.getPose());
                        if (posePath != null) {
                            pathsToLoad.add(formatPath(posePath));
                        }
                    }
                }
            }
            // processedScenes++;
            // updateProgress(processedScenes, totalScenes * 2); // Split progress between analysis and loading
        }

        updateMessage("Loading " + pathsToLoad.size() + " assets...");
        
        int totalAssets = pathsToLoad.size();
        int loadedAssets = 0;

        for (String path : pathsToLoad) {
            try {
                // We use SceneRenderer's cache directly. 
                // Since Image loading in JavaFX constructor is synchronous unless bgLoading=true,
                // we use bgLoading=false here because we ARE in a background thread (Task).
                // If we used bgLoading=true, this loop would finish instantly but images wouldn't be ready.
                
                // However, creating new Image() must be careful about Toolkit. 
                // Images can be created in background threads.
                
                Image img = new Image(path, 0, 0, true, true, false); // bgLoading = false, sync load
                
                // Trigger loading by accessing properties if needed, but 'new Image' with bg=false blocks until headers are read at least.
                // To fully force load, we might need a stream, but JavaFX Image is smart.
                // Actually, passing bgLoading=true is safer for UI responsiveness if we want to cancel,
                // but we want to wait here.
                
                SceneRenderer.addToCache(path, img); // Need to expose this method
                
            } catch (Exception e) {
                LOG.error("Failed to preload: " + path);
            }
            
            loadedAssets++;
            updateProgress(loadedAssets, totalAssets);
            updateMessage("Loading assets: " + loadedAssets + "/" + totalAssets);
            
            if (isCancelled()) break;
        }

        return null;
    }
    
    private String formatPath(String path) {
        return path.startsWith("file:") ? path : "file:" + path;
    }
}

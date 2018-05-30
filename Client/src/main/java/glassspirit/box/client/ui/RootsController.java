package glassspirit.box.client.ui;

import glassspirit.box.logging.SpiritLogger;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class RootsController {

    private static String currentRoot;
    private static Scene mainScene;

    private static HashMap<String, Parent> roots = new HashMap<>();

    public static HashMap<String, Parent> getRoots() {
        return roots;
    }

    public static void addRoot(String name, URL location) {
        FXMLLoader loader = new FXMLLoader(location);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        roots.put(name, root);
    }

    public static void setMainScene(Scene scene) {
        mainScene = scene;
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static Parent getRoot(String key) {
        return roots.get(key);
    }

    public static void setRoot(String key) {
        if (roots.containsKey(key)) {
            currentRoot = key;
            mainScene.setRoot(getRoot(key));
        }
    }

    public static void addTextToOutput(String string) {
        SpiritLogger.getLogger().info(string);
        if (currentRoot.equals("chat")) {
            ChatController.instance.outArea.appendText(string + "\n");
        } else if (currentRoot.equals("auth")) {
            AuthController.instance.logArea.appendText(string + "\n");
        }
    }

    public static void clearOutput() {
        if (currentRoot.equals("chat")) {
            ChatController.instance.outArea.clear();
        } else if (currentRoot.equals("auth")) {
            AuthController.instance.logArea.clear();
        }
    }

    public static void moveToChat() {
        if (mainScene.getRoot() == getRoot("chat"))
            return;
        FadeTransition fo = new FadeTransition(
                Duration.millis(500),
                RootsController.mainScene.getRoot()
        );
        fo.setFromValue(1.0);
        fo.setToValue(0.0);
        fo.play();
        fo.setOnFinished(event -> {
            fo.getNode().setOpacity(1.0);
            ChatController.instance.outArea.clear();
            RootsController.setRoot("chat");
        });
    }

    public static void moveToAuth() {
        if (mainScene.getRoot() == getRoot("auth"))
            return;
        FadeTransition fo = new FadeTransition(
                Duration.millis(500),
                RootsController.mainScene.getRoot()
        );
        fo.setFromValue(1.0);
        fo.setToValue(0.0);
        fo.play();
        fo.setOnFinished(event -> {
            fo.getNode().setOpacity(1.0);
            AuthController.instance.logArea.clear();
            RootsController.setRoot("auth");
        });
    }

}

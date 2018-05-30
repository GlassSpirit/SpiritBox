package glassspirit.box.client;

import glassspirit.box.client.ui.RootsController;
import glassspirit.box.logging.SpiritLogger;
import glassspirit.box.properties.SpiritProperties;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SpiritBoxClient extends Application {

    public static void main(String[] args) {
        SpiritLogger.getLogger().info("Запуск клиента");
        launch(args);
    }

    private static BoxConnection connection;

    @Override
    public void start(Stage primaryStage) {
        new SpiritProperties().load();

        RootsController.addRoot("auth", getClass().getClassLoader().getResource("auth.fxml"));
        RootsController.addRoot("chat", getClass().getClassLoader().getResource("chat.fxml"));

        Scene scene = new Scene(RootsController.getRoot("auth"),
                SpiritProperties.getInteger("width", 800),
                SpiritProperties.getInteger("height", 600));

        RootsController.setMainScene(scene);
        RootsController.setRoot("auth");

        primaryStage.setTitle("SpiritBox");
        primaryStage.setScene(RootsController.getMainScene());
        primaryStage.setMinWidth(SpiritProperties.getInteger("min_width", 600));
        primaryStage.setMinHeight(SpiritProperties.getInteger("min_height", 600));

        primaryStage.show();

        connection = new BoxConnection();
    }

    public static BoxConnection getConnection() {
        return connection;
    }
}

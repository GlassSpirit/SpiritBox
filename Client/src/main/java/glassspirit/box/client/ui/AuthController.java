package glassspirit.box.client.ui;

import glassspirit.box.client.SpiritBoxClient;
import glassspirit.box.command.Command;
import glassspirit.box.properties.SpiritProperties;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {

    public static AuthController instance;

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField connectionField;
    @FXML
    public TextArea logArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        connectionField.setText(SpiritProperties.getString("hostname", "localhost") + ":" + SpiritProperties.getInteger("port", 8888));
    }

    public void loginButton(MouseEvent mouseEvent) {
        if (SpiritBoxClient.getConnection().isConnected()) {
            SpiritBoxClient.getConnection().sendString(new Command(Command.LOGIN, loginField.getText() + " " + passwordField.getText()).toString());
        } else {
            RootsController.addTextToOutput("Нет соединения!");
        }
    }

    public void registerButton(MouseEvent mouseEvent) {
        if (SpiritBoxClient.getConnection().isConnected()) {
            SpiritBoxClient.getConnection().sendString(new Command(Command.REGISTER, loginField.getText() + " " + passwordField.getText() + " " + loginField.getText()).toString());
        } else {
            RootsController.addTextToOutput("Нет соединения!");
        }
    }

    public void connectButton(MouseEvent mouseEvent) {
        if (SpiritBoxClient.getConnection().isConnected()) {
            RootsController.addTextToOutput("Соединение уже установлено!");
            return;
        }
        String[] data = connectionField.getText().split(":");
        SpiritBoxClient.getConnection().connect(data[0], Integer.parseInt(data[1]));
    }
}

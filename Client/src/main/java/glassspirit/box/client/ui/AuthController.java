package glassspirit.box.client.ui;

import glassspirit.box.client.SpiritBoxClient;
import glassspirit.box.command.Command;
import glassspirit.box.command.Commands;
import glassspirit.box.properties.SpiritProperties;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

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
    @FXML
    public Button connectButton;
    @FXML
    public Button disconnectButton;
    @FXML
    public Label connectLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        connectionField.setText(SpiritProperties.getString("hostname", "localhost") + ":" + SpiritProperties.getInteger("port", 8888));
    }

    public void loginButton() {
        if (SpiritBoxClient.getConnection().isConnected()) {
            SpiritBoxClient.getConnection().sendString(new Command(Commands.LOGIN, loginField.getText() + " " + passwordField.getText()).toString());
        } else {
            RootsController.addTextToOutput("Нет соединения!");
        }
    }

    public void registerButton() {
        if (SpiritBoxClient.getConnection().isConnected()) {
            SpiritBoxClient.getConnection().sendString(new Command(Commands.REGISTER,
                    loginField.getText() + " " + passwordField.getText() + " " + loginField.getText()).toString());
        } else {
            RootsController.addTextToOutput("Нет соединения!");
        }
    }

    public void connectButton() {
        if (SpiritBoxClient.getConnection().isConnected()) {
            RootsController.addTextToOutput("Соединение уже установлено!");
            return;
        }
        String[] data = connectionField.getText().split(":");
        SpiritBoxClient.getConnection().connect(data[0], Integer.parseInt(data[1]));
    }

    public void disconnectButton() {
        if (!SpiritBoxClient.getConnection().isConnected()) {
            RootsController.addTextToOutput("Соединение еще не установлено!");
            return;
        }
        connectionField.setVisible(true);
        connectButton.setVisible(true);
        disconnectButton.setVisible(false);
        connectLabel.setVisible(false);
        SpiritBoxClient.getConnection().disconnect();
        checkButtonStates();
    }

    public void checkButtonStates() {
        if (SpiritBoxClient.getConnection().isConnected()) {
            connectionField.setVisible(false);
            connectButton.setVisible(false);

            disconnectButton.setVisible(true);
            connectLabel.setVisible(true);
        } else {
            connectionField.setVisible(true);
            connectButton.setVisible(true);

            disconnectButton.setVisible(false);
            connectLabel.setVisible(false);
        }
    }
}

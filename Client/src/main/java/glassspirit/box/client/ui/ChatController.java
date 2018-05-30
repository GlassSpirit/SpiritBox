package glassspirit.box.client.ui;

import glassspirit.box.client.BoxConnection;
import glassspirit.box.client.SpiritBoxClient;
import glassspirit.box.command.Command;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatController implements Initializable {

    public static ChatController instance;

    @FXML
    public TextArea outArea;

    @FXML
    public TextArea inArea;

    @FXML
    public Button commitButton;

    @FXML
    public ListView<String> currentUsers = new ListView<>();

    public static ObservableList<String> currentUsersList = FXCollections.observableArrayList();

    private Pattern pattern = Pattern.compile("\n*");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        currentUsers.setItems(currentUsersList);
    }

    public void commitField(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isShiftDown()) {
            inArea.appendText(System.lineSeparator());
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            commit();
            keyEvent.consume();
        }
    }

    public void commitButton() {
        commit();
        inArea.requestFocus();
    }

    public void logout() {
        SpiritBoxClient.getConnection().sendString(new Command(Command.LOGOUT, "").toString());
    }

    private void commit() {
        BoxConnection connection = SpiritBoxClient.getConnection();
        Matcher matcher = pattern.matcher(inArea.getText());

        if (!matcher.matches()) {
            connection.sendString(inArea.getText());
            inArea.clear();
        }
    }

    public void clickOnList(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            inArea.appendText("@" + currentUsers.getSelectionModel().getSelectedItem() + " ");
            inArea.requestFocus();
        }
    }
}

package glassspirit.box.client.ui;

import glassspirit.box.client.SpiritBoxClient;
import glassspirit.box.command.Command;
import glassspirit.box.command.Commands;
import glassspirit.box.model.BoxFile;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class BoxController implements Initializable {

    public static BoxController instance;

    @FXML
    TableView<BoxFile> tableView;

    @FXML
    TableColumn<BoxFile, String> columnFileName;

    @FXML
    TableColumn<BoxFile, String> columnFileSize;

    public ObservableList<BoxFile> fileList = FXCollections.observableArrayList();

    public Path currentDirectory = Paths.get("/");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        tableView.setItems(fileList);
        columnFileName.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFileName()));
        columnFileSize.setCellValueFactory(data -> new SimpleObjectProperty<>(formatFileSize(data.getValue().getFileSize())));
        SpiritBoxClient.getConnection().sendString(new Command(Commands.GET_FILES, currentDirectory.toString()).toString());
    }

    public void buttonRefresh() {
        SpiritBoxClient.getConnection().sendString(new Command(Commands.GET_FILES, currentDirectory.toString()).toString());
    }

    private String formatFileSize(long size) {
        if (size < 1024) return String.valueOf(size) + " байт";
        if (size < 1024 * 1024) return String.valueOf(size / 1024) + " КБ";
        return String.valueOf(size / 1024 / 1024) + " МБ";
    }

}

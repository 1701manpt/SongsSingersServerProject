package sgu.ltudm.songssingersserverproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    Text console;

    public void setConsole(String msg) {
        console.setText(" - " + console.getText() + "\n" + msg);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int port = 1071;
    }
}

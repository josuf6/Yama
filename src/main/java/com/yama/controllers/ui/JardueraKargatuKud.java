package com.yama.controllers.ui;

import com.yama.Main;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class JardueraKargatuKud implements Initializable {

    private Main mainApp;

    @FXML
    private Button btn_kargatu;

    public JardueraKargatuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @FXML
    void onClick(MouseEvent event) {
        aukeratuFitxategia();
    }

    private void aukeratuFitxategia() { //kargatu nahi den jardueraren fitxategia aukeratzeko
        FileChooser fc = new FileChooser();
        fc.setTitle("Aukeratu fitxategi bat");

        //baimendutako fitxategi motak murrizteko filtroak
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("GPX", "*.gpx")
        );

        //leiho bat ireki fitxategi bat aukeratzeko
        File fitxategia = fc.showOpenDialog(Main.getStage());

        //aukeratutako fitxategia kudeatu
        if (fitxategia != null && fitxategia.exists() && fitxategia.isFile()) {
            String ext = com.google.common.io.Files.getFileExtension(fitxategia.getName());

            if (ext.equals("gpx")) { //
                //TODO
                //TODO
                //TODO
            }
        }
    }
}

package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.files.GPXKud;
import com.yama.controllers.files.TCXKud;
import com.yama.models.JardueraModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class JardueraKargatuKud implements Initializable {

    private Main mainApp;

    private ArrayList<JardueraModel> jardZerr;
    private ObservableList<JardueraModel> jardZerrObs;

    @FXML
    private AnchorPane panela;

    @FXML
    private Button btn_kargatu;

    @FXML
    private TableView<JardueraModel> tbJard;

    @FXML
    private TableColumn<JardueraModel, String> name;

    @FXML
    private TableColumn<JardueraModel, String> time;

    @FXML
    private TableColumn<JardueraModel, String> type;

    @FXML
    private TableColumn<JardueraModel, String> duration;

    @FXML
    private TableColumn<JardueraModel, String> distance;

    public JardueraKargatuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jardZerr = new ArrayList<>();

        tbJard.setEditable(false);
        name.setResizable(false);
        name.setReorderable(false);
        time.setResizable(false);
        time.setReorderable(false);
        type.setResizable(false);
        type.setReorderable(false);
        duration.setResizable(false);
        duration.setReorderable(false);
        distance.setResizable(false);
        distance.setReorderable(false);

        name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        time.setCellValueFactory(new PropertyValueFactory<>("Time"));
        type.setCellValueFactory(new PropertyValueFactory<>("Type"));
        duration.setCellValueFactory(new PropertyValueFactory<>("Duration"));
        distance.setCellValueFactory(new PropertyValueFactory<>("Distance"));

        Platform.runLater(() -> panela.requestFocus());
    }

    @FXML
    void onClick(MouseEvent event) {
        aukeratuFitxategia();
    }

    @FXML
    void onClickTaula(MouseEvent event) {
        tbJard.requestFocus();
    }

    private void aukeratuFitxategia() { //Kargatu nahi den jardueraren fitxategia aukeratzeko
        FileChooser fc = new FileChooser();
        fc.setTitle("Aukeratu fitxategi bat");

        //Baimendutako fitxategi motak murrizteko filtroak
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("GPX", "*.gpx"),
                new FileChooser.ExtensionFilter("TCX", "*.tcx")
        );

        //Leiho bat ireki fitxategi bat aukeratzeko
        File fitxategia = fc.showOpenDialog(Main.getStage());

        //Aukeratutako fitxategia kudeatu
        if (fitxategia != null && fitxategia.exists() && fitxategia.isFile()) {
            fitxategiaAztertu(fitxategia);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ezin izan da fitxategia irakurri.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Errorea fitxategia irakurtzen.");
            alert.showAndWait();
        }
    }

    private void fitxategiaAztertu(File fitxategia) {
        String ext = com.google.common.io.Files.getFileExtension(fitxategia.getName());

        if (ext.equals("gpx")) { //Fitxategia gpx motakoa bada
            Thread haria = new Thread(() -> {
                desaktibatuFuntzionalitateak();
                ArrayList<JardueraModel> jardueraBerriak = GPXKud.getGPXKud().kudeatuGPX(fitxategia);

                Platform.runLater(() -> {
                    if (!jardueraBerriak.isEmpty()) {
                        jardueraBerriak.forEach(jardueraBerria -> {
                            jardZerr.add(jardueraBerria);
                            jardueraTaulaEguneratu();
                        });
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Ezin izan da jarduerarik lortu fitxategitik.", ButtonType.CLOSE);
                        alert.setTitle("Yama");
                        alert.setHeaderText("Errorea fitxategia aztertzen.");
                        alert.showAndWait();
                    }
                    aktibatuFuntzionalitateak();
                });
            });
            haria.start();
        } else if (ext.equals("tcx")) {//Fitxategia tcx motakoa bada
            Thread haria = new Thread(() -> {
                desaktibatuFuntzionalitateak();
                ArrayList<JardueraModel> jardueraBerriak = TCXKud.getTCXKud().kudeatuTCX(fitxategia);

                Platform.runLater(() -> {
                    if (!jardueraBerriak.isEmpty()) {
                        jardueraBerriak.forEach(jardueraBerria -> {
                            jardZerr.add(jardueraBerria);
                            jardueraTaulaEguneratu();
                        });
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Ezin izan da jarduerarik lortu fitxategitik.", ButtonType.CLOSE);
                        alert.setTitle("Yama");
                        alert.setHeaderText("Errorea fitxategia aztertzen.");
                        alert.showAndWait();
                    }
                    aktibatuFuntzionalitateak();
                });
            });
            haria.start();
        }
    }

    private void jardueraTaulaEguneratu() {
        jardZerrObs = FXCollections.observableArrayList(jardZerr);
        tbJard.setItems(jardZerrObs);
    }

    private void aktibatuFuntzionalitateak() {
        aktibatuJardKar();
        //TODO mainApp erabiliz beste pantailen funtzionalitateak aktibatu beharrezkoa bada
    }

    public void aktibatuJardKar() {
        btn_kargatu.setDisable(false);
    }

    private void desaktibatuFuntzionalitateak() {
        desaktibatuJardKar();
        //TODO mainApp erabiliz beste pantailen funtzionalitateak desaktibatu beharrezkoa bada
    }

    public void desaktibatuJardKar() {
        btn_kargatu.setDisable(true);
    }
}

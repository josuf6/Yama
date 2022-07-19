package com.yama.controllers.ui;

import com.garmin.fit.plugins.ActivityFileValidationResult;
import com.yama.Main;
import com.yama.controllers.files.FIT.FITKud;
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

    private int jardBistIndex;
    private ArrayList<JardueraModel> jardZerr;
    private ObservableList<JardueraModel> jardZerrObs;

    @FXML
    private Button btn_kargatu, btn_bistaratu, btn_ezabatu;

    @FXML
    private TableView<JardueraModel> tbJard;

    @FXML
    private TableColumn<JardueraModel, String> izena, hasiData, mota, iraupena, distantzia;

    public JardueraKargatuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jardZerr = new ArrayList<>();

        btn_bistaratu.setDisable(true);
        btn_ezabatu.setDisable(true);
        tbJard.setEditable(false);
        izena.setResizable(false);
        izena.setReorderable(false);
        hasiData.setResizable(false);
        hasiData.setReorderable(false);
        mota.setResizable(false);
        mota.setReorderable(false);
        iraupena.setResizable(false);
        iraupena.setReorderable(false);
        distantzia.setResizable(false);
        distantzia.setReorderable(false);

        izena.setCellValueFactory(new PropertyValueFactory<>("izena"));
        hasiData.setCellValueFactory(new PropertyValueFactory<>("hasiData"));
        mota.setCellValueFactory(new PropertyValueFactory<>("mota"));
        iraupena.setCellValueFactory(new PropertyValueFactory<>("iraupena"));
        distantzia.setCellValueFactory(new PropertyValueFactory<>("distantzia"));

        tbJard.setRowFactory(tbJard2 -> {
            final TableRow<JardueraModel> errenkada = new TableRow<>();
            errenkada.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                final int index = errenkada.getIndex();
                if (index < tbJard.getItems().size() && tbJard.getSelectionModel().isSelected(index)  ) {
                    tbJard.getSelectionModel().clearSelection();
                    btn_bistaratu.setDisable(true);
                    btn_ezabatu.setDisable(true);
                    event.consume();
                }
            });
            return errenkada;
        });
    }

    @FXML
    void onClickKargatu(MouseEvent event) {
        tbJard.getSelectionModel().clearSelection();
        btn_bistaratu.setDisable(true);
        btn_ezabatu.setDisable(true);
        aukeratuFitxategia();
    }

    @FXML
    void onClickBistaratu(MouseEvent event) {
        jardBistIndex = tbJard.getSelectionModel().getSelectedIndex();
        tbJard.getSelectionModel().clearSelection();
        btn_bistaratu.setDisable(true);
        btn_ezabatu.setDisable(true);
        JardueraModel jard = jardZerr.get(jardBistIndex);
        mainApp.jardBistaratu(jard);
    }

    @FXML
    void onClickEzabatu(MouseEvent event) {
        jardZerr.remove(tbJard.getSelectionModel().getSelectedIndex());
        tbJard.getSelectionModel().clearSelection();
        btn_bistaratu.setDisable(true);
        btn_ezabatu.setDisable(true);
        jardueraTaulaEguneratu();
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
                new FileChooser.ExtensionFilter("Kirol jarduera", "*.gpx", "*.tcx", "*.fit")
        );

        //Leiho bat ireki fitxategi bat aukeratzeko
        File fitxategia = fc.showOpenDialog(Main.getStage());

        //Aukeratutako fitxategia kudeatu
        if (fitxategia != null && fitxategia.exists() && fitxategia.isFile()) {
            fitxategiaAztertu(fitxategia);
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
        } else if (ext.equals("tcx")) { //Fitxategia tcx motakoa bada
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
        } else if (ext.equals("fit")) { //Fitxategia FIT motakoa bada
            Thread haria = new Thread(() -> {
                desaktibatuFuntzionalitateak();
                JardueraModel jardueraBerria = FITKud.getFITKud().kudeatuFIT(fitxategia);

                Platform.runLater(() -> {
                    if (jardueraBerria != null) {
                        jardZerr.add(jardueraBerria);
                        jardueraTaulaEguneratu();
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
        tbJard.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btn_bistaratu.setDisable(false);
            btn_ezabatu.setDisable(false);
        });
        tbJard.refresh();
    }

    public void eguneratuJarduera(JardueraModel jardEguneratuta) {
        jardZerr.set(jardBistIndex, jardEguneratuta);
        jardueraTaulaEguneratu();
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
        btn_bistaratu.setDisable(true);
        btn_ezabatu.setDisable(true);
    }
}

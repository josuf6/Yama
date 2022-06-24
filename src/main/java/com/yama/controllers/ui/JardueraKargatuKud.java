package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.GPXKud;
import com.yama.models.JardueraModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

            if (ext.equals("gpx")) { //fitxategia gpx motakoa bada
                ArrayList<JardueraModel> jardueraBerriak = GPXKud.getGPXKud().kudeatuGPX(fitxategia);

                //hau hobeto pentsatu (zerbait estandarragoa egin)
                if (!jardueraBerriak.isEmpty()) {
                    jardueraBerriak.forEach(jardueraBerria -> {
                        System.out.println(jardueraBerria);
                        jardZerr.add(jardueraBerria);
                        jardueraTaulaEguneratu();
                    });
                } else {
                    //TODO
                }
            }
        }
    }

    private void jardueraTaulaEguneratu() {
        jardZerrObs = FXCollections.observableArrayList(jardZerr);
        tbJard.setItems(jardZerrObs);
    }
}

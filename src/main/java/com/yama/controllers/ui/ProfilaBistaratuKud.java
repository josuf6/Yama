package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import com.yama.models.ErabiltzaileModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfilaBistaratuKud implements Initializable {

    private Main mainApp;
    private ErabiltzaileModel erabiltzailea = null;

    @FXML
    private Button btn_datuakEguneratu;

    @FXML
    private Label lbl_ezizena, lbl_izenAbizen;

    @FXML
    private TextField txt_abiBerri, txt_ezizenBerri, txt_izenBerri;

    @FXML
    private PasswordField txt_pasahitzBerri;

    public ProfilaBistaratuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTextFieldProperty(txt_izenBerri);
        setTextFieldProperty(txt_abiBerri);
        setTextFieldProperty(txt_ezizenBerri);
        setTextFieldProperty(txt_pasahitzBerri);

        setTextFieldFormatter(txt_ezizenBerri);
        setTextFieldFormatter(txt_pasahitzBerri);
    }

    @FXML
    void onClickDatuakEguneratu(MouseEvent event) {
        String izenBerri = txt_izenBerri.getText();
        String abiBerri = txt_abiBerri.getText();
        String ezizenBerri = txt_ezizenBerri.getText();
        String pasahitzBerri = txt_pasahitzBerri.getText();
        if (!izenBerri.isBlank() || !abiBerri.isBlank() || !ezizenBerri.isBlank() || !pasahitzBerri.isBlank()) {
            if (datuEgokiak(izenBerri, abiBerri, ezizenBerri, pasahitzBerri)) {
                datuakEguneratu(izenBerri, abiBerri, ezizenBerri, pasahitzBerri);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Zehaztutako daturen bat aurrekoaren berdina da.", ButtonType.CLOSE);
                alert.setTitle("Yama");
                alert.setHeaderText("Datu desegokiak.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    void onClickProfilaEzabatu(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Profilaren eta honekin erlazionatutako datu guztiak ezabatuko dira", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Yama");
        alert.setHeaderText("Ezabatu profila?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                YamaDBKud.getYamaDBKud().ezabatuErabiltzailea(erabiltzailea.getEzizena());
                saioaItxi();
            }
        });
    }

    @FXML
    void onClickSaioaItxi(MouseEvent event) {
        saioaItxi();
    }

    private void saioaItxi() {
        garbituPantaila();
        erabiltzailea = null;
        mainApp.saioaItxi();
    }

    private void setTextFieldProperty(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txt_izenBerri.getText().isBlank() && txt_abiBerri.getText().isBlank() && txt_ezizenBerri.getText().isBlank() && txt_pasahitzBerri.getText().isBlank()) {
                btn_datuakEguneratu.setDisable(true);
            } else {
                btn_datuakEguneratu.setDisable(false);
            }
        });
    }

    private void setTextFieldFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().replace(" ", ""));
            return change;
        }));
    }

    private void garbituPantaila() {
        lbl_izenAbizen.setText("-");
        lbl_ezizena.setText("-");

        btn_datuakEguneratu.setDisable(true);
        txt_izenBerri.clear();
        txt_abiBerri.clear();
        txt_ezizenBerri.clear();
        txt_pasahitzBerri.clear();
        //TODO jarduerak ezabatu
    }

    public void eguneratuPantaila() {
        garbituPantaila();
        erabiltzailea = mainApp.getErabiltzaileAktibo();
        datuakJarri();
        jarduerakJarri();
        //TODO aukeratu "Jarduerak" erlaitza tabpane-an
    }

    private void datuakJarri() {
        lbl_izenAbizen.setText(erabiltzailea.getIzena() + " " + erabiltzailea.getAbizena());
        lbl_ezizena.setText(erabiltzailea.getEzizena());
    }

    private void jarduerakJarri() {
        //TODO erabiltzailearekin erlazionatutako jarduerak jarri
    }

    private boolean datuEgokiak(String izenBerri, String abiBerri, String ezizenBerri, String pasahitzBerri) {
        if (!izenBerri.isBlank() && izenBerri.equals(erabiltzailea.getIzena())) {
            return false;
        }
        if (!abiBerri.isBlank() && abiBerri.equals(erabiltzailea.getAbizena())) {
            return false;
        }
        if (!ezizenBerri.isBlank() && ezizenBerri.equals(erabiltzailea.getEzizena())) {
            return false;
        }
        if (!pasahitzBerri.isBlank() && YamaDBKud.getYamaDBKud().pasahitzBerdinaDa(erabiltzailea.getEzizena(), pasahitzBerri)) {
            return false;
        }
        return true;
    }

    private void datuakEguneratu(String izenBerri, String abiBerri, String ezizenBerri, String pasahitzBerri) {
        String ezizena = erabiltzailea.getEzizena();
        if (!izenBerri.isBlank()) {
            YamaDBKud.getYamaDBKud().eguneratuErabIzen(erabiltzailea.getEzizena(), izenBerri);
        }
        if (!abiBerri.isBlank()) {
            YamaDBKud.getYamaDBKud().eguneratuErabAbizen(erabiltzailea.getEzizena(), abiBerri);
        }
        if (!pasahitzBerri.isBlank()) {
            YamaDBKud.getYamaDBKud().eguneratuErabPasahitz(erabiltzailea.getEzizena(), pasahitzBerri);
        }
        if (!ezizenBerri.isBlank()) {
            YamaDBKud.getYamaDBKud().eguneratuErabEzizen(erabiltzailea.getEzizena(), ezizenBerri);
            ezizena = ezizenBerri;
        }
        mainApp.saioaHasi(ezizena);
    }
}

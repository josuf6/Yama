package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import com.yama.models.ErabiltzaileModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.SQLException;
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
            try {
                if (datuEgokiak(izenBerri, abiBerri, ezizenBerri, pasahitzBerri)) {
                    datuakEguneratu(izenBerri, abiBerri, ezizenBerri, pasahitzBerri);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Zehaztutako daturen bat aurrekoaren berdina da.", ButtonType.CLOSE);
                    alert.setTitle("Yama");
                    alert.setHeaderText("Datu desegokiak.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
                alert.setTitle("Yama");
                alert.setHeaderText("Ustekabeko errorea.");
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
                if (YamaDBKud.getYamaDBKud().ezabatuErabiltzailea(erabiltzailea.getEzizena())) {
                    //TODO ezabatu profilarekin erlazionatutako jarduerak

                    saioaItxi();
                } else {
                    Alert alert2 = new Alert(Alert.AlertType.ERROR, "Ezin izan da erabiltzailea ezabatu. Berriro saiatu", ButtonType.CLOSE);
                    alert2.setTitle("Yama");
                    alert2.setHeaderText("Errorea erabiltzailea ezabatzen.");
                    alert2.showAndWait();
                }
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

    private boolean datuEgokiak(String izenBerri, String abiBerri, String ezizenBerri, String pasahitzBerri) throws SQLException {
        if (!izenBerri.isBlank() && izenBerri.equals(erabiltzailea.getIzena())) {
            return false;
        }
        if (!abiBerri.isBlank() && abiBerri.equals(erabiltzailea.getAbizena())) {
            return false;
        }
        if (!ezizenBerri.isBlank() && ezizenBerri.equals(erabiltzailea.getEzizena())) {
            return false;
        }

        int emaitza = YamaDBKud.getYamaDBKud().pasahitzBerdinaDa(erabiltzailea.getEzizena(), pasahitzBerri);
        if (emaitza == 1 && !pasahitzBerri.isBlank()) {
            return false;
        } else if (emaitza == -1) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Ustekabeko errorea.");
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private void datuakEguneratu(String izenBerri, String abiBerri, String ezizenBerri, String pasahitzBerri) {
        String ezizena = erabiltzailea.getEzizena();

        if (izenBerri.isBlank()) izenBerri = erabiltzailea.getIzena();
        if (abiBerri.isBlank()) abiBerri = erabiltzailea.getAbizena();
        if (ezizenBerri.isBlank()) ezizenBerri = erabiltzailea.getEzizena();

        if (!YamaDBKud.getYamaDBKud().eguneratuErabiltzailea(erabiltzailea.getEzizena(), izenBerri, abiBerri, ezizenBerri, pasahitzBerri)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ezin izan da erabiltzailea eguneratu. Berriro saiatu", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Errorea erabiltzailea eguneratzen.");
            alert.showAndWait();
        } else {
            if (!ezizenBerri.isBlank()) {
                ezizena = ezizenBerri;
            }
            mainApp.saioaHasi(ezizena);
        }
    }
}

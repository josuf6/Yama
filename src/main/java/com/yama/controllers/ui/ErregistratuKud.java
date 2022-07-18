package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ErregistratuKud implements Initializable {

    private Main mainApp;

    @FXML
    private Button btn_kontuaSortu, btn_SaioaHasi;

    @FXML
    private PasswordField txt_pasahitza;

    @FXML
    private TextField txt_abizena, txt_ezizena, txt_izena;

    public ErregistratuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTextFieldProperty(txt_izena);
        setTextFieldProperty(txt_abizena);
        setTextFieldProperty(txt_ezizena);
        setTextFieldProperty(txt_pasahitza);

        setTextFieldFormatter(txt_ezizena);
        setTextFieldFormatter(txt_pasahitza);
    }

    @FXML
    void onClickKontuaSortu(MouseEvent event) {
        int emaitza = YamaDBKud.getYamaDBKud().existitzenDaErabiltzailea(txt_ezizena.getText());
        if (emaitza == 0) {
            if (YamaDBKud.getYamaDBKud().erregistratuErabiltzailea(txt_ezizena.getText(), txt_pasahitza.getText(), txt_izena.getText(), txt_abizena.getText())) {
                mainApp.saioaHasi(txt_ezizena.getText());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ezin izan da erabiltzailea erregistratu. Berriro saiatu.", ButtonType.CLOSE);
                alert.setTitle("Yama");
                alert.setHeaderText("Errorea erabiltzailea erregistratzen.");
                alert.showAndWait();
            }
        } else if (emaitza == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Zehaztutako ezizena erabileran dago.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Ezizen okerra.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Ustekabeko errorea.");
            alert.showAndWait();
        }
    }

    @FXML
    void onClickSaioaHasi(MouseEvent event) {
        mainApp.erakutsiSaioaHasi();
    }

    private void setTextFieldProperty(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txt_izena.getText().isBlank() || txt_abizena.getText().isBlank() || txt_ezizena.getText().isBlank() || txt_pasahitza.getText().isBlank()) {
                btn_kontuaSortu.setDisable(true);
            } else {
                btn_kontuaSortu.setDisable(false);
            }
        });
    }

    private void setTextFieldFormatter(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().replace(" ", ""));
            return change;
        }));
    }

    public void garbituPantaila() {
        btn_kontuaSortu.setDisable(true);
        txt_ezizena.clear();
        txt_pasahitza.clear();
        txt_izena.clear();
        txt_abizena.clear();
    }
}

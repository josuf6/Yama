package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class SaioaHasiKud implements Initializable {

    private Main mainApp;

    @FXML
    private Button btn_saioaHasi;

    @FXML
    private PasswordField txt_pasahitza;

    @FXML
    private TextField txt_ezizena;

    public SaioaHasiKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setTextFieldProperty(txt_ezizena);
        setTextFieldProperty(txt_pasahitza);

        setTextFieldFormatter(txt_ezizena);
        setTextFieldFormatter(txt_pasahitza);
    }

    @FXML
    void onClickSaioaHasi(MouseEvent event) {
        int emaitza = YamaDBKud.getYamaDBKud().egiaztatuErabiltzailea(txt_ezizena.getText(), txt_pasahitza.getText());
        if (emaitza == 1) {
            mainApp.saioaHasi(txt_ezizena.getText());
        } else  if (emaitza == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Zehaztutako ezizena eta/edo pasahitza ez dira zuzenak.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Kredentzial okerrak.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Ustekabeko errorea.");
            alert.showAndWait();
        }
    }

    @FXML
    void onClickKontuBerria(MouseEvent event) {
        mainApp.erakutsiErregistratu();
    }

    private void setTextFieldProperty(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txt_ezizena.getText().isBlank() || txt_pasahitza.getText().isBlank()) {
                btn_saioaHasi.setDisable(true);
            } else {
                btn_saioaHasi.setDisable(false);
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
        btn_saioaHasi.setDisable(true);
        txt_ezizena.clear();
        txt_pasahitza.clear();
    }
}

package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.models.JardueraModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class JardBistaratuKud implements Initializable {

    private Main mainApp;

    @FXML
    private AnchorPane panela;

    @FXML
    private Button btn_atzera;

    @FXML
    private Label lblHasiData, lblIzena, lblMota;

    public JardBistaratuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO Garbitu pantaila
    }

    @FXML
    void onClickAtzera(MouseEvent event) {
        mainApp.atzeraJardBistaratu();
    }

    public void jardBistaratu(JardueraModel pJard) {
        lblIzena.setText(pJard.getIzena());
        lblHasiData.setText(pJard.getHasiData());
        lblMota.setText(pJard.getMota());
        //TODO
    }
}

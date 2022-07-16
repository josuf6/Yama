package com.yama.controllers.ui;

import com.sothawo.mapjfx.*;
import com.yama.Main;
import com.yama.models.IbilJardModel;
import com.yama.models.JardueraModel;
import com.yama.models.KorrJardModel;
import com.yama.models.TxirrJardModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SaioaHasiKud implements Initializable {

    private Main mainApp;

    public SaioaHasiKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}
}

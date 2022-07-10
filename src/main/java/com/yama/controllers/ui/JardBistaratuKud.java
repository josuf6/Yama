package com.yama.controllers.ui;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.yama.Main;
import com.yama.models.JardueraModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JardBistaratuKud implements Initializable {

    private Main mainApp;
    private JardueraModel jarduera;
    private CoordinateLine clKoords;
    private Extent extent;

    @FXML
    private AnchorPane panela;

    @FXML
    private Button btn_atzera;

    @FXML
    private ImageView imgMota;

    @FXML
    private Label lblHasiData, lblIzena;

    @FXML
    private MapView mapa;

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
        jarduera = pJard;
        lblIzena.setText(jarduera.getIzena());
        lblHasiData.setText(jarduera.getHasiData());
        hasieratuMapa();
        hasieratuGrafikoak();

        //TODO
    }


    private void hasieratuMapa() {
        clKoords = sortuIbilbidea();

        ChangeListener<Boolean> trackVisibleListener = (observable, oldValue, newValue) -> mapa.setExtent(extent);
        clKoords.visibleProperty().addListener(trackVisibleListener);

        mapa.initializedProperty().addListener((observable, oldValue, newValue) -> { //Mapa hasieratzean
            if (newValue) {
                afterMapIsInitialized();
            }
        });

        if (!mapa.getInitialized()) { //Mapa hasieratuta ez badago
            mapa.initialize(Configuration.builder()
                    .showZoomControls(true)
                    .build());
        } else { //Mapa hasieratuta badago
            afterMapIsInitialized();
        }
    }

    private CoordinateLine sortuIbilbidea() {
        ArrayList<Coordinate> koordsArray = new ArrayList<>();
        for (Double[] koords : jarduera.getKoordZerr()) {
            koordsArray.add(new Coordinate(koords[0], koords[1]));
        }
        extent = Extent.forCoordinates(koordsArray);
        return new CoordinateLine(koordsArray);
    }

    private void afterMapIsInitialized() {
        mapa.setCenter(kalkMapaZentroa());
        mapa.setZoom(10);
        mapa.addCoordinateLine(clKoords.setVisible(true)
                .setColor(Color.ORANGE).setWidth(5));
    }

    private Coordinate kalkMapaZentroa() {
        double iparLat = jarduera.getKoordZerr().get(0)[0];
        double hegoLat = jarduera.getKoordZerr().get(0)[0];
        double ekiLon = jarduera.getKoordZerr().get(0)[1];
        double mendLon = jarduera.getKoordZerr().get(0)[1];

        //Lortu muturretan dauden koordenatuak
        for (Double[] koords : jarduera.getKoordZerr()) {
            if (koords[0] > iparLat) {
                iparLat = koords[0];
            }
            if (koords[0] < hegoLat) {
                hegoLat = koords[0];
            }
            if (koords[1] > ekiLon) {
                ekiLon = koords[1];
            }
            if (koords[1] < mendLon) {
                mendLon = koords[1];
            }
        }

        //Kalkulatu erdiko puntua
        double lat = (iparLat + hegoLat) / 2;
        double lon = (ekiLon + mendLon) / 2;

        return new Coordinate(lat, lon);
    }

    private void hasieratuGrafikoak() {

    }
}

package com.yama.controllers.ui;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import com.yama.Main;
import com.yama.models.IbilJardModel;
import com.yama.models.JardueraModel;
import com.yama.models.KorrJardModel;
import com.yama.models.TxirrJardModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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
    private AnchorPane panela, altPane, bMPane, kadPane, potPane, tenpPane;

    @FXML
    private Button btn_atzera;

    @FXML
    private ImageView imgMota;

    @FXML
    private Label lblHasiData, lblIzena,
            laburAbiMax, laburBBAbi, laburBBAbiTxt, laburDenbMugi, laburDist, laburIrauTot,
            abiAbiMax, abiBBAbi, abiBBAbiTxt,
            altIgoTot, altAltMin, altAltMax,
            bMBBBM, bMBMMax,
            kadBBKad, kadKadMax,
            potBBPot, potPotMax,
            tenpBBTenp, tenpTenpMin, tenpTenpMax;

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
        hasieratuEstatistikak();

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

    private void hasieratuEstatistikak() {
        hasiLaburPane();
        hasiAbiPane();
        hasiAltPane();
        hasiBMPane();
        hasiKadPane();
        hasiPotPane();
        hasiTenpPane();

        //TODO pane guztiak
    }

    private void hasiLaburPane() {
        laburDist.setText(jarduera.getDistantzia());
        laburDenbMugi.setText(jarduera.getDenbMugi());
        if (jarduera instanceof IbilJardModel || jarduera instanceof KorrJardModel) {
            laburBBAbiTxt.setText("Batez besteko erritmoa");
        } else {
            laburBBAbiTxt.setText("Batez besteko abiadura");
        }
        laburBBAbi.setText(jarduera.getBbAbiadura());
        laburAbiMax.setText(jarduera.getAbiaduraMax());
        laburIrauTot.setText(jarduera.getIraupena());
    }

    private void hasiAbiPane() {
        //TODO abiadura-grafikoa

        if (jarduera instanceof IbilJardModel || jarduera instanceof KorrJardModel) {
            abiBBAbiTxt.setText("Batez besteko erritmoa");
        } else {
            abiBBAbiTxt.setText("Batez besteko abiadura");
        }
        abiBBAbi.setText(jarduera.getBbAbiadura());
        abiAbiMax.setText(jarduera.getAbiaduraMax());
    }

    private void hasiAltPane() {
        //TODO altueraren grafikoa

        if (jarduera.getAltZerr() != null) {
            altIgoTot.setText(jarduera.getIgoeraTot());
            altAltMin.setText(jarduera.getAltueraMin());
            altAltMax.setText(jarduera.getAltueraMax());
            altPane.setVisible(true);
            altPane.setManaged(true);
        } else {
            altPane.setVisible(false);
            altPane.setManaged(false);
        }
    }

    private void hasiBMPane() {
        //TODO Bihotz-maiztasunaren grafikoa

        if (jarduera.getBihotzMaizZerr() != null) {
            bMBBBM.setText(jarduera.getBbBihotzMaiz());
            bMBMMax.setText(jarduera.getBihotzMaizMax());
            bMPane.setVisible(true);
            bMPane.setManaged(true);
        } else {
            bMPane.setVisible(false);
            bMPane.setManaged(false);
        }
    }

    private void hasiKadPane() {
        //TODO Kadentziaren grafikoa

        if (jarduera instanceof TxirrJardModel && ((TxirrJardModel) jarduera).getKadZerr() != null) {
            kadBBKad.setText(((TxirrJardModel) jarduera).getBbKad());
            kadKadMax.setText(((TxirrJardModel) jarduera).getKadMax());
            kadPane.setVisible(true);
            kadPane.setManaged(true);
        } else {
            kadPane.setVisible(false);
            kadPane.setManaged(false);
        }
    }

    private void hasiPotPane() {
        //TODO Potentziaren grafikoa

        if (jarduera instanceof TxirrJardModel && ((TxirrJardModel) jarduera).getPotZerr() != null) {
            potBBPot.setText(((TxirrJardModel) jarduera).getBbPot());
            potPotMax.setText(((TxirrJardModel) jarduera).getPotMax());
            potPane.setVisible(true);
            potPane.setManaged(true);
        } else {
            potPane.setVisible(false);
            potPane.setManaged(false);
        }
    }

    private void hasiTenpPane() {
        //TODO altueraren grafikoa

        if (jarduera.getTenpZerr() != null) {
            tenpBBTenp.setText(jarduera.getBbTenp());
            tenpTenpMin.setText(jarduera.getTenpMin());
            tenpTenpMax.setText(jarduera.getTenpMax());
            tenpPane.setVisible(true);
            tenpPane.setManaged(true);
        } else {
            tenpPane.setVisible(false);
            tenpPane.setManaged(false);
        }
    }
}

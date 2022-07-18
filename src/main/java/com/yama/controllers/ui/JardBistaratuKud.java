package com.yama.controllers.ui;

import com.sothawo.mapjfx.*;
import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import com.yama.models.IbilJardModel;
import com.yama.models.JardueraModel;
import com.yama.models.KorrJardModel;
import com.yama.models.TxirrJardModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class JardBistaratuKud implements Initializable {

    private Main mainApp;
    private JardueraModel jarduera;
    private double distantzia;
    private ArrayList<Double> distZerr;
    private boolean estatistikakHasita,
            laburPaneHasita, abiPaneHasita, altPaneHasita, bMPaneHasita, kadPaneHasita, potPaneHasita, tenpPaneHasita,
            abiGrafHasita, altGrafHasita, bMGrafHasita, kadGrafHasita, potGrafHasita, tenpGrafHasita;
    private CoordinateLine clKoords;
    private Extent extent;
    private AreaChart<Number, Number> abiGraf, altGraf, bihotzMaizGraf, kadGraf, potGraf, tenpGraf;

    @FXML
    private AnchorPane altPane, bMPane, kadPane, potPane, tenpPane, abiGrafPane, altGrafPane, bMGrafPane, kadGrafPane, potGrafPane, tenpGrafPane;

    @FXML
    private Button btn_jardGorde;

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
    public void initialize(URL location, ResourceBundle resources) {}

    @FXML
    void onClickAtzera() {
        mainApp.atzeraJardBistaratu();
    }

    @FXML
    void onClickJardGorde(MouseEvent event) {
        if (!YamaDBKud.getYamaDBKud().jardueraGordeta(mainApp.getErabiltzaileAktibo().getEzizena(), jarduera)) {
            YamaDBKud.getYamaDBKud().gordeJarduera(mainApp.getErabiltzaileAktibo().getEzizena(), jarduera);

            jardBistaratu(jarduera, "gordeta");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Datu hauek dituen jarduera bat existitzen da gordeta profil honetan.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Jarduera errepikatuta.");
            alert.showAndWait();
        }
    }

    public void jardBistaratu(JardueraModel pJard, String jardMota) {
        garbituPantaila();

        jarduera = pJard;
        lblIzena.setText(jarduera.getIzena());
        lblHasiData.setText(jarduera.getHasiData());

        if (jardMota.equals("inportatuta") && mainApp.getErabiltzaileAktibo() != null) {
            btn_jardGorde.setVisible(true);
        } else if (jardMota.equals("gordeta")) {
            btn_jardGorde.setVisible(false);

            //TODO jarduera profil batean gordeta badago (erabiltzailearen izena jarri pantailan)
        }

        hasieratuMapa();

        estatistikakHasita = false;
        hasieratuEstatistikak();

        //TODO mapa/estatistikak pantaila (haria ere)

        while (!estatistikakHasita) { //TODO gehitu analisiaHasita aldagaia mapa/estatistikak pantaila egin eta gero
            try { //Hau ez badago programaren exekuzioa ez da aurera joaten
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //TODO aukeratu "Mapa" erlaitza tabpane-an
    }

    private void garbituPantaila() {
        //TODO garbitu pantaila

        //TODO erreseteatu mapa
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

        sortuExtent(koordsArray);

        return new CoordinateLine(koordsArray);
    }

    private void sortuExtent(ArrayList<Coordinate> koordsArray) {
        Extent extentEstandar = Extent.forCoordinates(koordsArray);
        double minLat = extentEstandar.getMin().getLatitude();
        double minLon = extentEstandar.getMin().getLongitude();
        double maxLat = extentEstandar.getMax().getLatitude();
        double maxLon = extentEstandar.getMax().getLongitude();
        double minLatBerria = minLat - ((maxLat - minLat) * 0.05);
        double minLonBerria = minLon - ((maxLon - minLon) * 0.05);
        double maxLatBerria = maxLat + ((maxLat - minLat) * 0.05);
        double maxLonBerria = maxLon + ((maxLon - minLon) * 0.05);
        extent = Extent.forCoordinates(new Coordinate(minLatBerria, minLonBerria), new Coordinate(maxLatBerria, maxLonBerria));
    }

    private void afterMapIsInitialized() {
        //mapa.setCenter(kalkMapaZentroa());
        //mapa.setZoom(10);
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
        distZerr = (ArrayList<Double>) jarduera.getDistZerr().clone();
        distantzia = jarduera.getDistBal();
        if (distantzia >= 1000) {
            distZerr.replaceAll(dist -> dist / 1000);
        }

        //Estatistiken grafikoak sortzeko hariak prestatu eta hasieratu
        abiGrafHasita = false;
        altGrafHasita = false;
        bMGrafHasita = false;
        kadGrafHasita = false;
        potGrafHasita = false;
        tenpGrafHasita = false;

        Thread hariAbiPane = new Thread(() -> { //Beti dago abiaduraren informazioa
            hasiAbiGraf();
            abiGrafHasita = true;
        });
        hariAbiPane.start();

        if (jarduera.getAltZerr() != null) { //Altitudearen informazioa badago
            Thread hariAltPane = new Thread(() -> {
                hasiAltGraf();
                altGrafHasita = true;
            });
            hariAltPane.start();
        } else { //Altitudearen informazioa ez badago
            altPane.setVisible(false);
            altPane.setManaged(false);
            altGrafHasita = true;
        }

        if (jarduera.getBihotzMaizZerr() != null) { //Bihotz-maiztasunaren informazioa badago
            Thread hariBMPane = new Thread(() -> {
                hasiBMGraf();
                bMGrafHasita = true;
            });
            hariBMPane.start();
        } else { //Bihotz-maiztasunaren informazioa ez badago
            bMPane.setVisible(false);
            bMPane.setManaged(false);
            bMGrafHasita = true;
        }

        if (jarduera instanceof TxirrJardModel && ((TxirrJardModel) jarduera).getKadZerr() != null) { //Kadentziaren informazioa badago
            Thread hariKadPane = new Thread(() -> {
                hasiKadGraf();
                kadGrafHasita = true;
            });
            hariKadPane.start();
        } else { //Kadentziaren informazioa ez badago
            kadPane.setVisible(false);
            kadPane.setManaged(false);
            kadGrafHasita = true;
        }

        if (jarduera instanceof TxirrJardModel && ((TxirrJardModel) jarduera).getPotZerr() != null) { //Potentziaren informazioa badago
            Thread hariPotPane = new Thread(() -> {
                hasiPotGraf();
                potGrafHasita = true;
            });
            hariPotPane.start();
        } else { //Potentziaren informazioa ez badago
            potPane.setVisible(false);
            potPane.setManaged(false);
            potGrafHasita = true;
        }

        if (jarduera.getTenpZerr() != null) { //Tenperaturaren informazioa badago
            Thread hariTenpPane = new Thread(() -> {
                hasiTenpGraf();
                tenpGrafHasita = true;
            });
            hariTenpPane.start();
        } else { //Tenperaturaren informazioa ez badago
            tenpPane.setVisible(false);
            tenpPane.setManaged(false);
            tenpGrafHasita = true;
        }

        //Estatistiken panelak hasieratu (grafikoak sortzen diren bitartean)
        laburPaneHasita = false;
        abiPaneHasita = false;
        altPaneHasita = false;
        bMPaneHasita = false;
        kadPaneHasita = false;
        potPaneHasita = false;
        tenpPaneHasita = false;

        do {
            try { //Hau ez badago programaren exekuzioa ez da aurera joaten
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!laburPaneHasita) {
                hasiLaburPane();
                laburPaneHasita = true;
            }

            if (abiGrafHasita && !abiPaneHasita) {
                hasiAbiPane();
                abiPaneHasita = true;
            }

            if (altGrafHasita && !altPaneHasita) {
                if (jarduera.getAltZerr() != null) {
                    hasiAltPane();
                }
                altPaneHasita = true;
            }

            if (bMGrafHasita && !bMPaneHasita) {
                if (jarduera.getBihotzMaizZerr() != null) {
                    hasiBMPane();
                }
                bMPaneHasita = true;
            }

            if (kadGrafHasita && !kadPaneHasita) {
                if (jarduera instanceof TxirrJardModel && ((TxirrJardModel) jarduera).getKadZerr() != null) {
                    hasiKadPane();
                }
                kadPaneHasita = true;
            }

            if (potGrafHasita && !potPaneHasita) {
                if (jarduera instanceof TxirrJardModel && ((TxirrJardModel) jarduera).getPotZerr() != null) {
                    hasiPotPane();
                }
                potPaneHasita = true;
            }

            if (tenpGrafHasita && !tenpPaneHasita) {
                if (jarduera.getTenpZerr() != null) {
                    hasiTenpPane();
                }
                tenpPaneHasita = true;
            }
        } while (!laburPaneHasita || !abiPaneHasita || !altPaneHasita || !bMPaneHasita || !kadPaneHasita || !potPaneHasita || !tenpPaneHasita);

        estatistikakHasita = true;
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

    private void hasiAbiGraf() {

        //Grafikoaren limiteak definitu eta grafikoa sortu
        ArrayList<Double> abiZerr = jarduera.getAbiZerr();
        String xLabel = "m";
        double xMax = distantzia;
        if (distantzia >= 1000) {
            xMax = distantzia / 1000;
            xLabel = "km";
        }
        double abiMax = jarduera.getAbiMaxBal();
        String yLabel = "km/h";
        int[] kolorea = {70, 203, 255};
        if (jarduera instanceof IbilJardModel || jarduera instanceof KorrJardModel) {
            abiZerr.replaceAll(abi -> abi / 3.6);
            abiMax = abiMax / 3.6;
            yLabel = "m/s";
        }
        abiGraf = sortuGrafikoa(distZerr, abiZerr, xMax, 0, abiMax, String.valueOf(jarduera.getBbAbiBal()), xLabel, yLabel, kolorea);
    }

    private void hasiAbiPane() {
        if (jarduera instanceof IbilJardModel || jarduera instanceof KorrJardModel) {
            abiBBAbiTxt.setText("Batez besteko erritmoa");
        } else {
            abiBBAbiTxt.setText("Batez besteko abiadura");
        }
        abiBBAbi.setText(jarduera.getBbAbiadura());
        abiAbiMax.setText(jarduera.getAbiaduraMax());

        jarriGrafikoa(abiGraf, abiGrafPane);
    }

    private void hasiAltGraf() {
        //Grafikoaren limiteak definitu eta grafikoa sortu
        ArrayList<Double> altZerr = jarduera.getAltZerr();
        String xLabel = "m";
        double xMax = distantzia;
        if (distantzia >= 1000) {
            xMax = distantzia / 1000;
            xLabel = "km";
        }
        double altMin = jarduera.getAltueraMinBal();
        if (altMin > 0) {
            altMin = 0;
        }
        double altMax = jarduera.getAltueraMaxBal();
        String yLabel = "m";
        int[] kolorea = {77, 222, 77};
        altGraf = sortuGrafikoa(distZerr, altZerr, xMax, altMin, altMax, null, xLabel, yLabel, kolorea);

        altPane.setVisible(true);
        altPane.setManaged(true);
    }

    private void hasiAltPane() {
        altIgoTot.setText(jarduera.getIgoeraTot());
        altAltMin.setText(jarduera.getAltueraMin());
        altAltMax.setText(jarduera.getAltueraMax());

        jarriGrafikoa(altGraf, altGrafPane);
    }

    private void hasiBMGraf() {

        //Grafikoaren limiteak definitu eta grafikoa sortu
        ArrayList<Double> bihotzMaizZerr = (ArrayList<Double>) jarduera.getBihotzMaizZerr().clone();
        String xLabel = "m";
        double xMax = distantzia;
        if (distantzia >= 1000) {
            xMax = distantzia / 1000;
            xLabel = "km";
        }
        double bihotzMaizMin = jarduera.getBihotzMaizMinBal();
        bihotzMaizMin = bihotzMaizMin - (bihotzMaizMin * 0.1);
        double bihotzMaizMax = jarduera.getBihotzMaizMaxBal();
        String yLabel = "bpm";
        int[] kolorea = {222, 88, 77};
        bihotzMaizGraf = sortuGrafikoa(distZerr, bihotzMaizZerr, xMax, bihotzMaizMin, bihotzMaizMax, jarduera.getBbBihotzMaizBal(), xLabel, yLabel, kolorea);

        bMPane.setVisible(true);
        bMPane.setManaged(true);
    }

    private void hasiBMPane() {
        bMBBBM.setText(jarduera.getBbBihotzMaiz());
        bMBMMax.setText(jarduera.getBihotzMaizMax());

        jarriGrafikoa(bihotzMaizGraf, bMGrafPane);
    }

    private void hasiKadGraf() {

        //Grafikoaren limiteak definitu eta grafikoa sortu
        ArrayList<Double> kadZerr = (ArrayList<Double>) ((TxirrJardModel) jarduera).getKadZerr().clone();
        String xLabel = "m";
        double xMax = distantzia;
        if (distantzia >= 1000) {
            xMax = distantzia / 1000;
            xLabel = "km";
        }
        double kadMax = ((TxirrJardModel) jarduera).getKadMaxBal();
        String yLabel = "rpm";
        int[] kolorea = {222, 150, 77};
        kadGraf = sortuGrafikoa(distZerr, kadZerr, xMax, 0, kadMax, ((TxirrJardModel) jarduera).getBbKadBal(), xLabel, yLabel, kolorea);

        kadPane.setVisible(true);
        kadPane.setManaged(true);
    }

    private void hasiKadPane() {
        kadBBKad.setText(((TxirrJardModel) jarduera).getBbKad());
        kadKadMax.setText(((TxirrJardModel) jarduera).getKadMax());

        jarriGrafikoa(kadGraf, kadGrafPane);
    }

    private void hasiPotGraf() {

        //Grafikoaren limiteak definitu eta grafikoa sortu
        ArrayList<Double> potZerr = (ArrayList<Double>) ((TxirrJardModel) jarduera).getPotZerr().clone();
        String xLabel = "m";
        double xMax = distantzia;
        if (distantzia >= 1000) {
            xMax = distantzia / 1000;
            xLabel = "km";
        }
        double potMax = ((TxirrJardModel) jarduera).getPotMaxBal();
        String yLabel = "W";
        int[] kolorea = {222, 77, 222};
        potGraf = sortuGrafikoa(distZerr, potZerr, xMax, 0, potMax, ((TxirrJardModel) jarduera).getBbPotBal(), xLabel, yLabel, kolorea);

        potPane.setVisible(true);
        potPane.setManaged(true);
    }

    private void hasiPotPane() {
        potBBPot.setText(((TxirrJardModel) jarduera).getBbPot());
        potPotMax.setText(((TxirrJardModel) jarduera).getPotMax());

        jarriGrafikoa(potGraf, potGrafPane);
    }

    private void hasiTenpGraf() {

        //Grafikoaren limiteak definitu eta grafikoa sortu
        ArrayList<Double> tenpZerr = jarduera.getTenpZerr();
        String xLabel = "m";
        double xMax = distantzia;
        if (distantzia >= 1000) {
            xMax = distantzia / 1000;
            xLabel = "km";
        }
        double tenpMin = jarduera.getTenpMinBal();
        tenpMin = tenpMin - (tenpMin * 0.1);
        double tenpMax = jarduera.getTenpMaxBal();
        String yLabel = "ºC";
        int[] kolorea = {77, 222, 222};
        tenpGraf = sortuGrafikoa(distZerr, tenpZerr, xMax, tenpMin, tenpMax, jarduera.getBbTenpBal(), xLabel, yLabel, kolorea);

        tenpPane.setVisible(true);
        tenpPane.setManaged(true);
    }

    private void hasiTenpPane() {
        tenpBBTenp.setText(jarduera.getBbTenp());
        tenpTenpMin.setText(jarduera.getTenpMin());
        tenpTenpMax.setText(jarduera.getTenpMax());

        jarriGrafikoa(tenpGraf, tenpGrafPane);
    }

    private AreaChart<Number, Number> sortuGrafikoa(ArrayList<Double> xZerr, ArrayList<Double> yZerr, double xMax, double yMin, double yMax, String yBatezBesteko, String xLabel, String yLabel, int[] rgb) {

        //X ardatza definitu
        NumberAxis xAxis = new NumberAxis(0, xMax, (int) (xMax / 10));
        xAxis.setLabel(xLabel);

        //Y ardatza definitu
        int maxY = (int) (yMax + (yMax * 0.1));
        int minY = (int) yMin;
        if (minY < 0) {
            minY = 0;
        }
        NumberAxis yAxis = new NumberAxis(minY, maxY, (maxY - minY) / 5);
        yAxis.setLabel(yLabel);

        //Grafikoa sortu eta estiloa aplikatu
        AreaChart<Number, Number> grafikoa = new AreaChart(xAxis, yAxis);
        grafikoa.setStyle("-fx-legend-visible: false");
        grafikoa.setCreateSymbols(false);

        //Jarri informazioa grafikoan
        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < yZerr.size(); i++) {
            Number xElem = xZerr.get(i);
            Number yElem = yZerr.get(i);
            if (yElem == null) {
                yElem = 0;
            }
            if (jarduera.getAbiZerr().get(i) > 0.5) { //Mugimenduan egonez gero
                series.getData().add(new XYChart.Data(xElem, yElem));
            }
        }
        grafikoa.getData().addAll(series);
        series.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgba(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ");");
        series.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: rgba(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ");");

        //Batez bestekoaren lerro bat marraztu existitzen bada
        if (yBatezBesteko != null) {
            XYChart.Series seriesBB = new XYChart.Series();
            seriesBB.getData().add(new XYChart.Data<>(0, Double.parseDouble(yBatezBesteko)));
            seriesBB.getData().add(new XYChart.Data<>(xZerr.get(xZerr.size() - 1), Double.parseDouble(yBatezBesteko)));
            grafikoa.getData().addAll(seriesBB);
            seriesBB.getNode().lookup(".chart-series-area-fill").setStyle("-fx-fill: rgb(0,0,0,0);");
            seriesBB.getNode().lookup(".chart-series-area-line").setStyle("-fx-stroke: rgba(" + (rgb[0] - 70) + "," + (rgb[1] - 70) + "," + (rgb[2] - 70) + "); -fx-stroke-width: 1.5; -fx-stroke-dash-array: 6");
        }

        return grafikoa;
    }

    private void jarriGrafikoa(AreaChart<Number, Number> grafikoa, AnchorPane panela) {
        grafikoa.setPrefWidth(panela.getPrefWidth());
        grafikoa.setPrefHeight(panela.getPrefHeight());
        panela.getChildren().add(grafikoa);
    }
}

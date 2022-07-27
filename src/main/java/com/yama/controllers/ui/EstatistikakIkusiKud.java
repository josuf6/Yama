package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import com.yama.models.JardueraModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.*;

public class EstatistikakIkusiKud implements Initializable {

    private Main mainApp;
    private String urtea, kirola;
    private ArrayList<JardueraModel> jardZerr;
    private ArrayList<String> urteZerr;

    @FXML
    private AnchorPane pane_hilak, pane_jardKop, pane_sekDiag;

    @FXML
    private ComboBox<String> cmb_kirola, cmb_urtea;

    @FXML
    private Label lbl_besteKop, lbl_ibilKop, lbl_jardKop, lbl_korrKop, lbl_txirrKop, lbl_laburDenbTot, lbl_laburDistTot, lbl_laburJardKop;

    @FXML
    private ScrollPane scrll_Estatistikak;

    public EstatistikakIkusiKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmb_kirola.getItems().add("Edozein kirol");
        cmb_kirola.getItems().add("Txirrindularitza");
        cmb_kirola.getItems().add("Korrika");
        cmb_kirola.getItems().add("Ibilaritza");
        cmb_kirola.getItems().add("Bestelakoa");

        cmb_urtea.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) eguneratuPantaila();
        });

        cmb_kirola.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) eguneratuPantaila();
        });
    }

    public void hasieratuPantaila() {
        jardZerr = YamaDBKud.getYamaDBKud().getErabJarduerak(mainApp.getErabiltzaileAktibo().getEzizena());
        urteZerr = new ArrayList<>();

        cmb_urtea.getItems().clear();
        cmb_urtea.getItems().add("Edozein urte");

        if (jardZerr != null && jardZerr.size() > 0) {
            Collections.sort(jardZerr, Comparator.comparing(JardueraModel::getHasiData));
            Collections.reverse(jardZerr);

            for (JardueraModel jarduera : jardZerr) {
                if (jarduera.getHasiDataUrte() != null && !cmb_urtea.getItems().contains(jarduera.getHasiDataUrte())) {
                    cmb_urtea.getItems().add(jarduera.getHasiDataUrte());
                    urteZerr.add(jarduera.getHasiDataUrte());
                }
            }

            Collections.reverse(urteZerr);
        }

        cmb_urtea.getSelectionModel().select(0);
        cmb_kirola.getSelectionModel().select(0);

        eguneratuPantaila();
    }

    private void eguneratuPantaila() {
        scrll_Estatistikak.setVvalue(0);

        if (cmb_urtea.getSelectionModel().getSelectedIndex() == 0) urtea = "";
        else urtea = cmb_urtea.getSelectionModel().getSelectedItem();

        if (cmb_kirola.getSelectionModel().getSelectedIndex() == 0) kirola = "";
        else kirola = cmb_kirola.getSelectionModel().getSelectedItem();

        if (kirola.equals("")) {
            eguJardKopTxartela();
            pane_jardKop.setVisible(true);
            pane_jardKop.setManaged(true);
        } else {
            pane_jardKop.setVisible(false);
            pane_jardKop.setManaged(false);
        }

        eguKmKopTxartela();
        eguLaburpenTxartela();
    }

    private void eguJardKopTxartela() {
        int jardKop = 0, txirrKop = 0, korrKop = 0, ibilKop = 0, besteKop = 0;

        for (JardueraModel jard : jardZerr) {
            if (urtea.isBlank() || urtea.equals(jard.getHasiDataUrte())) {
                jardKop++;

                if (jard.getMota().equals("Txirrindularitza")) txirrKop++;
                else if (jard.getMota().equals("Korrika")) korrKop++;
                else if (jard.getMota().equals("Ibilaritza")) ibilKop++;
                else besteKop++;
            }
        }

        lbl_jardKop.setText(String.valueOf(jardKop));
        lbl_txirrKop.setText(String.valueOf(txirrKop));
        lbl_korrKop.setText(String.valueOf(korrKop));
        lbl_ibilKop.setText(String.valueOf(ibilKop));
        lbl_besteKop.setText(String.valueOf(besteKop));

        jarriKirolDiagrama(jardKop, txirrKop, korrKop, ibilKop, besteKop);
    }

    private void eguKmKopTxartela() {
        if (urtea.isBlank()) {
            double[] distUrteak = new double[urteZerr.size()];

            for (JardueraModel jard : jardZerr) {
                if (kirola.isBlank() || kirola.equals(jard.getMota())) {
                    int jardUrte = Integer.parseInt(jard.getHasiDataUrte());
                    distUrteak[urteZerr.indexOf(String.valueOf(jardUrte))] += (int) jard.getDistBal();
                }
            }

            for (int i = 0; i < distUrteak.length; i++) {
                distUrteak[i] = distUrteak[i] / 1000;
            }
            jarriUrteDiagrama(distUrteak);
        } else {
            double[] distHilak = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            for (JardueraModel jard : jardZerr) {
                if (urtea.equals(jard.getHasiDataUrte()) && (kirola.isBlank() || kirola.equals(jard.getMota()))) {
                    int jardHilabete = Integer.parseInt(jard.getHasiDataHilabete());
                    distHilak[jardHilabete] += (int) jard.getDistBal();
                }
            }

            for (int i = 0; i < distHilak.length; i++) {
                distHilak[i] = distHilak[i] / 1000;
            }
            jarriHilabeteDiagrama(distHilak);
        }
    }

    private void eguLaburpenTxartela() {
        int jardKop = 0, denbTot = 0, distTot = 0;

        for (JardueraModel jard : jardZerr) {
            if ((urtea.isBlank() || urtea.equals(jard.getHasiDataUrte())) && (kirola.isBlank() || kirola.equals(jard.getMota()))) {
                jardKop++;
                denbTot += jard.getIraupenaBal();
                distTot += jard.getDistBal();
            }
        }

        lbl_laburJardKop.setText(String.valueOf(jardKop));
        lbl_laburDenbTot.setText(formateatuIraupena(denbTot));
        lbl_laburDistTot.setText(formateatuDistantzia(distTot));
    }

    private void jarriUrteDiagrama(double[] distUrteak) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Urtea");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("km");

        BarChart<String, Number> urteDiagrama = new BarChart(xAxis, yAxis);
        urteDiagrama.setStyle("-fx-legend-visible: false");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (urteZerr.size() > 0) {
            int urtea = Integer.parseInt(urteZerr.get(0));
            int urteMax = Integer.parseInt(urteZerr.get(urteZerr.size() - 1));

            int i = 0;
            while (urtea <= urteMax) {
                double distantzia = 0;

                if (urteZerr.contains(String.valueOf(urtea))) {
                    distantzia = distUrteak[i];
                    i++;
                }
                series.getData().add(new XYChart.Data(String.valueOf(urtea), distantzia));

                urtea++;
            }
        }

        urteDiagrama.getData().add(series);

        pane_hilak.getChildren().clear();
        urteDiagrama.setPrefWidth(pane_hilak.getPrefWidth());
        urteDiagrama.setPrefHeight(pane_hilak.getPrefHeight());
        pane_hilak.getChildren().add(urteDiagrama);
    }

    private void jarriHilabeteDiagrama(double[] distHilak) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Hilabetea");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("km");

        BarChart<String, Number> hilabeteDiagrama = new BarChart(xAxis, yAxis);
        hilabeteDiagrama.setStyle("-fx-legend-visible: false");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data("Urtarrila", distHilak[0]));
        series.getData().add(new XYChart.Data("Otsaila", distHilak[1]));
        series.getData().add(new XYChart.Data("Martxoa", distHilak[2]));
        series.getData().add(new XYChart.Data("Aprirla", distHilak[3]));
        series.getData().add(new XYChart.Data("Maiatza", distHilak[4]));
        series.getData().add(new XYChart.Data("Ekaina", distHilak[5]));
        series.getData().add(new XYChart.Data("Uztaila", distHilak[6]));
        series.getData().add(new XYChart.Data("Abuztua", distHilak[7]));
        series.getData().add(new XYChart.Data("Iraila", distHilak[8]));
        series.getData().add(new XYChart.Data("Urria", distHilak[9]));
        series.getData().add(new XYChart.Data("Azaroa", distHilak[10]));
        series.getData().add(new XYChart.Data("Abendua", distHilak[11]));

        hilabeteDiagrama.getData().add(series);

        pane_hilak.getChildren().clear();
        hilabeteDiagrama.setPrefWidth(pane_hilak.getPrefWidth());
        hilabeteDiagrama.setPrefHeight(pane_hilak.getPrefHeight());
        pane_hilak.getChildren().add(hilabeteDiagrama);
    }

    private void jarriKirolDiagrama(int pJardKop, int pTxirrKop, int pKorrKop, int pIbilKop, int pBesteKop) {
        pane_sekDiag.getChildren().clear();

        if (pJardKop > 0) {
            ObservableList<PieChart.Data> kirolDiagDatuak = FXCollections.observableArrayList(
                    new PieChart.Data("Txirrindularitza", pTxirrKop),
                    new PieChart.Data("Korrika", pKorrKop),
                    new PieChart.Data("Ibilaritza", pIbilKop),
                    new PieChart.Data("Bestelakoa", pBesteKop));

            PieChart kirolDiagrama = new PieChart(kirolDiagDatuak);
            kirolDiagrama.setLabelsVisible(false);

            kirolDiagrama.setPrefWidth(pane_sekDiag.getPrefWidth());
            kirolDiagrama.setPrefHeight(pane_sekDiag.getPrefHeight());
            pane_sekDiag.getChildren().add(kirolDiagrama);
        }
    }

    private String formateatuIraupena(long pDenbora) {
        long denbora = pDenbora;

        long seg = denbora;
        if (seg < 60) {
            return seg + "s";
        }
        seg = seg % 60;
        denbora = denbora - seg;

        long min = denbora / 60;
        if (min < 60) {
            return min + "m " + seg + "s";
        }
        min = min % 60;
        denbora = denbora - (min * 60);

        long ord = denbora / (60 * 60);
        if (ord < 24) {
            return ord + "h " + min + "m " + seg + "s";
        }
        ord = ord % 24;
        denbora = denbora - (ord * 60 * 60);

        long egu = denbora / (60 * 60 * 24);

        return egu + "d " + ord + "h " + min + "m " + seg + "s";
    }

    private String formateatuDistantzia(int pDistantzia) {
        if (pDistantzia < 1000) {
            return pDistantzia + " m";
        }

        int m = pDistantzia % 1000;
        if (m >= 100) {
            m = m / 10;
        }
        int km = pDistantzia / 1000;
        return km + "," + m + " km";
    }
}

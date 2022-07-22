package com.yama.controllers.ui;

import com.sothawo.mapjfx.*;
import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import com.yama.models.IbilJardModel;
import com.yama.models.JardueraModel;
import com.yama.models.KorrJardModel;
import com.yama.models.TxirrJardModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class JardBistaratuKud implements Initializable {

    private Main mainApp;
    private JardueraModel jarduera;
    private double distantzia;
    private ArrayList<Double> distZerr;
    private boolean estatistikakHasita, analisiaHasita, analisiTabHasieratuta,
            laburPaneHasita, abiPaneHasita, altPaneHasita, bMPaneHasita, kadPaneHasita, potPaneHasita, tenpPaneHasita,
            abiGrafHasita, altGrafHasita, bMGrafHasita, kadGrafHasita, potGrafHasita, tenpGrafHasita;
    private Coordinate hasiKoord, bukKoord;
    private CoordinateLine clKoords;
    private ArrayList<Coordinate> koordsArray;
    private ArrayList<CoordinateLine> clArray;
    private Marker hasiMarker, bukMarker, analisiMarker;
    private Extent extent;
    private AreaChart<Number, Number> abiGraf, altGraf, bihotzMaizGraf, kadGraf, potGraf, tenpGraf, analisiAbiGraf, analisiAltGraf, analisiBMGraf, analisiPotGraf;

    @FXML
    private AnchorPane analisiGrafPane, altPane, bMPane, kadPane, potPane, tenpPane, abiGrafPane, altGrafPane, bMGrafPane, kadGrafPane, potGrafPane, tenpGrafPane, pane_mapaAnalisia;

    @FXML
    private Button btn_datuakEguneratu, btn_jardGorde;

    @FXML
    private ComboBox<String> cmb_kirolMotaBerria, cmb_analisia;

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
            tenpBBTenp, tenpTenpMin, tenpTenpMax,
            lbl_anal1Txt, lbl_anal1, lbl_anal2Txt, lbl_anal2, lbl_anal3Txt, lbl_anal3;

    @FXML
    private MapView mapa, mapa_analisia;

    @FXML
    private ScrollPane scrll_Estatistikak;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField txt_izenBerria;

    public JardBistaratuKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmb_kirolMotaBerria.getItems().add("");
        cmb_kirolMotaBerria.getItems().add("Txirrindularitza");
        cmb_kirolMotaBerria.getItems().add("Korrika");
        cmb_kirolMotaBerria.getItems().add("Ibilaritza");

        setTextFieldProperty(txt_izenBerria);
        setComboBoxProperty(cmb_kirolMotaBerria);

        cmb_analisia.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals(oldValue)) eguneratuMapaAnalisia();
        });

        clArray = new ArrayList<>();
    }

    @FXML
    void onClickAtzera() {
        mainApp.atzeraJardBistaratu();
    }

    @FXML
    void onClickDatuakEguneratu() {
        btn_datuakEguneratu.setDisable(true);
        String izenBerria = txt_izenBerria.getText();
        String motaBerria = cmb_kirolMotaBerria.getSelectionModel().getSelectedItem();

        JardueraModel jardBerria = jarduera;
        boolean izenaAldatuta = false;
        boolean motaAldatuta = false;
        if (!izenBerria.equals(jarduera.getIzenaBal())) {
            jardBerria.setIzena(izenBerria);
            izenaAldatuta = true;
        }
        if (!motaBerria.equals(jarduera.getMotaBal())) {
            jardBerria.setMota(motaBerria);
            motaAldatuta = true;
            jardBerria = formateatuJard(jardBerria);
        }
        if (jarduera.getIdDB() < 0) {
            if (motaAldatuta) {
                switch (jardBerria.getMotaBal()) {
                    case "Txirrindularitza" -> {
                        TxirrJardModel txirrJardBerria = (TxirrJardModel) jardBerria;
                        mainApp.jardKargTaulaEguneratu(txirrJardBerria);
                        jardBistaratu(txirrJardBerria);
                    }
                    case "Korrika" -> {
                        KorrJardModel korrJardBerria = (KorrJardModel) jardBerria;
                        mainApp.jardKargTaulaEguneratu(korrJardBerria);
                        jardBistaratu(korrJardBerria);
                    }
                    case "Ibilaritza" -> {
                        IbilJardModel ibilJardBerria = (IbilJardModel) jardBerria;
                        mainApp.jardKargTaulaEguneratu(ibilJardBerria);
                        jardBistaratu(ibilJardBerria);
                    }
                    default -> {
                        mainApp.jardKargTaulaEguneratu(jardBerria);
                        jardBistaratu(jardBerria);
                    }
                }
            } else if (izenaAldatuta) {
                mainApp.jardKargTaulaEguneratu(jardBerria);
                jardBistaratu(jardBerria);
            }
        } else if (jarduera.getIdDB() >= 0) {
            if (YamaDBKud.getYamaDBKud().eguneratuJarduera(jardBerria.getIdDB(), jardBerria)) {
                JardueraModel bistaratzekoJard = YamaDBKud.getYamaDBKud().getJarduera(jardBerria.getIdDB());
                if (bistaratzekoJard != null) {
                    jardBistaratu(bistaratzekoJard);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
                    alert.setTitle("Yama");
                    alert.setHeaderText("Ustekabeko errorea.");
                    alert.showAndWait();
                    mainApp.atzeraJardBistaratu();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
                alert.setTitle("Yama");
                alert.setHeaderText("Ustekabeko errorea.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    void onClickJardGorde(MouseEvent event) {
        JardueraModel jardBerria = formateatuJard(jarduera);
        int emaitza = YamaDBKud.getYamaDBKud().jardueraGordeta(mainApp.getErabiltzaileAktibo().getEzizena(), jardBerria);
        if (emaitza == 0) {
            if (YamaDBKud.getYamaDBKud().gordeJarduera(mainApp.getErabiltzaileAktibo().getEzizena(), jardBerria)) {
                int jardId = YamaDBKud.getYamaDBKud().getAzkenJardId();
                if (jardId > -1) {
                    jardBerria.setIdDB(jardId);
                    jardBistaratu(jardBerria);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
                    alert.setTitle("Yama");
                    alert.setHeaderText("Ustekabeko errorea.");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
                alert.setTitle("Yama");
                alert.setHeaderText("Ustekabeko errorea.");
                alert.showAndWait();
            }
        } else if (emaitza == 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Datu hauek dituen jarduera bat existitzen da gordeta profil honetan.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Jarduera errepikatuta.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Ustekabeko errorea.");
            alert.showAndWait();
        }
    }

    private void setTextFieldProperty(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txt_izenBerria.getText().equals(jarduera.getIzenaBal()) && cmb_kirolMotaBerria.getSelectionModel().getSelectedItem().equals(jarduera.getMotaBal())) {
                btn_datuakEguneratu.setDisable(true);
            } else {
                btn_datuakEguneratu.setDisable(false);
            }
        });
    }

    private void setComboBoxProperty(ComboBox<String> cmb_kirolMotaBerria) {
        cmb_kirolMotaBerria.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (txt_izenBerria.getText().equals(jarduera.getIzenaBal()) && cmb_kirolMotaBerria.getSelectionModel().getSelectedItem().equals(jarduera.getMotaBal())) {
                btn_datuakEguneratu.setDisable(true);
            } else {
                btn_datuakEguneratu.setDisable(false);
            }
        });
    }

    private JardueraModel formateatuJard(JardueraModel jarduera) {
        int idDB = jarduera.getIdDB();
        String izena = jarduera.getIzenaBal();
        String mota = jarduera.getMotaBal();
        ArrayList<Double[]> koordZerr = jarduera.getKoordZerr();
        ArrayList<Double> altZerr = jarduera.getAltZerr();
        ArrayList<String> dataZerr = jarduera.getDataZerr();
        ArrayList<Integer> bihotzMaizZerr = jarduera.getBihotzMaizZerr();
        ArrayList<Integer> kadZerr = jarduera.getKadZerr();
        ArrayList<Integer> potZerr = jarduera.getPotZerr();
        ArrayList<Double> tenpZerr = jarduera.getTenpZerr();

        return switch (mota) {
            case "Txirrindularitza" -> new TxirrJardModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
            case "Korrika" -> new KorrJardModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
            case "Ibilaritza" -> new IbilJardModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
            default -> new JardueraModel(idDB, izena, mota, koordZerr, altZerr, dataZerr, bihotzMaizZerr, kadZerr, potZerr, tenpZerr);
        };
    }

    public void jardBistaratu(JardueraModel pJard) {
        jarduera = pJard;
        garbituPantaila();
        tabPane.getSelectionModel().select(0);

        lblIzena.setText(jarduera.getIzena());
        lblHasiData.setText(jarduera.getHasiData());

        if (jarduera.getIdDB() < 0 && mainApp.getErabiltzaileAktibo() != null) {
            btn_jardGorde.setVisible(true);
        } else {
            btn_jardGorde.setVisible(false);

            //TODO jarduera profil batean gordeta badago (erabiltzailearen izena jarri pantailan)
        }


        hasieratuMapa();

        estatistikakHasita = false;
        hasieratuEstatistikak();

        analisiaHasita = false;
        hasieratuAnalisia();

        //TODO mapa/estatistikak pantaila (haria ere)

        while (!estatistikakHasita/* || !analisiaHasita*/) { //TODO gehitu analisiaHasita aldagaia mapa/estatistikak pantaila egin eta gero
            try { //Hau ez badago programaren exekuzioa ez da aurera joaten
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void garbituPantaila() {
        btn_datuakEguneratu.setDisable(true);
        cmb_kirolMotaBerria.getSelectionModel().select(jarduera.getMotaBal());
        txt_izenBerria.setText(jarduera.getIzenaBal());

        scrll_Estatistikak.setVvalue(0);

        //TODO garbitu pantaila (analisia erlaitza)

        mapa.removeCoordinateLine(clKoords);
        mapa.removeMarker(hasiMarker);
        mapa.removeMarker(bukMarker);

        mapa_analisia = new MapView();
        mapa_analisia.setPrefWidth(438);
        mapa_analisia.setPrefHeight(414);
        pane_mapaAnalisia.getChildren().clear();
        pane_mapaAnalisia.getChildren().add(mapa_analisia);

        analisiTabHasieratuta = false;
        tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            if (!analisiTabHasieratuta && newTab.getText().equals("Analisia")) {
                eguneratuMapaAnalisia();
                analisiTabHasieratuta = true;
            }
        });

        //TODO erreseteatu mapa (analisia erlaitza)
    }


    private void hasieratuMapa() {
        clKoords = sortuIbilbidea();

        hasiKoord = new Coordinate(jarduera.getKoordZerr().get(0)[0], jarduera.getKoordZerr().get(0)[1]);
        bukKoord = new Coordinate(jarduera.getKoordZerr().get(jarduera.getKoordZerr().size() - 1)[0], jarduera.getKoordZerr().get(jarduera.getKoordZerr().size() - 1)[1]);

        hasiMarker = new Marker(Main.class.getResource("irudiak/start.png"), -12, -12).setPosition(hasiKoord);
        bukMarker = new Marker(Main.class.getResource("irudiak/stop.png"), -12, -12).setPosition(bukKoord);

        //TODO uste dut hau kendu daitekeela
        //ChangeListener<Boolean> trackVisibleListener = (observable, oldValue, newValue) -> mapa.setExtent(extent);
        //clKoords.visibleProperty().addListener(trackVisibleListener);

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
        koordsArray = new ArrayList<>();
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
        double minLatBerria = minLat - ((maxLat - minLat) * 0.1);
        double minLonBerria = minLon - ((maxLon - minLon) * 0.1);
        double maxLatBerria = maxLat + ((maxLat - minLat) * 0.1);
        double maxLonBerria = maxLon + ((maxLon - minLon) * 0.1);
        extent = Extent.forCoordinates(new Coordinate(minLatBerria, minLonBerria), new Coordinate(maxLatBerria, maxLonBerria));
    }

    private void afterMapIsInitialized() {
        mapa.setExtent(extent);
        mapa.addCoordinateLine(clKoords.setVisible(true)
                .setColor(Color.ORANGE).setWidth(5));
        mapa.addMarker(hasiMarker.setVisible(true));
        mapa.addMarker(bukMarker.setVisible(true));
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

    private void hasieratuAnalisia() {
        cmb_analisia.getItems().clear();
        cmb_analisia.getItems().add("Abiadura");
        if (jarduera.getAltZerr() != null) cmb_analisia.getItems().add("Altitudea");
        if (jarduera.getBihotzMaizZerr() != null) cmb_analisia.getItems().add("Bihotz-maiztasuna");
        if (jarduera instanceof TxirrJardModel && jarduera.getPotZerr() != null) cmb_analisia.getItems().add("Potentzia");
        cmb_analisia.getSelectionModel().select("Abiadura");

        hasieratuMapaAnalisia();
        //TODO
        //TODO
        //TODO
        //TODO
        analisiaHasita = true;
    }

    private void hasieratuMapaAnalisia() {
        analisiMarker = new Marker(Main.class.getResource("irudiak/point.png"), -12, -12).setPosition(hasiKoord);

        mapa_analisia.initializedProperty().addListener((observable, oldValue, newValue) -> { //Mapa hasieratzean
            if (newValue) {
                while (!mapa_analisia.getInitialized()) {
                    try { //Hau ez badago programaren exekuzioa ez da aurera joaten
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                afterAnalisiaMapIsInitialized();
            }
        });

        if (!mapa_analisia.getInitialized()) { //Mapa hasieratuta ez badago
            mapa_analisia.initialize(Configuration.builder()
                    .interactive(false)
                    .showZoomControls(false)
                    .build());
        } else { //Mapa hasieratuta badago
            afterAnalisiaMapIsInitialized();
        }
    }

    private void afterAnalisiaMapIsInitialized() {
        mapa_analisia.setExtent(extent);
        mapa_analisia.addMarker(hasiMarker.setVisible(true));
        mapa_analisia.addMarker(bukMarker.setVisible(true));
        mapa_analisia.addMarker(analisiMarker.setVisible(true));
    }

    private void eguneratuMapaAnalisia() { //TODO metodo honi egindako deiak kudeatu (hasieraketetan batez ere)
        for (CoordinateLine cl : clArray) {
            mapa_analisia.removeCoordinateLine(cl);
        }
        clArray.clear();

        analisiMarker.setPosition(hasiKoord);

        if (cmb_analisia.getSelectionModel().getSelectedItem().equals("Abiadura")) abiAnalisiaErakutsi();
        else if (cmb_analisia.getSelectionModel().getSelectedItem().equals("Altitudea")) altAnalisiaErakutsi();
        else if (cmb_analisia.getSelectionModel().getSelectedItem().equals("Bihotz-maiztasuna")) bMAnalisiaErakutsi();
        else if (cmb_analisia.getSelectionModel().getSelectedItem().equals("Potentzia")) potAnalisiaErakutsi();
    }

    private void abiAnalisiaErakutsi() {
        lbl_anal1Txt.setText("Abiadura");
        lbl_anal2Txt.setText("Distantzia");
        lbl_anal3Txt.setText("Iraupena");
        lbl_anal1.setText("-");
        lbl_anal2.setText("-");
        lbl_anal3.setText("-");

        for (int i = 0; i < koordsArray.size() - 1; i++) {
            CoordinateLine cl = new CoordinateLine(koordsArray.get(i), koordsArray.get(i + 1));

            Color color;
            if (jarduera.getAbiZerr().get(i + 1) == null) color = Color.GRAY;
            else {
                double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * jarduera.getAbiZerr().get(i + 1) / jarduera.getAbiMaxBal();
                color = Color.hsb(hue, 1.0, 1.0);
            }

            mapa_analisia.addCoordinateLine(cl.setVisible(true).setColor(color).setWidth(5));
        }

        analisiAbiGraf();
    }

    private void analisiAbiGraf() {

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
        analisiAbiGraf = sortuGrafikoa(distZerr, abiZerr, xMax, 0, abiMax, String.valueOf(jarduera.getBbAbiBal()), xLabel, yLabel, kolorea);

        grafMouseMovedListener(analisiAbiGraf, "Abiadura");
        jarriGrafikoa(analisiAbiGraf, analisiGrafPane);
    }

    private void altAnalisiaErakutsi() {
        lbl_anal1Txt.setText("Altitudea");
        lbl_anal2Txt.setText("Distantzia");
        lbl_anal3Txt.setText("Abiadura");
        lbl_anal1.setText("-");
        lbl_anal2.setText("-");
        lbl_anal3.setText("-");

        for (int i = 0; i < koordsArray.size() - 1; i++) {
            CoordinateLine cl = new CoordinateLine(koordsArray.get(i), koordsArray.get(i + 1));

            Color color;
            if (jarduera.getAltZerr().get(i + 1) == null) color = Color.GRAY;
            else {
                double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * (jarduera.getAltZerr().get(i + 1) - jarduera.getAltueraMinBal()) / (jarduera.getAltueraMaxBal() - jarduera.getAltueraMinBal());
                color = Color.hsb(hue, 1.0, 1.0);
            }

            mapa_analisia.addCoordinateLine(cl.setVisible(true).setColor(color).setWidth(5));
        }

        analisiAltGraf();
    }

    private void analisiAltGraf() {

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
        analisiAltGraf = sortuGrafikoa(distZerr, altZerr, xMax, altMin, altMax, null, xLabel, yLabel, kolorea);

        grafMouseMovedListener(analisiAltGraf, "Altitudea");
        jarriGrafikoa(analisiAltGraf, analisiGrafPane);
    }

    private void bMAnalisiaErakutsi() {
        lbl_anal1Txt.setText("Bihotz-maiztasuna");
        lbl_anal2Txt.setText("Distantzia");
        lbl_anal3Txt.setText("Abiadura");
        lbl_anal1.setText("-");
        lbl_anal2.setText("-");
        lbl_anal3.setText("-");

        for (int i = 0; i < koordsArray.size() - 1; i++) {
            CoordinateLine cl = new CoordinateLine(koordsArray.get(i), koordsArray.get(i + 1));

            Color color;
            if (jarduera.getBihotzMaizZerr().get(i + 1) == null) color = Color.GRAY;
            else {
                double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * (jarduera.getBihotzMaizZerr().get(i + 1) - jarduera.getBihotzMaizMinBal()) / (jarduera.getBihotzMaizMaxBal() - jarduera.getBihotzMaizMinBal());
                color = Color.hsb(hue, 1.0, 1.0);
            }

            mapa_analisia.addCoordinateLine(cl.setVisible(true).setColor(color).setWidth(5));
        }

        analisiBMGraf();
    }

    private void analisiBMGraf() {

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
        analisiBMGraf = sortuGrafikoa(distZerr, bihotzMaizZerr, xMax, bihotzMaizMin, bihotzMaizMax, jarduera.getBbBihotzMaizBal(), xLabel, yLabel, kolorea);

        grafMouseMovedListener(analisiBMGraf, "Bihotz-maiztasuna");
        jarriGrafikoa(analisiBMGraf, analisiGrafPane);
    }

    private void potAnalisiaErakutsi() {
        lbl_anal1Txt.setText("Potentzia");
        lbl_anal2Txt.setText("Distantzia");
        lbl_anal3Txt.setText("Abiadura");
        lbl_anal1.setText("-");
        lbl_anal2.setText("-");
        lbl_anal3.setText("-");

        for (int i = 0; i < koordsArray.size() - 1; i++) {
            CoordinateLine cl = new CoordinateLine(koordsArray.get(i), koordsArray.get(i + 1));

            Color color;
            if (jarduera.getPotZerr().get(i + 1) == null) color = Color.GRAY;
            else {
                double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * jarduera.getPotZerr().get(i + 1) / jarduera.getPotMaxBal();
                color = Color.hsb(hue, 1.0, 1.0);
            }

            mapa_analisia.addCoordinateLine(cl.setVisible(true).setColor(color).setWidth(5));
        }

        analisiPotGraf();
    }

    private void analisiPotGraf() {

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
        analisiPotGraf = sortuGrafikoa(distZerr, potZerr, xMax, 0, potMax, ((TxirrJardModel) jarduera).getBbPotBal(), xLabel, yLabel, kolorea);

        grafMouseMovedListener(analisiPotGraf, "Potentzia");
        jarriGrafikoa(analisiPotGraf, analisiGrafPane);
    }

    private void grafMouseMovedListener(AreaChart<Number, Number> analisiGraf, String mota) {
        Region plotArea = (Region) analisiGraf.lookup(".chart-plot-background");
        Pane chartContent = (Pane) analisiGraf.lookup(".chart-content");
        Line line = new Line();
        chartContent.getChildren().add(line);

        analisiGraf.setOnMouseMoved((MouseEvent event) -> {
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
            double x = analisiGraf.getXAxis().sceneToLocal(mouseSceneCoords).getX();
            if (x >= 0 && x <= plotArea.getWidth()) {
                double distGraf = (x / plotArea.getWidth()) * jarduera.getDistBal();
                int zerrInd = lortuKoordGraf(distGraf);

                marraztuLerroa(x, plotArea, chartContent, line);
                analisiMarker.setPosition(new Coordinate(jarduera.getKoordZerr().get(zerrInd)[0], jarduera.getKoordZerr().get(zerrInd)[1]));

                if (mota.equals("Abiadura")) {
                    lbl_anal1.setText(jarduera.getAbiadura(zerrInd));
                    lbl_anal2.setText(jarduera.getDistPunt(zerrInd));
                    lbl_anal3.setText(jarduera.getDenbPunt(zerrInd));
                } else if (mota.equals("Altitudea")) {
                    lbl_anal1.setText(jarduera.getAltuera(zerrInd));
                    lbl_anal2.setText(jarduera.getDistPunt(zerrInd));
                    lbl_anal3.setText(jarduera.getAbiadura(zerrInd));
                } else if (mota.equals("Bihotz-maiztasuna")) {
                    lbl_anal1.setText(jarduera.getBihotzMaiz(zerrInd));
                    lbl_anal2.setText(jarduera.getDistPunt(zerrInd));
                    lbl_anal3.setText(jarduera.getAbiadura(zerrInd));
                } else if (mota.equals("Potentzia")) {
                    lbl_anal1.setText(jarduera.getPot(zerrInd));
                    lbl_anal2.setText(jarduera.getDistPunt(zerrInd));
                    lbl_anal3.setText(jarduera.getAbiadura(zerrInd));
                }
            }
        });
    }

    private int lortuKoordGraf(double distGraf) {
        boolean aurkituta = false;
        int i = 0;
        while (!aurkituta && i < jarduera.getDistZerr().size()) {
            if (distGraf <= jarduera.getDistZerr().get(i)) aurkituta = true;
            else i++;
        }
        return i;
    }

    //https://stackoverflow.com/a/40730299
    private void marraztuLerroa(double x, Region plotArea, Pane chartContent, Line line) {
        Point2D a = plotArea.localToScene(new Point2D(x, 0));
        Point2D b = plotArea.localToScene(new Point2D(x, plotArea.getHeight()));

        Point2D aTrans = chartContent.sceneToLocal(a);
        Point2D bTrans = chartContent.sceneToLocal(b);

        line.setStartX(aTrans.getX());
        line.setStartY(aTrans.getY());
        line.setEndX(bTrans.getX());
        line.setEndY(bTrans.getY());
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

    //TODO jarri hemen hasieratuAnalisia()

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
        panela.getChildren().clear();
        grafikoa.setPrefWidth(panela.getPrefWidth());
        grafikoa.setPrefHeight(panela.getPrefHeight());
        panela.getChildren().add(grafikoa);
    }
}

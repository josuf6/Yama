package com.yama.controllers.ui;

import com.yama.Main;
import com.yama.controllers.db.YamaDBKud;
import com.yama.models.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;

public class ProfilaBistaratuKud implements Initializable {

    private Main mainApp;
    private ErabiltzaileModel erabiltzailea = null;
    private ArrayList<JardueraModel> jardZerr;

    @FXML
    private AnchorPane pane_jardEz;

    @FXML
    private Button btn_datuakEguneratu;

    @FXML
    private Label lbl_ezizena, lbl_izenAbizen;

    @FXML
    private PasswordField txt_pasahitzBerri;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField txt_abiBerri, txt_ezizenBerri, txt_izenBerri;

    @FXML
    private VBox vBox_jarduerak;

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
        tabPane.getSelectionModel().select(0);
        scrollPane.setVvalue(0);

        lbl_izenAbizen.setText("-");
        lbl_ezizena.setText("-");

        btn_datuakEguneratu.setDisable(true);
        txt_izenBerri.clear();
        txt_abiBerri.clear();
        txt_ezizenBerri.clear();
        txt_pasahitzBerri.clear();

        vBox_jarduerak.getChildren().clear();
    }

    public void eguneratuPantaila() {
        erabiltzailea = mainApp.getErabiltzaileAktibo();

        garbituPantaila();
        datuakJarri();
        jarduerakJarri();
    }

    private void datuakJarri() {
        lbl_izenAbizen.setText(erabiltzailea.getIzena() + " " + erabiltzailea.getAbizena());
        lbl_ezizena.setText(erabiltzailea.getEzizena());
    }

    private void jarduerakJarri() {
        jardZerr = YamaDBKud.getYamaDBKud().getErabJarduerak(erabiltzailea.getEzizena());

        if (jardZerr != null && jardZerr.size() > 0) {
            Collections.sort(jardZerr, Comparator.comparing(JardueraModel::getHasiData));
            Collections.reverse(jardZerr);

            for (JardueraModel jarduera : jardZerr) {
                vBox_jarduerak.getChildren().add(sortuJardTxartela(jarduera));
            }
            scrollPane.toFront();
        } else {
            pane_jardEz.toFront();
        }
    }

    private AnchorPane sortuJardTxartela(JardueraModel jarduera) {
        AnchorPane jardTxartela = new AnchorPane();

        jardTxartela.setPrefSize(600, 180);
        jardTxartela.setMinSize(600, 180);
        jardTxartela.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        VBox.setMargin(jardTxartela, new Insets(0, 0, 18, 168));

        Label lbl_izena = new Label();
        lbl_izena.setMaxWidth(500);
        lbl_izena.setFont(Font.font("System", FontWeight.BOLD, 18));
        lbl_izena.setLayoutX(20);
        lbl_izena.setLayoutY(15);
        lbl_izena.setText(jarduera.getIzena());

        lbl_izena.setCursor(Cursor.HAND);
        lbl_izena.setOnMouseClicked(event -> mainApp.jardBistaratu(jarduera));
        lbl_izena.setOnMouseEntered(event -> lbl_izena.setTextFill(Color.rgb(13,130,0)));
        lbl_izena.setOnMouseExited(event -> lbl_izena.setTextFill(Color.BLACK));

        ImageView img_trash = new ImageView();
        img_trash.setFitWidth(30);
        img_trash.setFitHeight(30);
        img_trash.setLayoutX(550);
        img_trash.setLayoutY(15);

        Image irudi_trash_grisa = new Image(Main.class.getResource("irudiak/trash_grisa.png").toString());
        Image irudi_trash_gorria = new Image(Main.class.getResource("irudiak/trash_gorria.png").toString());

        img_trash.setImage(irudi_trash_grisa);

        img_trash.setCursor(Cursor.HAND);
        img_trash.setOnMouseEntered(event -> img_trash.setImage(irudi_trash_gorria));
        img_trash.setOnMouseExited(event -> img_trash.setImage(irudi_trash_grisa));
        img_trash.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Jarduerarekin erlazionatutako datu guztiak ezabatuko dira.", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Yama");
            alert.setHeaderText("Ezabatu jarduera?");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    if (YamaDBKud.getYamaDBKud().ezabatuJarduera(jarduera.getIdDB())) {
                        eguneratuPantaila();
                    } else {
                        Alert alert2 = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
                        alert2.setTitle("Yama");
                        alert2.setHeaderText("Ustekabeko errorea.");
                        alert2.showAndWait();
                    }
                }
            });
        });

        ImageView img_mota = new ImageView();
        img_mota.setFitWidth(20);
        img_mota.setFitHeight(20);
        img_mota.setLayoutX(20);
        img_mota.setLayoutY(45);

        Image irudia;
        if (jarduera instanceof TxirrJardModel) {
            irudia = new Image(Main.class.getResource("irudiak/cycling.png").toString());
        } else if (jarduera instanceof KorrJardModel) {
            irudia = new Image(Main.class.getResource("irudiak/running.png").toString());
        } else if (jarduera instanceof IbilJardModel) {
            irudia = new Image(Main.class.getResource("irudiak/walking.png").toString());
        } else {
            irudia = new Image(Main.class.getResource("irudiak/sport.png").toString());
        }
        img_mota.setImage(irudia);

        Label lbl_hasiData = new Label();
        lbl_hasiData.setFont(Font.font("System", 12));
        lbl_hasiData.setLayoutX(50);
        lbl_hasiData.setLayoutY(47);
        lbl_hasiData.setText(jarduera.getHasiData());

        HBox hBox_datuak = getHBoxDatuak(jarduera);

        Line lerro1 = new Line();
        lerro1.setStyle("-fx-stroke: rgb(200,200,200);");
        lerro1.setStartX(200);
        lerro1.setStartY(90);
        lerro1.setEndX(200);
        lerro1.setEndY(150);

        Line lerro2 = new Line();
        lerro2.setStyle("-fx-stroke: rgb(200,200,200);");
        lerro2.setStartX(400);
        lerro2.setStartY(90);
        lerro2.setEndX(400);
        lerro2.setEndY(150);

        jardTxartela.getChildren().add(lbl_izena);
        jardTxartela.getChildren().add(img_trash);
        jardTxartela.getChildren().add(img_mota);
        jardTxartela.getChildren().add(lbl_hasiData);
        jardTxartela.getChildren().add(hBox_datuak);
        jardTxartela.getChildren().add(lerro1);
        jardTxartela.getChildren().add(lerro2);

        return jardTxartela;
    }

    private HBox getHBoxDatuak(JardueraModel pJarduera) {
        HBox hBox_datuak = new HBox();
        hBox_datuak.setAlignment(Pos.CENTER);
        hBox_datuak.setPrefWidth(600);
        hBox_datuak.setPrefHeight(80);
        hBox_datuak.setLayoutX(0);
        hBox_datuak.setLayoutY(80);

        VBox vBox_distantzia = getDistantziaVBox(pJarduera.getDistantzia());
        VBox vBox_iraupena = getIraupenaVBox(pJarduera.getIraupena());
        VBox vBox_abiadura = getAbiaduraVBox(pJarduera);

        hBox_datuak.getChildren().add(vBox_distantzia);
        hBox_datuak.getChildren().add(vBox_iraupena);
        hBox_datuak.getChildren().add(vBox_abiadura);

        return hBox_datuak;
    }

    private VBox getDistantziaVBox(String pDistantzia) {
        VBox vBox_distantzia = new VBox();
        vBox_distantzia.setAlignment(Pos.CENTER);
        vBox_distantzia.setPrefWidth(200);
        vBox_distantzia.setSpacing(10);

        Label lbl_dist_txt = new Label();
        lbl_dist_txt.setFont(Font.font("System", 14));
        lbl_dist_txt.setTextAlignment(TextAlignment.CENTER);
        lbl_dist_txt.setText("Distantzia");

        Label lbl_dist = new Label();
        lbl_dist.setFont(Font.font("System", 18));
        lbl_dist.setTextAlignment(TextAlignment.CENTER);
        lbl_dist.setText(pDistantzia);

        vBox_distantzia.getChildren().add(lbl_dist_txt);
        vBox_distantzia.getChildren().add(lbl_dist);

        return vBox_distantzia;
    }

    private VBox getIraupenaVBox(String pIraupena) {
        VBox vBox_iraupena = new VBox();
        vBox_iraupena.setAlignment(Pos.CENTER);
        vBox_iraupena.setPrefWidth(200);
        vBox_iraupena.setSpacing(10);

        Label lbl_irau_txt = new Label();
        lbl_irau_txt.setFont(Font.font("System", 14));
        lbl_irau_txt.setTextAlignment(TextAlignment.CENTER);
        lbl_irau_txt.setText("Iraupena");

        Label lbl_irau = new Label();
        lbl_irau.setFont(Font.font("System", 18));
        lbl_irau.setTextAlignment(TextAlignment.CENTER);
        lbl_irau.setText(pIraupena);

        vBox_iraupena.getChildren().add(lbl_irau_txt);
        vBox_iraupena.getChildren().add(lbl_irau);

        return vBox_iraupena;
    }

    private VBox getAbiaduraVBox(JardueraModel pJarduera) {
        VBox vBox_abiadura = new VBox();
        vBox_abiadura.setAlignment(Pos.CENTER);
        vBox_abiadura.setPrefWidth(200);
        vBox_abiadura.setSpacing(10);

        Label lbl_abi_txt = new Label();
        lbl_abi_txt.setFont(Font.font("System", 14));
        lbl_abi_txt.setTextAlignment(TextAlignment.CENTER);

        if (pJarduera instanceof IbilJardModel || pJarduera instanceof KorrJardModel) {
            lbl_abi_txt.setText("Batez besteko erritmoa");
        } else {
            lbl_abi_txt.setText("Batez besteko abiadura");
        }

        Label lbl_abi = new Label();
        lbl_abi.setFont(Font.font("System", 18));
        lbl_abi.setTextAlignment(TextAlignment.CENTER);
        lbl_abi.setText(pJarduera.getBbAbiadura());

        vBox_abiadura.getChildren().add(lbl_abi_txt);
        vBox_abiadura.getChildren().add(lbl_abi);

        return vBox_abiadura;
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ezin izan da erabiltzailea eguneratu. Berriro saiatu.", ButtonType.CLOSE);
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

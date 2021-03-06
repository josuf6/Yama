package com.yama.controllers.ui;

import com.yama.Main;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class MainKud implements Initializable {

    private Main mainApp;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private Label lbl_estatistikak, lbl_jarduera, lbl_profila;

    @FXML
    private AnchorPane pane_barra, pane_beltza, pane_Erregistratu, pane_estatistikak, pane_EstatistikakIkusi, pane_itxi,
            pane_jarduera, pane_profila, pane_JardBistaratu, pane_JardueraKargatu, pane_menu, pane_menuIzenak,
            pane_menuIkonoak, pane_minimizatu, pane_ProfilaBistaratu, pane_SaioaHasi;

    public MainKud(Main main) {
        mainApp = main;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //"X" ikurra sakatzean aplikazioa ixteko
        pane_itxi.setOnMouseClicked(event -> System.exit(0));

        pane_itxi.setOnMouseEntered(event -> pane_itxi.setStyle("-fx-background-color: rgb(200,0,0); -fx-background-radius: 5.0"));

        pane_itxi.setOnMouseExited(event -> pane_itxi.setStyle("-fx-background-color: rgb(10,100,0); -fx-background-radius: 5.0"));


        //"-" ikurra sakatzean aplikazioa minimizatzeko
        pane_minimizatu.setOnMousePressed(event -> pane_minimizatu.setStyle("-fx-background-color: rgb(10,100,0); -fx-background-radius: 5.0"));

        pane_minimizatu.setOnMouseClicked(event -> Main.getStage().setIconified(true));

        pane_minimizatu.setOnMouseEntered(event -> pane_minimizatu.setStyle("-fx-background-color: rgb(13,130,0); -fx-background-radius: 5.0"));

        pane_minimizatu.setOnMouseExited(event -> pane_minimizatu.setStyle("-fx-background-color: rgb(10,100,0); -fx-background-radius: 5.0"));


        //Leihoa mugitu ahal izateko
        pane_barra.setOnMousePressed(event -> {
            if (event.getSource() != pane_menu) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        pane_barra.setOnMouseDragged(event -> {
            if (event.getSource() != pane_menu) {
                Main.getStage().setX(event.getScreenX() - xOffset);
                Main.getStage().setY(event.getScreenY() - yOffset);
            }
        });


        //Alboko menuaren hasierako egoera
        pane_beltza.setVisible(false);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.1), pane_beltza);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        fadeTransition.play();

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.1), pane_menuIzenak);
        translateTransition.setByX(-180);
        translateTransition.play();

        mainApp.menuIrekita = false;


        //pane_menu-ren (alboko menua irekitzeko eta ixteko erabiltzen den botoia) gertaerak
        pane_menu.setOnMouseClicked(event -> {
            if (mainApp.menuIrekita) {
                pane_menu.setStyle("-fx-background-color: rgb(13,130,0); -fx-background-radius: 5.0");
                menuItxi();
            } else {
                pane_menu.setStyle("-fx-background-color: rgb(7,70,0); -fx-background-radius: 5.0");
                menuIreki();
            }
        });

        pane_menu.setOnMouseEntered(event -> {
            if (!mainApp.menuIrekita) {
                pane_menu.setStyle("-fx-background-color: rgb(13,130,0); -fx-background-radius: 5.0");
            }
        });

        pane_menu.setOnMouseExited(event -> {
            if (!mainApp.menuIrekita) {
                pane_menu.setStyle("-fx-background-color: rgb(10,100,0); -fx-background-radius: 5.0");
            }
        });


        //alboko menua irekita dagoenean, pane_beltza (ilundutako leihoaren eremua) sakatzean menua ixteko
        pane_beltza.setOnMouseClicked(event -> {
            pane_menu.setStyle("-fx-background-color: rgb(10,100,0); -fx-background-radius: 5.0");
            menuItxi();
        });


        //aplikazioa irekitzean profilaren pantaila bistaratzeko
        if (mainApp.getErabiltzaileAktibo() == null) kenduEstatistikakAukera();
        else erakutsiEstatistikakAukera();
        erakutsiProfila();
        ordenaEgokitu();
    }

    @FXML
    void onClick(MouseEvent event) { //alboko menuko elementu bat klikatzerakoan egin behar dena

        //klikatutako elementua aukeratutakoa ez bada horrekin erlazionatutako leihoa erakutsi
        if (event.getSource() == lbl_profila || event.getSource() == pane_profila) {
            erakutsiProfila();
            ordenaEgokitu();
        } else if (event.getSource() == lbl_jarduera || event.getSource() == pane_jarduera) {
            erakutsiJardueraKargatu();
            ordenaEgokitu();
        } else if (event.getSource() == lbl_estatistikak || event.getSource() == pane_estatistikak) {
            erakutsiEstatistikakIkusi();
            ordenaEgokitu();
        }

        if (mainApp.menuIrekita) { //alboko menua itxi irekita badago
            pane_menu.setStyle("-fx-background-color: rgb(10,100,0); -fx-background-radius: 5.0");
            menuItxi();
        }
    }

    @FXML
    void onEnter(MouseEvent event) { //kurtsorea alboko menuko elementu baten gainetik sartzean horren koloreak aldatu
        if (mainApp.leihoAktibo != 1 && (event.getSource() == lbl_profila || event.getSource() == pane_profila)) {
            lbl_profila.setStyle("-fx-background-color: rgba(10,100,0,0.2)");
            pane_profila.setStyle("-fx-background-color: rgba(10,100,0,0.2)");
        } else if (mainApp.leihoAktibo != 2 && (event.getSource() == lbl_jarduera || event.getSource() == pane_jarduera)) {
            lbl_jarduera.setStyle("-fx-background-color: rgba(10,100,0,0.2)");
            pane_jarduera.setStyle("-fx-background-color: rgba(10,100,0,0.2)");
        } else if (mainApp.leihoAktibo != 3 && (event.getSource() == lbl_estatistikak || event.getSource() == pane_estatistikak)) {
            lbl_estatistikak.setStyle("-fx-background-color: rgba(10,100,0,0.2)");
            pane_estatistikak.setStyle("-fx-background-color: rgba(10,100,0,0.2)");
        }
    }

    @FXML
    void onExit(MouseEvent event) { //kurtsorea alboko menuko elementu baten gainetik irtetzean horren koloreak aldatu
        if (mainApp.leihoAktibo != 1 && (event.getSource() == lbl_profila || event.getSource() == pane_profila)) {
            lbl_profila.setStyle("-fx-background-color: White");
            pane_profila.setStyle("-fx-background-color: White");
        } else if (mainApp.leihoAktibo != 2 && (event.getSource() == lbl_jarduera || event.getSource() == pane_jarduera)) {
            lbl_jarduera.setStyle("-fx-background-color: White");
            pane_jarduera.setStyle("-fx-background-color: White");
        } else if (mainApp.leihoAktibo != 3 && (event.getSource() == lbl_estatistikak || event.getSource() == pane_estatistikak)) {
            lbl_estatistikak.setStyle("-fx-background-color: White");
            pane_estatistikak.setStyle("-fx-background-color: White");
        }
    }

    private void ordenaEgokitu() { //leihoaren elementuen ordena egokitzeko
        pane_beltza.toFront();
        pane_menuIzenak.toFront();
        pane_menuIkonoak.toFront();
    }

    public void setProfilaText(String pText) {
        lbl_profila.setText(pText);
    }

    private void erakutsiProfila() {
        mainApp.leihoAktibo = 1;
        lbl_profila.setStyle("-fx-background-color: rgba(10,100,0,0.5)");
        pane_profila.setStyle("-fx-background-color: rgba(10,100,0,0.5)");

        elementuakZuriz();

        if (mainApp.getErabiltzaileAktibo() == null) {
            mainApp.erakutsiSaioaHasi();
        } else {
            mainApp.erakutsiProfilaBistaratu();
        }
    }

    private void erakutsiJardueraKargatu() {
        mainApp.leihoAktibo = 2;
        lbl_jarduera.setStyle("-fx-background-color: rgba(10,100,0,0.5)");
        pane_jarduera.setStyle("-fx-background-color: rgba(10,100,0,0.5)");

        elementuakZuriz();

        pane_JardueraKargatu.toFront();
    }

    private void erakutsiEstatistikakIkusi() {
        mainApp.leihoAktibo = 3;
        lbl_estatistikak.setStyle("-fx-background-color: rgba(10,100,0,0.5)");
        pane_estatistikak.setStyle("-fx-background-color: rgba(10,100,0,0.5)");

        elementuakZuriz();

        mainApp.erakutsiEstatistikakIkusi();
        pane_EstatistikakIkusi.toFront();
    }

    public void erakutsiEstatistikakAukera() {
        lbl_estatistikak.setVisible(true);
        lbl_estatistikak.setManaged(true);
        pane_estatistikak.setVisible(true);
        pane_estatistikak.setManaged(true);
    }

    public void kenduEstatistikakAukera() {
        lbl_estatistikak.setVisible(false);
        lbl_estatistikak.setManaged(false);
        pane_estatistikak.setVisible(false);
        pane_estatistikak.setManaged(false);
    }

    public void erakutsiSaioaHasi() {
        pane_SaioaHasi.toFront();
        pane_SaioaHasi.requestFocus();
    }

    public void erakutsiProfilaBistaratu() {
        pane_ProfilaBistaratu.toFront();
        pane_ProfilaBistaratu.requestFocus();
    }

    public void erakutsiErregistratu() {
        pane_Erregistratu.toFront();
        pane_Erregistratu.requestFocus();
    }

    public void erakutsiJardBistaratu() {
        pane_JardBistaratu.toFront();
        pane_JardBistaratu.requestFocus();
    }

    public void atzeraJardBistaratu() {
        pane_JardBistaratu.toBack();
    }

    private void elementuakZuriz() { //alboko menuan zuriz jarri aukeratuak izan ez diren elementuak
        if (mainApp.leihoAktibo != 1) {
            lbl_profila.setStyle("-fx-background-color: White");
            pane_profila.setStyle("-fx-background-color: White");
        }
        if (mainApp.leihoAktibo != 2) {
            lbl_jarduera.setStyle("-fx-background-color: White");
            pane_jarduera.setStyle("-fx-background-color: White");
        }
        if (mainApp.leihoAktibo != 3) {
            lbl_estatistikak.setStyle("-fx-background-color: White");
            pane_estatistikak.setStyle("-fx-background-color: White");
        }
    }

    private void menuIreki() {
        if (!mainApp.menuMugitzen) { //alboko menua ireki eta animazioak abiarazi
            mainApp.menuIrekita = true;
            mainApp.menuMugitzen = true;
            pane_beltza.setVisible(true);

            FadeTransition fadeTransition2 = new FadeTransition(Duration.seconds(0.1), pane_beltza);
            fadeTransition2.setFromValue(0);
            fadeTransition2.setToValue(0.3);
            fadeTransition2.play();

            TranslateTransition translateTransition2 = new TranslateTransition(Duration.seconds(0.1), pane_menuIzenak);
            translateTransition2.setByX(+180);
            translateTransition2.play();

            translateTransition2.setOnFinished(event -> {
                mainApp.menuMugitzen = false;
            });
        }
    }

    private void menuItxi() {
        if (!mainApp.menuMugitzen) { //alboko menua itxi eta animazioak abiarazi
            mainApp.menuIrekita = false;
            mainApp.menuMugitzen = true;

            FadeTransition fadeTransition3 = new FadeTransition(Duration.seconds(0.1), pane_beltza);
            fadeTransition3.setFromValue(0.3);
            fadeTransition3.setToValue(0);
            fadeTransition3.play();

            fadeTransition3.setOnFinished(event -> pane_beltza.setVisible(false));

            TranslateTransition translateTransition3 = new TranslateTransition(Duration.seconds(0.1), pane_menuIzenak);
            translateTransition3.setByX(-180);
            translateTransition3.play();

            translateTransition3.setOnFinished(event -> {
                mainApp.menuMugitzen = false;
            });
        }
    }
}
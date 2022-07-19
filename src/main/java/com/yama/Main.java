package com.yama;

import com.yama.controllers.db.YamaDBKud;
import com.yama.controllers.ui.*;
import com.yama.models.ErabiltzaileModel;
import com.yama.models.JardueraModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;

public class Main extends Application {

    private Parent mainUI;

    private static Stage stage;
    private static Scene scene;

    private MainKud mainKud;
    private SaioaHasiKud saioaHasiKud;
    private ErregistratuKud erregistratuKud;
    private ProfilaBistaratuKud profilaBistaratuKud;
    private JardueraKargatuKud jardueraKargatuKud;
    private JardBistaratuKud jardBistaratuKud;

    public int lehioAktibo;
    private ErabiltzaileModel erabiltzaileAktibo = null;

    public boolean menuIrekita = false;
    public boolean menuMugitzen = false;

    public static void main(String[] args) {
        launch();
    }

    public static Stage getStage() {
        return stage;
    }

    public static Scene getScene() {
        return scene;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("irudiak/yama.png")));

        this.mainErakutsi();
    }

    private void mainErakutsi() throws IOException {
        this.pantailakKargatu();

        //eszena nagusia bistaratzeko
        scene = new Scene(mainUI);
        stage.setScene(scene);
        stage.show();
    }

    private void pantailakKargatu() throws IOException {
        FXMLLoader loaderMain = new FXMLLoader(Main.class.getResource("FXML/Main.fxml"));

        //kudeatzaileak hasieratu
        mainKud = new MainKud(this);
        saioaHasiKud = new SaioaHasiKud(this);
        erregistratuKud = new ErregistratuKud(this);
        profilaBistaratuKud = new ProfilaBistaratuKud(this);
        jardueraKargatuKud = new JardueraKargatuKud(this);
        jardBistaratuKud = new JardBistaratuKud(this);

        Callback<Class<?>, Object> controllerFactory = type -> {
            if (type == MainKud.class) {
                return mainKud;
            } else if (type == SaioaHasiKud.class) {
                return saioaHasiKud;
            } else if (type == ErregistratuKud.class) {
                return erregistratuKud;
            } else if (type == ProfilaBistaratuKud.class) {
                return profilaBistaratuKud;
            } else if (type == JardueraKargatuKud.class) {
                return jardueraKargatuKud;
            } else if (type == JardBistaratuKud.class) {
                return jardBistaratuKud;
            } else {
                try {
                    return type.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        };

        loaderMain.setControllerFactory(controllerFactory);

        mainUI = (Parent) loaderMain.load();
    }

    public ErabiltzaileModel getErabiltzaileAktibo() {
        return erabiltzaileAktibo;
    }

    public void erakutsiSaioaHasi() {
        saioaHasiKud.garbituPantaila();
        mainKud.erakutsiSaioaHasi();
    }

    public void erakutsiErregistratu() {
        erregistratuKud.garbituPantaila();
        mainKud.erakutsiErregistratu();
    }

    public void saioaHasi(String pEzizena) {
        erabiltzaileAktibo = YamaDBKud.getYamaDBKud().getErabiltzailea(pEzizena);
        if (erabiltzaileAktibo != null) {
            mainKud.setProfilaText(erabiltzaileAktibo.getEzizena());
            erakutsiProfilaBistaratu();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ustekabeko errore bat egon da. Berrio saiatu.", ButtonType.CLOSE);
            alert.setTitle("Yama");
            alert.setHeaderText("Ustekabeko errorea.");
            alert.showAndWait();
            saioaItxi();
        }
    }

    public void saioaItxi() {
        erabiltzaileAktibo = null;
        mainKud.setProfilaText("Saioa hasi");
        erakutsiSaioaHasi();
    }

    public void erakutsiProfilaBistaratu() {
        profilaBistaratuKud.eguneratuPantaila();
        mainKud.erakutsiProfilaBistaratu();
    }

    public void jardBistaratu(JardueraModel pJard) {
        jardBistaratuKud.jardBistaratu(pJard);
        mainKud.erakutsiJardBistaratu();
    }

    public void atzeraJardBistaratu() {
        if (lehioAktibo == 1) {
            profilaBistaratuKud.eguneratuPantaila();
        }
        mainKud.atzeraJardBistaratu();
    }

    public void jardKargTaulaEguneratu(JardueraModel jardEguneratuta) {
        jardueraKargatuKud.eguneratuJarduera(jardEguneratuta);
    }
}
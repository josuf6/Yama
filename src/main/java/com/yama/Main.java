package com.yama;

import com.yama.controllers.ui.JardueraKargatuKud;
import com.yama.controllers.ui.MainKud;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.IOException;

public class Main extends Application {

    private Parent mainUI;

    private static Stage stage;
    private static Scene scene;

    private MainKud mainKud;
    private JardueraKargatuKud jardueraKargatuKud;

    public int lehioAktibo  ;

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
        jardueraKargatuKud = new JardueraKargatuKud(this);

        Callback<Class<?>, Object> controllerFactory = type -> {
            if (type == MainKud.class) {
                return mainKud;
            } else if (type == JardueraKargatuKud.class) {
                return jardueraKargatuKud;
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
}
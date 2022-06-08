package com.yama.controllers.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainKud {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
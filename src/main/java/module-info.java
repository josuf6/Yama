module com.yama {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.yama to javafx.fxml;
    exports com.yama;
    exports com.yama.controllers.ui;
    opens com.yama.controllers.ui to javafx.fxml;
}
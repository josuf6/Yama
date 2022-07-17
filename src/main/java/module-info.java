module com.yama {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.common;
    requires org.json;
    requires FitCSVTool;
    requires com.sothawo.mapjfx;
    requires org.slf4j;
    requires com.jfoenix;
    requires java.sql;
    requires org.apache.commons.codec;


    opens com.yama to javafx.fxml;
    exports com.yama;
    exports com.yama.models;
    exports com.yama.controllers.ui;
    opens com.yama.controllers.ui to javafx.fxml;
    opens com.yama.models to javafx.fxml;
}
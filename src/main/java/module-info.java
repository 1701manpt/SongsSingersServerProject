module sgu.ltudm.songssingersserverproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires org.jsoup;
    requires javafx.media;
    requires org.json;
    requires annotations;

    opens sgu.ltudm.songssingersserverproject to javafx.fxml;
    exports sgu.ltudm.songssingersserverproject to javafx.graphics;
    exports sgu.ltudm.songssingersserverproject.models;
    exports sgu.ltudm.songssingersserverproject.controllers;
    opens sgu.ltudm.songssingersserverproject.controllers to javafx.fxml;
    exports sgu.ltudm.songssingersserverproject.encryptions;
}
module com.rooms.gui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.rooms.gui to javafx.fxml;
    exports com.rooms.gui;
}
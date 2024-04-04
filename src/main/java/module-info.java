module com.rooms.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.rooms.gui to javafx.fxml;
    exports com.rooms.gui;
}
module com.grocery {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.grocery to javafx.fxml;
    exports com.grocery;
}

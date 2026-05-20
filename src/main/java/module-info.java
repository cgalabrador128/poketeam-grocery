module com.grocery {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;

    opens com.grocery to javafx.fxml;
    exports com.grocery;
    exports com.grocery.controller;
    opens com.grocery.controller to javafx.fxml;
    exports com.grocery.util;
    opens com.grocery.util to javafx.fxml;
}

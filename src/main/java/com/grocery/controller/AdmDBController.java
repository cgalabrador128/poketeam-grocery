package com.grocery.controller;

import com.grocery.util.Employee;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.grocery.util.DataBConnection;
import com.grocery.util.Product;
import com.grocery.util.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

public class AdmDBController {
    @FXML
    private Label adm_name;
    @FXML
    private ListView alert_list;
    @FXML
    private TableColumn colName;
    @FXML
    private TableColumn colBatch;
    @FXML
    private TableColumn colPrice;
    @FXML
    private TableColumn colExpiry;
    @FXML
    private TableColumn colId;
    @FXML
    private TableColumn colStock;
    @FXML
    private TableView productTable;
    @FXML
    private TableView<Employee> staffTable;
    @FXML
    private TableColumn<Employee, String> colStaffRole;
    @FXML
    private TableColumn<Employee, Integer> colStaffId;
    @FXML
    private TableColumn<Employee, Integer> colStaffName;
    @FXML
    private Button cr8_stf_btn_id;
    @FXML
    private TextField search_field;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab prod_tab;

    //data
    DataBConnection data;
    User user;
    Connection dm;

    //barcodebuffer
    private StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final int SCANNER_THRESHOLD_MS = 50;

    @FXML
    private void initialize() throws SQLException {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
        try {
            data = DataBConnection.getInstance();
            user = User.getInstance();
            dm = data.getConnection();

            if (user.getUsername() != null) {
                adm_name.setText(user.getUsername());
            }

            // Inventory Value Factory
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("itemName")); // Matches product.getItemName()
            colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNum"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("noOfStock"));

            // Staff Value Factory
            colStaffId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colStaffName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colStaffRole.setCellValueFactory(new PropertyValueFactory<>("role"));
            //loads methods
            loadAlerts();
            loadInventory();
            loadStaff();
        }catch (SQLException e){
            e.printStackTrace();
        }

        //Events
        mainTabPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            //switches tabs
            if (event.getCode() == KeyCode.TAB) {
                if (event.isShiftDown()) {
                    mainTabPane.getSelectionModel().selectPrevious();
                } else {
                    mainTabPane.getSelectionModel().selectNext();
                }
                event.consume();
            }

            //scans
            mainTabPane.getSelectionModel().select(prod_tab);

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKeyTime > SCANNER_THRESHOLD_MS) {
                barcodeBuffer.setLength(0);
            }
            lastKeyTime = currentTime;

            if (event.getCode() == KeyCode.ENTER) {
                if (barcodeBuffer.length() > 4) {

                    String finalBarcode = barcodeBuffer.toString();

                    Platform.runLater(() -> {
                        search_field.setText(finalBarcode);
                        search_field.requestFocus();
                        search_field.positionCaret(finalBarcode.length());
                    });

                    barcodeBuffer.setLength(0);
                    event.consume();
                } else if (event.getText() != null && !event.getText().isEmpty()) {
                    barcodeBuffer.append(event.getText());
                }
            } // end of barcodescans
        });

        prod_tab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                Platform.runLater(() -> {
                    search_field.requestFocus();
                });
            }
        });
    }
    @FXML
    private void gen_inv_rep_btn(ActionEvent actionEvent){

    }

    @FXML
    public void logout_btn(ActionEvent actionEvent) throws IOException {
        Boolean confirm = confirmAlert("Logout","Do you want to logout?");

        if (confirm) {
            data.closeConnection();
            user.clearSession();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/grocery/login-page.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Node node = (Node) actionEvent.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }

    @FXML
    public void search(ActionEvent actionEvent) throws SQLException {
        search(search_field.getText());
    }

    @FXML
    public void rem_staff_btn(ActionEvent actionEvent) {
        Employee selectedEmp = staffTable.getSelectionModel().getSelectedItem();

        if (selectedEmp == null){
            showSimpleAlert("Selection Error", "Please select an employee from the table first.");
            return;
        }

        Boolean confirm = confirmAlert("Delete Staff", "Delete " + selectedEmp.getName() + " from Database?");
        if (confirm){
            String updateQuery = "DELETE users WHERE user_id = ?";
            try (PreparedStatement pstmt = dm.prepareStatement(updateQuery)) {
                pstmt.setInt(1, selectedEmp.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSimpleAlert("Success!", "Staff Deleted");
                    loadStaff();
                } else {
                    showSimpleAlert("Error", "Error has occurred during deletion process");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void create_staff_btn(ActionEvent actionEvent) throws IOException {
        Stage stage;
        Parent root;
        stage = (Stage) cr8_stf_btn_id.getScene().getWindow();
        root = FXMLLoader.load(getClass().getResource("/com/grocery/create-staff.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void re_prod_btn(ActionEvent actionEvent) { //remove product

    }

    @FXML
    public void adm_perm_btn(ActionEvent actionEvent) {
        Employee selectedEmp = staffTable.getSelectionModel().getSelectedItem();

        if (selectedEmp == null) {
            showSimpleAlert("Selection Error", "Please select an employee from the table first.");
            return;
        }

        if ("admin".equalsIgnoreCase(selectedEmp.getRole())) {
            showSimpleAlert("Information", selectedEmp.getName() + " is already an admin!");
            return;
        }

        Boolean confirm = confirmAlert("Grant Permissions", "Promote " + selectedEmp.getName() + " to Admin?");
        if (confirm) {
            String updateQuery = "UPDATE users SET user_role = 'admin' WHERE user_id = ?";
            try (PreparedStatement pstmt = dm.prepareStatement(updateQuery)) {
                pstmt.setInt(1, selectedEmp.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    showSimpleAlert("Success!", selectedEmp.getName() + " is now an Admin.");
                    loadStaff();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void add_prod_btn(ActionEvent actionEvent) {
    }

    @FXML
    public void reg_pro_btn(ActionEvent actionEvent) {
    }

    private void loadAlerts() throws SQLException {
        String query = "SELECT * FROM alert";
        Statement stmt = dm.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        ObservableList<String> alerts = FXCollections.observableArrayList();
        while (rs.next()) {
            String alertString = rs.getTimestamp("alert_time").toString() +
                    " : " + rs.getString("alert_type") +
                    " : " + Integer.toString(rs.getInt("product_serial_number")) +
                    " : " + Integer.toString(rs.getInt("alert_id"));

            alerts.add(alertString);
        }
        alert_list.setItems(alerts);
    }

    private void loadInventory() throws SQLException {
        ObservableList<Product> productList = FXCollections.observableArrayList();
        String query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory";

        try (PreparedStatement pstmt = dm.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("product_serial_number");
                String name = rs.getString("product_name");
                String batch = rs.getString("product_batch_no");
                double price = rs.getDouble("product_price");
                LocalDate expiry = rs.getDate("product_expiry_date").toLocalDate();
                int stock = rs.getInt("product_stock_count");

                productList.add(new Product(id, name, batch, price, expiry, stock));
            }
        }
        productTable.setItems(productList);
    }

    private void loadStaff() throws SQLException {
        ObservableList<Employee> staffList = FXCollections.observableArrayList();
        String query = "SELECT user_id, user_name, user_role FROM users";

        try (PreparedStatement pstmt = dm.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String username = rs.getString("user_name");
                String role = rs.getString("user_role");

                Employee emp = new Employee(id, username, role);
                staffList.add(emp);
            }
        }
        staffTable.setItems(staffList);
    }

    private void search(String find) throws SQLException {
        Boolean isInteger = false;
        int findInt = 0;
        if (find.matches("\\d+")){
            findInt = Integer.parseInt(find);
            isInteger = true;
        }

        ObservableList<Product> productList = FXCollections.observableArrayList();
        String query;
        PreparedStatement pstmt;
        if (isInteger) {
            query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory" +
                    "WHERE product_serial_number = ? OR product_batch_no = ?";
            pstmt = dm.prepareStatement(query);
            pstmt.setInt(1, findInt);
            pstmt.setInt(2, findInt);
        } else {
            query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory" +
                    "WHERE product_name = ?";
            pstmt = dm.prepareStatement(query);
            pstmt.setString(1, find);
        }
        try (ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("product_serial_number");
                String name = rs.getString("product_name");
                String batch = rs.getString("product_batch_no");
                double price = rs.getDouble("product_price");
                LocalDate expiry = rs.getDate("product_expiry_date").toLocalDate();
                int stock = rs.getInt("product_stock_count");

                productList.add(new Product(id, name, batch, price, expiry, stock));
            }
        }
        productTable.setItems(productList);
    }

    public Boolean confirmAlert(String title, String content ){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
            return true;
        } else{
            return false;
        }
    }

    public void showSimpleAlert(String title, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
    }

}

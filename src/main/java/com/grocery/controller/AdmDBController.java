package com.grocery.controller;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    private GridPane staff_list;
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

    //data
    DataBConnection data;
    User user;
    Connection dm;

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

            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("itemName")); // Matches product.getItemName()
            colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNum"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("noOfStock"));

            //loads methods
            loadAlerts();
            loadInventory();
        }catch (SQLException e){
            e.printStackTrace();
        }
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
    public void search(ActionEvent actionEvent) {
    }

    @FXML
    public void rem_staff_btn(ActionEvent actionEvent) {
    }

    @FXML
    public void create_staff_btn(ActionEvent actionEvent) {
    }

    @FXML
    public void re_prod_btn(ActionEvent actionEvent) {
    }

    @FXML
    public void adm_perm_btn(ActionEvent actionEvent) {
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


}

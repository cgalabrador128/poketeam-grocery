package com.grocery.controller;

import com.grocery.data.LoadData;
import com.grocery.data.Product;
import com.grocery.data.User;
import com.grocery.util.AlertHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.sql.Connection;
import java.sql.SQLException;

public class StaffDBController
{
    @javafx.fxml.FXML
    private Label staff_name;
    @FXML
    private Label staff_id;
    @FXML
    private Button delete_btn_id;
    @FXML
    private TableColumn colName;
    @FXML
    private TableView productTable;
    @FXML
    private TableColumn colStock;
    @FXML
    private CheckBox select_all;
    @FXML
    private Tab prod_tab;
    @FXML
    private TableColumn colBatch;
    @FXML
    private TableColumn colPrice;
    @FXML
    private TableColumn colExpiry;
    @FXML
    private TableColumn colId;
    @FXML
    private TextField search_field;

    //barcodebuffer
    private StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final int SCANNER_THRESHOLD_MS = 50;

    //Initialize other Variables
    User user;
    AlertHandler alert;
    Product product;
    LoadData dat;
    Connection dm;


    @javafx.fxml.FXML
    public void initialize() throws SQLException {
        //initialize variables
        user = User.getInstance();
        alert = new AlertHandler();
        dat = new LoadData();
        if (user.getUsername()!=null){
            staff_name.setText(user.getUsername());
        }
        staff_id.setText(Integer.toString(user.getUserId()));

        new Thread(() -> {
            try {
                while (true) {
                    dm = user.getConnection();
                    Thread.sleep(2000);
                }
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    alert.showSimpleAlert("Database Error", "Check your connection and try again");
                });
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        //valueFactory and Editable
        productTable.setEditable(true);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName")); // Matches product.getItemName()
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNum"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("noOfStock"));

        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colBatch.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colStock.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));



        productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //CSS STYLES
        delete_btn_id.visibleProperty().bind(Bindings.isNotEmpty(productTable.getSelectionModel().getSelectedItems()));
    }

    @javafx.fxml.FXML
    public void logout_btn(ActionEvent actionEvent) {

    }

    @FXML
    public void search(ActionEvent actionEvent) {
    }

    @FXML
    public void re_prod_btn(ActionEvent actionEvent) {
    }

    @FXML
    public void reg_pro_btn(ActionEvent actionEvent) {
    }

    @Deprecated
    public void add_prod_btn(ActionEvent actionEvent) {
    }

    @FXML
    public void select_all_products(ActionEvent actionEvent) {
    }
}
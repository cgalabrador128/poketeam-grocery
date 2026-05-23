package com.grocery.controller;

import com.grocery.data.*;
import com.grocery.util.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

import static com.grocery.App.loadFXML;
import static com.grocery.App.popFXML;
import static com.grocery.data.DataBConnection.closeConnection;

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
    private TextField search_field;
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab prod_tab;
    @FXML
    private CheckBox select_all;
    @FXML
    private Button adm_perm_btn;
    @FXML
    private Button del_prd_btn;
    @FXML
    private Button del_stf_btn;

    //data
    AlertHandler alert;
    User user;
    Connection dm;
    LoadData dat;

    //barcodebuffer
    private StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final int SCANNER_THRESHOLD_MS = 50;



    @FXML
    private void initialize() throws SQLException {
        // Hint: initialize() will be called when the associated FXML has been completely loaded.
        try {
            alert = new AlertHandler();
            dat = new LoadData();
            user = User.getInstance();

            if (user.getUsername() != null) {
                adm_name.setText(user.getUsername());
            }

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

            // Inventory Value Factory & Table
            productTable.setEditable(true);
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("itemName")); // Matches product.getItemName()
            colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNum"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("noOfStock"));

            //text-field
            colName.setCellFactory(TextFieldTableCell.forTableColumn());
            colBatch.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
            colStock.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));


            // Staff Value Factory
            colStaffId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colStaffName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colStaffRole.setCellValueFactory(new PropertyValueFactory<>("role"));

            //Selection Mode
            productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            //CSS Styles
            del_prd_btn.visibleProperty().bind(Bindings.isNotEmpty(productTable.getSelectionModel().getSelectedItems()));
            del_stf_btn.visibleProperty().bind(Bindings.isNotEmpty(staffTable.getSelectionModel().getSelectedItems()));

            //loads methods
            alert_list.setItems(dat.loadAlerts(dm, user));
            productTable.setItems(dat.loadInventory(dm));
            staffTable.setItems(dat.loadStaff(dm, user));


        }catch (SQLException e){
            alert.showSimpleAlert("Error", "Something occurred wrong during startup");
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
        //idk yet
    }

    @FXML
    public void logout_btn(ActionEvent actionEvent) throws IOException, SQLException {
        Boolean confirm = alert.confirmAlert("Logout","Do you want to logout?");
        if (confirm) {
            closeConnection();
            user.clearSession();
            loadFXML("login-page");
        }
    }

    @FXML
    public void search(ActionEvent actionEvent) throws SQLException {
        String find = search_field.getText();
        if (find.isEmpty()){
            productTable.setItems(dat.loadInventory(dm));
        } else {
            int findin = 0;
            long findi = 0;
            if (find.matches("^\\d{1,9}$")) {
                findin = Integer.parseInt(find);
            } else if (find.matches("^\\d{13}$")) {
                findi = Long.parseLong(find);
            }

            ObservableList<Product> productList = FXCollections.observableArrayList();
            String query;
            PreparedStatement pstmt;
            if (findin != 0) {
                query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory" +
                        " WHERE product_batch_no = ? OR product_serial_number = ?";
                pstmt = dm.prepareStatement(query);
                pstmt.setInt(1, findin);
                pstmt.setLong(2, findin);
            } else if (findi != 0) {
                query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory" +
                        " WHERE product_serial_number = ?";
                pstmt = dm.prepareStatement(query);
                pstmt.setLong(1, findi);
            } else {
                query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory" +
                        " WHERE product_name = ?";
                pstmt = dm.prepareStatement(query);
                pstmt.setString(1, find);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    long id = rs.getLong("product_serial_number");
                    String name = rs.getString("product_name");
                    int batch = rs.getInt("product_batch_no");
                    double price = rs.getDouble("product_price");
                    LocalDate expiry = rs.getDate("product_expiry_date").toLocalDate();
                    int stock = rs.getInt("product_stock_count");

                    productList.add(new Product(id, name, batch, price, expiry, stock));
                }
                if (!found){
                    Boolean register = alert.confirmAlert("Item not registered", "Register this item?");
                    if (register) {
                        if (findi != 0) {
                            user.setSearch(String.valueOf(findi));
                        } else if (findin != 0){
                            user.setSearch(String.valueOf(findin));
                        } else {
                            user.setSearch(find);
                        }
                        popFXML("reg-prod", "Register Product");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            productTable.setItems(productList);
        }
    }

    @FXML
    public void rem_staff_btn(ActionEvent actionEvent) {
        Employee selectedEmp = staffTable.getSelectionModel().getSelectedItem();

        if (selectedEmp == null){
            alert.showSimpleAlert("Selection Error", "Please select an employee from the table first.");
            return;
        }

        Boolean confirm = alert.confirmAlert("Delete Staff", "Delete " + selectedEmp.getName() + " from Database?");
        if (confirm){
            String updateQuery = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement pstmt = dm.prepareStatement(updateQuery)) {
                pstmt.setInt(1, selectedEmp.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    alert.showMessageAlert("Success!", "Staff Deleted");
                    staffTable.setItems(dat.deleteStaff(selectedEmp.getId()));
                } else {
                    alert.showSimpleAlert("Error", "Error has occurred during deletion process");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void create_staff_btn(ActionEvent actionEvent) throws IOException {popFXML("create-staff", "Register a Staff Member");}

    @FXML
    public void re_prod_btn(ActionEvent actionEvent) { //remove product
        ObservableList<Product> selectedProducts = productTable.getSelectionModel().getSelectedItems();
        boolean confirm = alert.confirmAlert("Confirm Deletion",
                "Are you sure you want to delete " + selectedProducts.size() + " product(s)? This cannot be undone.");

        if (confirm) {
            String query = "DELETE FROM inventory WHERE product_serial_number = ?";

            try (PreparedStatement pstmt = dm.prepareStatement(query)) {
                for (Product product : selectedProducts) {
                    pstmt.setLong(1, product.getId());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();

                productTable.getItems().removeAll(new ArrayList<>(selectedProducts));
                productTable.getSelectionModel().clearSelection();

                alert.showMessageAlert("Success", "Products successfully deleted.");

            } catch (SQLException e) {
                alert.showSimpleAlert("Error", "A database error occurred during deletion.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void adm_perm_btn(ActionEvent actionEvent) {
        Employee selectedEmp = staffTable.getSelectionModel().getSelectedItem();

        if (selectedEmp == null) {
            alert.showSimpleAlert("Selection Error", "Please select an employee from the table first.");
            return;
        }

        if (selectedEmp.getPermit() == true) {
            alert.showSimpleAlert("Information", selectedEmp.getName() + " is already an admin!");
            return;
        }

        Boolean confirm = alert.confirmAlert("Grant Permissions", "Add Admin permissions to " + selectedEmp.getName() + "?");
        if (confirm) {
            String updateQuery = "UPDATE users SET has_admin = true WHERE user_id = ?";
            try (PreparedStatement pstmt = dm.prepareStatement(updateQuery)) {
                pstmt.setInt(1, selectedEmp.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    alert.showMessageAlert("Success!", selectedEmp.getName() + " is now an Admin.");
                    dat.loadStaff(dm, user);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void select_all_products(ActionEvent actionEvent) {productTable.getSelectionModel().selectAll();
    }

    @FXML
    public void reg_pro_btn(ActionEvent actionEvent) throws IOException {popFXML("reg-prod", "Register Product");}

    @FXML
    public void edit_prod(Event event) {
        TableRow col = (TableRow) event.getSource();

    }

    @FXML
    public void save_chngs(ActionEvent actionEvent) {
    }
}

package com.grocery.controller;

import com.grocery.data.LoadData;
import com.grocery.data.Product;
import com.grocery.data.User;
import com.grocery.util.AlertHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.grocery.App.loadFXML;
import static com.grocery.data.DataBConnection.closeConnection;

public class StaffDBController
{
    public CheckBox select_all;
    @FXML
    private Label staff_name;
    @FXML
    private Label staff_id;
    @FXML
    private Button delete_btn_id;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, Integer> colBatch;
    @FXML
    private TableColumn<Product, Double> colPrice;
    @FXML
    private TableColumn<Product, LocalDate> colExpiry;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, Integer> colStock;
    @FXML
    private Tab prod_tab;
    @FXML
    private TextField search_field;
    @FXML
    private TabPane mainTabPane;

    //barcodebuffer
    private final StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final int SCANNER_THRESHOLD_MS = 50;

    //Initialize other Variables
    User user;
    AlertHandler alert;
    LoadData dat;
    Connection dm;
    private boolean session = true;


    @FXML
    public void initialize() throws SQLException {
        //initialize variables
        user = User.getInstance();
        alert = new AlertHandler();
        dat = new LoadData();
        if (user.getUserRole() == null) {
            return;
        }
        if (user.getUsername()!=null){
            staff_name.setText(user.getUsername());
        }
        staff_id.setText(Integer.toString(user.getUserId()));

        new Thread(() -> {
            try {
                while (session) {
                    dm = user.getConnection();
                    Thread.sleep(2000);
                }
            } catch (SQLException e) {
                Platform.runLater(() -> alert.showSimpleAlert("Database Error", "Check your connection and try again"));
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName")); // Matches product.getItemName()
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNum"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("noOfStock"));

        if (user.getPermit()) { //admin - permissions
            productTable.setEditable(true);
            //text-field
            colName.setCellFactory(TextFieldTableCell.forTableColumn());
            colExpiry.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter(DateTimeFormatter.ofPattern("yyyy-MM-dd"), null)));
            colBatch.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            colPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
            colStock.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            productTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            //CSS STYLES
            delete_btn_id.visibleProperty().bind(Bindings.isNotEmpty(productTable.getSelectionModel().getSelectedItems()));
            productTable.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(Product item, boolean empty) {
                    super.updateItem(item, empty);

                    // Always clear existing custom styles first
                    getStyleClass().remove("edited-stock-row");

                    if (item != null && !empty) {
                        if (item.isModified()) {
                            getStyleClass().add("edited-stock-row");
                        }
                    }
                }
            });
        }



        productTable.setItems(dat.loadInventory(user.getConnection()));

        prod_tab.selectedProperty().addListener((observable, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                Platform.runLater(() -> search_field.requestFocus());
            }
        });

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


    }

    @FXML
    public void logout_btn(ActionEvent actionEvent) throws SQLException, IOException {
        Boolean confirm = alert.confirmAlert("Logout","Do you want to logout?");
        if (confirm) {
            user.clearSession();
            session = false;
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
                    if (findi != 0) {
                        String query2 = "INSERT INTO alert(product_serial_number, alert_type, alert_time) VALUES (?, ?, ?)";
                        try (PreparedStatement pstmt2 = dm.prepareStatement(query2)) {
                            pstmt2.setLong(1, findi);
                            pstmt2.setString(2, "unregistered");
                            pstmt2.setTimestamp(3, Timestamp.from(Instant.now()));

                            try{
                            int rowsInserted = pstmt2.executeUpdate();
                            if (rowsInserted > 0) {
                                alert.showSimpleAlert("Item not registered", "Alerting to Admin");
                            }
                            } catch (SQLException e) {
                                alert.showSimpleAlert("Error", "Cannot Execute Search, Please try again");
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
            productTable.setItems(productList);
        }
    }

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
    public void select_all_products(ActionEvent actionEvent) { int total  = productTable.getItems().size();
        if  (productTable.getSelectionModel().getSelectedItems().size() != total) {
            productTable.getSelectionModel().selectAll();
        } else {
            productTable.getSelectionModel().clearSelection();
        }}

    @FXML
    public void refresh_prod(ActionEvent actionEvent) throws SQLException {productTable.setItems(dat.loadInventory(dm));}

    @FXML
    public void save_chngs(ActionEvent actionEvent) {
        String updateSql = "UPDATE inventory SET product_name = ?, product_batch_no = ?, " +
                "product_price = ?, product_stock_count = ? WHERE product_serial_number = ?";

        try (PreparedStatement pstmt = dm.prepareStatement(updateSql)) {
            int changesCount = 0;

            for (Product p : productTable.getItems()) {
                if (p.isModified()) {
                    pstmt.setString(1, p.getItemName());
                    pstmt.setInt(2, p.getBatchNum());
                    pstmt.setDouble(3, p.getPrice());
                    pstmt.setInt(4, p.getNoOfStock());
                    pstmt.setLong(5, p.getId());

                    pstmt.addBatch();
                    p.setModified(false); //reset
                    changesCount++;
                }
            }

            if (changesCount > 0) {
                pstmt.executeBatch(); // Send all updates to MySQL at once
                alert.showMessageAlert("Success", changesCount + " changes saved!");
            } else {
                alert.showSimpleAlert("No Changes", "No modifications detected.");
            }

        } catch (SQLException e) {
            alert.showSimpleAlert("Error", "Failed to save changes.");
            e.printStackTrace();
        }

    }

    @FXML
    public void edit_prodPrice(TableColumn.CellEditEvent<Product, Double> event) {
        Product p = event.getRowValue();
        p.setPrice(event.getNewValue());
        p.setModified(true);
        productTable.refresh();
    }

    @FXML
    public void edit_prodNoStock(TableColumn.CellEditEvent<Product, Integer> event ) {
        Product p = event.getRowValue();
        p.setNoOfStock(event.getNewValue());
        p.setModified(true);
        productTable.refresh();
    }

    @FXML
    public void edit_prodExpDate(TableColumn.CellEditEvent<Product, LocalDate> event) {
        Product p = event.getRowValue();
        p.setExpiryDate(event.getNewValue());
        p.setModified(true);
        productTable.refresh();
    }

    @FXML
    public void edit_prodBtchNum(TableColumn.CellEditEvent<Product, Integer> event) {
        Product p = event.getRowValue();
        p.setBatchNum(event.getNewValue());
        p.setModified(true);
        productTable.refresh();
    }

    @FXML
    public void edit_prodName(TableColumn.CellEditEvent<Product, String> event) {
        Product p = event.getRowValue();
        p.setItemName(event.getNewValue());
        p.setModified(true);
        productTable.refresh();
    }
}
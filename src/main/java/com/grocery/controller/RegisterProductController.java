package com.grocery.controller;

import com.grocery.data.User;
import com.grocery.util.AlertHandler;
import com.grocery.data.DataBConnection;
import com.grocery.util.GeneralFormatter;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.sql.*;

public class RegisterProductController
{
    @javafx.fxml.FXML
    private Spinner<Integer> prod_amt;
    @javafx.fxml.FXML
    private DatePicker expiry_date;
    @javafx.fxml.FXML
    private Spinner<Double> prod_price;
    @javafx.fxml.FXML
    private TextField prod_id;
    @javafx.fxml.FXML
    private TextField prod_batchno;
    @javafx.fxml.FXML
    private TextField prod_name;
    @javafx.fxml.FXML
    private Label title_label;
    @javafx.fxml.FXML
    private AnchorPane mainPane;

    //data
    private User user;
    private Connection dm;
    AlertHandler alert;
    GeneralFormatter formatter;

    private StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final int SCANNER_THRESHOLD_MS = 50;


    @javafx.fxml.FXML
    public void initialize() throws SQLException {
        alert = new AlertHandler();
        formatter = new GeneralFormatter();
        prod_amt.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1, 1));
        prod_price.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 999999.99, 1.0, 1.0));

        //initialize other variables
        prod_batchno.setTextFormatter(formatter.createNumberFormatter(9));
        prod_id.setTextFormatter(formatter.createNumberFormatter(13)); // EP

        user = User.getInstance();
        dm = user.getConnection();

        //find if search is empty
        if (!user.getSearch().isEmpty()){
            String item = user.getSearch();
            if (item.matches("^\\d+")){
                prod_id.setText(item);
            } else {
                prod_name.setText(item);
            }
        }

        //mainTab Event
        mainPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastKeyTime > SCANNER_THRESHOLD_MS) {
                barcodeBuffer.setLength(0);
            }
            lastKeyTime = currentTime;

            if (event.getCode() == KeyCode.ENTER) {
                if (barcodeBuffer.length() > 4) {
                    String finalBarcode = barcodeBuffer.toString();
                    long code = Long.parseLong(finalBarcode);
                    //find the serial no here.
                    String query = "SELECT product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory WHERE product_serial_number = ?";
                    try(PreparedStatement pstmt = dm.prepareStatement(query)){
                        pstmt.setLong(1, code);
                        try(ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                alert.showSimpleAlert("Product Exists", "Use a different barcode for this item");
                            } else {
                                prod_id.clear();
                                prod_id.setText(finalBarcode);
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                    barcodeBuffer.setLength(0);
                    event.consume();
                }
            } else if (event.getText() != null && !event.getText().isEmpty()) {
                barcodeBuffer.append(event.getText());
            }
        });

    }

    @javafx.fxml.FXML
    public void submit_btn(ActionEvent actionEvent) {
        prod_amt.increment(0);
        prod_price.increment(0);
        Boolean confirm = alert.confirmAlert("Confirm Inventory update?", "Changes will not be reversed");
        if (confirm){
            long id = Long.parseLong(prod_id.getText());
            int batch = Integer.parseInt(prod_batchno.getText());
            String query = "INSERT INTO inventory(product_serial_number, product_name, product_batch_no, product_price, product_expiry_date, product_stock_count) VALUES (?, ?, ?, ?, ?,?)";
            try(PreparedStatement pstmt = dm.prepareStatement(query)) {
                pstmt.setLong(1, id);
                pstmt.setString(2, prod_name.getText());
                pstmt.setInt(3, batch);
                pstmt.setDouble(4, prod_price.getValue());
                if (expiry_date != null) {
                    pstmt.setDate(5, Date.valueOf(expiry_date.getValue()));
                } else {
                    pstmt.setNull(5, java.sql.Types.DATE);
                }
                pstmt.setInt(6, prod_amt.getValue());

                int rowsInserted = pstmt.executeUpdate();

                if (rowsInserted > 0) {
                    alert.showMessageAlert("Success", "Product successfully added to inventory!");
                    prod_id.clear();
                    prod_batchno.clear();
                    prod_name.clear();
                    prod_amt.decrement(99999);
                    prod_price.decrement((int) 9999999.99);
                    expiry_date.setValue(null);
                }

            } catch (SQLException e) {
                alert.showSimpleAlert("Error", "Check your inputs! Serial and Batch must be valid numbers.");
                e.printStackTrace();
            }
        }
    }
}
package com.grocery.controller;

import com.grocery.util.DataBConnection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DefineProductController
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
    private DataBConnection data;
    private Connection dm;

    private StringBuilder barcodeBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    private static final int SCANNER_THRESHOLD_MS = 50;

    @javafx.fxml.FXML
    public void initialize() throws SQLException {
        data = DataBConnection.getInstance();
        dm = data.getConnection();

        prod_id.textProperty().addListener((observable, oldValue, newValue)->{
            if (newValue == null || newValue.trim().isEmpty()) {return;}
            int serialNum = Integer.parseInt(newValue);
            String query = "SELECT product_name, product_batch_no, product_price, product_expiry_date, product_stock_count FROM inventory WHERE product_serial_number = ?";
            try(PreparedStatement pstmt = dm.prepareStatement(query)){
                pstmt.setInt(1, serialNum);
                try (ResultSet rs = pstmt.executeQuery()){
                    if (rs.next()){
                        prod_name.setText(rs.getString("product_name"));
                        prod_batchno.setText(rs.getString("product_batch_no"));
                        prod_price.getValueFactory().setValue(rs.getDouble("product_price"));
                        expiry_date.setValue(rs.getDate("product_expiry_date").toLocalDate());
                        prod_amt.getValueFactory().setValue( rs.getInt("product_stock_count"));
                    }
            } catch (SQLException e) {throw new RuntimeException(e);}
        } catch (SQLException e) {throw new RuntimeException(e);}
        }); // scanning text up-to-date

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
                    //find the batch no. and serial no here.
                    String query =
                    /*Platform.runLater(() -> {
                        search_field.setText(finalBarcode);
                        search_field.requestFocus();
                        search_field.positionCaret(finalBarcode.length());
                    });*/

                    barcodeBuffer.setLength(0);
                    event.consume();
                } else if (event.getText() != null && !event.getText().isEmpty()) {
                    barcodeBuffer.append(event.getText());
                }
            }
        });
    }

    @javafx.fxml.FXML
    public void submit_btn(ActionEvent actionEvent) {
    }
}
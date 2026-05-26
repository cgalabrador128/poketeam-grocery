package com.grocery.controller;

import com.grocery.data.Archive;
import com.grocery.data.LoadData;
import com.grocery.data.User;
import com.grocery.util.AlertHandler;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Date;
import java.util.stream.Collectors;

public class InventoryReportController
{
    @javafx.fxml.FXML
    private DatePicker cust;
    @javafx.fxml.FXML
    private TableView<Archive> mainTable;
    @javafx.fxml.FXML
    private TableColumn<Archive, String> colName;
    @javafx.fxml.FXML
    private TableColumn<Archive, Integer> colRem;
    @javafx.fxml.FXML
    private TableColumn<Archive, Double> colPrice;
    @javafx.fxml.FXML
    private TableColumn<Archive, LocalDate> colDate;
    @javafx.fxml.FXML
    private TableColumn<Archive, Integer> colHr;
    @javafx.fxml.FXML
    private TableColumn<Archive, Integer> colStock;
    @javafx.fxml.FXML
    private TableColumn<Archive, Integer> colAdd;



    private LocalDate rangeStart = null;
    private LocalDate rangeEnd = null;
    private final ObservableSet<LocalDate> selectedDates = FXCollections.observableSet();

    Connection dm;
    User user;
    AlertHandler alert;


    @javafx.fxml.FXML
    public void initialize() throws SQLException {
        user = User.getInstance();
        alert = new AlertHandler();
        dm = user.getConnection();

        //select dates
        cust.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty){
                super.updateItem(item, empty);
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0c0;");
                }
                if (rangeStart != null && rangeEnd != null &&
                        !item.isBefore(rangeStart) && !item.isAfter(rangeEnd)) {
                    setStyle("-fx-background-color: lightblue;");
                } else if (item.equals(rangeStart) || item.equals(rangeEnd)) {
                    setStyle("-fx-background-color: #336699; -fx-text-fill: white;");
                } else {
                    setStyle("");
                }

                this.setOnMouseClicked(event -> {
                    handleDateSelection(item);
                    cust.show();
                });
            }
        }); // end of select dates

        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colRem.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRemoved()));
        colPrice.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPrice()));
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDate()));
        colHr.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getHour()));
        colStock.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCount()));
        colAdd.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAdded()));
    }

    @javafx.fxml.FXML
    public void gen_daily(ActionEvent actionEvent) {
        ObservableSet<LocalDate> genDate = FXCollections.observableSet(LocalDate.now());
        mainTable.setItems(gen(genDate));
    }

    @javafx.fxml.FXML
    public void gen_monthly(ActionEvent actionEvent) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        ObservableSet<LocalDate> genDate = FXCollections.observableSet();
        LocalDate temp = startOfMonth;
        while (!temp.isAfter(endOfMonth)) {
            genDate.add(temp);
            temp = temp.plusDays(1);
        }
        ObservableList<Archive> reportData = gen(genDate);
        reportData.sort(Comparator.comparing(Archive::getDate));
        mainTable.setItems(reportData);
    }

    @javafx.fxml.FXML
    public void gen_weekly(ActionEvent actionEvent) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        ObservableSet<LocalDate> genDate = FXCollections.observableSet();
        LocalDate temp = startOfWeek;
        while (!temp.isAfter(endOfWeek)) {
            genDate.add(temp);
            temp = temp.plusDays(1);
        }
        ObservableList<Archive> reportData = gen(genDate);
        reportData.sort(Comparator.comparing(Archive::getDate));
        mainTable.setItems(reportData);
    }

    @javafx.fxml.FXML
    public void gen_cust(ActionEvent actionEvent) {
        if (selectedDates.isEmpty()) {
            alert.showSimpleAlert("Selection Error", "Please select dates first.");
            return;
        }

        ObservableList<Archive> reportData = gen(selectedDates);
        reportData.sort(Comparator.comparing(Archive::getDate));
        mainTable.setItems(reportData);
    }

    private ObservableList<Archive> gen(ObservableSet<LocalDate> selectedDates){
        ObservableList<Archive> generatedReport = FXCollections.observableArrayList();
        try{
            String placeholders = selectedDates.stream()
                    .map(d -> "?")
                    .collect(Collectors.joining(","));

            String query = "SELECT * FROM archive WHERE archive_date IN (" + placeholders + ")";
            try(PreparedStatement pstmt = dm.prepareStatement(query)){
                int index = 1;
                for (LocalDate date : selectedDates) {
                    pstmt.setDate(index++, java.sql.Date.valueOf(date));
                }

                try(ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Archive a = new Archive(
                                rs.getInt("archive_hour"),
                                rs.getDate("archive_date").toLocalDate(),
                                rs.getInt("total_removed"),
                                rs.getInt("total_added"),
                                rs.getDouble("product_price"),
                                rs.getInt("product_stock_count"),
                                rs.getString("product_name"));
                        generatedReport.add(a);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() -> alert.showSimpleAlert("Database Error", "Failed to retrieve report data."));
        }
        System.out.println(generatedReport);
        return generatedReport;
    }
    private void handleDateSelection(LocalDate date) {
        if (rangeStart == null || (rangeStart != null && rangeEnd != null)) {
            rangeStart = date;
            rangeEnd = null;
            selectedDates.clear();
            selectedDates.add(date);
        } else {
            if (date.isBefore(rangeStart)) {
                rangeStart = date;
            } else {
                rangeEnd = date;
            }
            selectedDates.clear();
            LocalDate temp = rangeStart;
            while (!temp.isAfter(rangeEnd)) {
                selectedDates.add(temp);
                temp = temp.plusDays(1);
            }
        }
    }
}
package com.busreservation.controllers;

import com.busreservation.models.Database;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.List;

public class ReportsController {
    
    @FXML private TableView<Object[]> summaryTable;
    @FXML private TableColumn<Object[], String> colDate;
    @FXML private TableColumn<Object[], Integer> colTotalBookings;
    @FXML private TableColumn<Object[], String> colRevenue;
    
    @FXML private TableView<Object[]> routesTable;
    @FXML private TableColumn<Object[], String> colOrigin;
    @FXML private TableColumn<Object[], String> colDestination;
    @FXML private TableColumn<Object[], Integer> colBookings;
    @FXML private TableColumn<Object[], String> colRouteRevenue;
    
    private Database db;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0]));
        colTotalBookings.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue()[1]).asObject());
        colRevenue.setCellValueFactory(cellData -> {
            double revenue = (Double) cellData.getValue()[2];
            return new javafx.beans.property.SimpleStringProperty("₱" + String.format("%.2f", revenue));
        });
        
        colOrigin.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[0]));
        colDestination.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty((String) cellData.getValue()[1]));
        colBookings.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty((Integer) cellData.getValue()[2]).asObject());
        colRouteRevenue.setCellValueFactory(cellData -> {
            double revenue = (Double) cellData.getValue()[3];
            return new javafx.beans.property.SimpleStringProperty("₱" + String.format("%.2f", revenue));
        });
        
        loadReports();
    }
    
    private void loadReports() {
        List<Object[]> summary = db.getBookingSummaryReport();
        summaryTable.getItems().clear();
        if (summary != null && !summary.isEmpty()) {
            summaryTable.getItems().addAll(summary);
        }
        
        List<Object[]> routes = db.getPopularRoutesReport();
        routesTable.getItems().clear();
        if (routes != null && !routes.isEmpty()) {
            routesTable.getItems().addAll(routes);
        }
    }
}
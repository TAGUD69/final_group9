package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Booking;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {
    
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, String> colDate;
    @FXML private TableColumn<Booking, String> colTime;
    @FXML private TableColumn<Booking, String> colPassenger;
    @FXML private TableColumn<Booking, String> colRoute;
    @FXML private TableColumn<Booking, String> colSeats;
    @FXML private TableColumn<Booking, String> colFare;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private Label totalLabel;
    @FXML private Label revenueLabel;
    
    private Database db;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colDate.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getBookingDate();
            if (date != null && date.length() >= 10) {
                return new javafx.beans.property.SimpleStringProperty(date.substring(0, 10));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colTime.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getBookingDate();
            if (date != null && date.length() >= 16) {
                return new javafx.beans.property.SimpleStringProperty(date.substring(11, 16));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colRoute.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrigin() + " → " + cellData.getValue().getDestination()));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seatNumbers"));
        colFare.setCellValueFactory(cellData -> {
            double fare = cellData.getValue().getTotalFare();
            return new javafx.beans.property.SimpleStringProperty(String.format("₱%.2f", fare));
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        loadBookings();
    }
    
    private void loadBookings() {
        List<Booking> bookings = db.getAllBookings();
        bookingsTable.getItems().clear();
        bookingsTable.getItems().addAll(bookings);
        
        int totalBookings = bookings.size();
        double totalRevenue = bookings.stream()
            .filter(b -> "confirmed".equals(b.getStatus()))
            .mapToDouble(Booking::getTotalFare)
            .sum();
        
        totalLabel.setText(String.valueOf(totalBookings));
        revenueLabel.setText(String.format("₱%.2f", totalRevenue));
    }
    
    @FXML
    private void refreshReport() {
        loadBookings();
    }
}
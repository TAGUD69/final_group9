package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Booking;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyBookingsController {
    
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, String> colDate;
    @FXML private TableColumn<Booking, String> colBus;
    @FXML private TableColumn<Booking, String> colRoute;
    @FXML private TableColumn<Booking, String> colPassenger;
    @FXML private TableColumn<Booking, String> colSeats;
    @FXML private TableColumn<Booking, String> colFare;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, Void> colAction;
    
    private Database db;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("busName"));
        colRoute.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrigin() + " → " + cellData.getValue().getDestination()));
        colPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("seatNumbers"));
        colFare.setCellValueFactory(cellData -> {
            double fare = cellData.getValue().getTotalFare();
            return new javafx.beans.property.SimpleStringProperty(String.format("₱%.2f", fare));
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button cancelButton = new Button("Cancel");
            {
                cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 10 5 10;");
                cancelButton.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if ("confirmed".equals(booking.getStatus())) {
                        cancelBooking(booking);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    if ("cancelled".equals(booking.getStatus())) {
                        setGraphic(null);
                    } else {
                        cancelButton.setDisable(false);
                        cancelButton.setText("Cancel");
                        setGraphic(cancelButton);
                    }
                }
            }
        });
        
        loadBookings();
        startAutoRefresh();
    }
    
    private void startAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000);
                    javafx.application.Platform.runLater(() -> loadBookings());
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
    
    private boolean isBookingExpired(Booking booking) {
        try {
            String departureTime = booking.getDepartureTime();
            if (departureTime == null || departureTime.isEmpty()) return false;
            LocalDateTime departure = LocalDateTime.parse(departureTime, formatter);
            return LocalDateTime.now().isAfter(departure);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void loadBookings() {
        List<Booking> allBookings = db.getUserBookings(SessionManager.getCurrentUser().getUserId());
        List<Booking> activeBookings = allBookings.stream()
            .filter(b -> "confirmed".equals(b.getStatus()))
            .filter(b -> !isBookingExpired(b))
            .collect(Collectors.toList());
        
        bookingsTable.getItems().clear();
        bookingsTable.getItems().addAll(activeBookings);
    }
    
    private void cancelBooking(Booking booking) {
        if (db.cancelBooking(booking.getBookingId(), SessionManager.getCurrentUser().getUserId())) {
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Cancelled");
            successAlert.setHeaderText("Booking Cancelled");
            successAlert.setContentText("Booking #" + booking.getBookingId() + " has been cancelled.");
            
            ButtonType undoButton = new ButtonType("Undo");
            ButtonType closeButton = new ButtonType("Close");
            successAlert.getButtonTypes().setAll(undoButton, closeButton);
            
            var result = successAlert.showAndWait();
            if (result.isPresent() && result.get() == undoButton) {
                undoCancelBooking(booking);
            }
            loadBookings();
        } else {
            showAlert("Error", "Failed to cancel booking", Alert.AlertType.ERROR);
        }
    }
    
    private void undoCancelBooking(Booking booking) {
        if (db.restoreBooking(booking.getBookingId())) {
            showAlert("Success", "Booking restored!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Could not restore booking", Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
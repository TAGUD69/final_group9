package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Schedule;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class BookingController {
    
    @FXML private Label busInfoLabel;
    @FXML private Label routeLabel;
    @FXML private Label departureLabel;
    @FXML private Label fareLabel;
    @FXML private TextField passengerNameField;
    @FXML private TextField seatNumbersField;
    @FXML private TextField baggageField;
    @FXML private Label totalLabel;
    @FXML private HBox quickSeatButtonsContainer;
    @FXML private TextField customSeatsField;
    
    private Database db;
    private Schedule currentSchedule;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        seatNumbersField.textProperty().addListener((obs, old, newVal) -> calculateTotal());
        baggageField.textProperty().addListener((obs, old, newVal) -> calculateTotal());
        
        addQuickSeatButtons();
    }
    
    private void addQuickSeatButtons() {
        HBox seatButtons = new HBox(10);
        int[] seatOptions = {1, 2, 3, 4, 5, 6, 8, 10};
        for (int seats : seatOptions) {
            Button btn = new Button(seats + " Seat" + (seats > 1 ? "s" : ""));
            btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 12 5 12;");
            final int seatCount = seats;
            btn.setOnAction(e -> {
                seatNumbersField.setText(generateSeatNumbers(seatCount));
                calculateTotal();
            });
            seatButtons.getChildren().add(btn);
        }
        quickSeatButtonsContainer.getChildren().add(seatButtons);
    }
    
    @FXML
    private void applyCustomSeats() {
        try {
            int numSeats = Integer.parseInt(customSeatsField.getText().trim());
            if (numSeats > 0 && numSeats <= 50) {
                seatNumbersField.setText(generateSeatNumbers(numSeats));
                calculateTotal();
                customSeatsField.clear();
            } else {
                showAlert("Please enter a number between 1 and 50");
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number");
        }
    }
    
    private String generateSeatNumbers(int count) {
        StringBuilder seats = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            seats.append("A").append(i);
            if (i < count) seats.append(",");
        }
        return seats.toString();
    }
    
    public void setSchedule(Schedule schedule) {
        this.currentSchedule = schedule;
        busInfoLabel.setText(schedule.getBusNumber() + " - " + schedule.getBusName());
        routeLabel.setText(schedule.getOrigin() + " → " + schedule.getDestination());
        departureLabel.setText(schedule.getDepartureTime());
        fareLabel.setText("₱" + String.format("%.2f", schedule.getFare()));
        calculateTotal();
    }
    
    private void calculateTotal() {
        try {
            int numSeats = seatNumbersField.getText().isEmpty() ? 0 : seatNumbersField.getText().split(",").length;
            double baggage = baggageField.getText().isEmpty() ? 0 : Double.parseDouble(baggageField.getText());
            double total = (currentSchedule.getFare() * numSeats) + (baggage * 5);
            totalLabel.setText(String.format("Total: ₱%.2f", total));
        } catch (NumberFormatException e) {
            totalLabel.setText("Total: ₱0.00");
        }
    }
    
    @FXML
    private void handleConfirm() {
        String passengerName = passengerNameField.getText().trim();
        String seatNumbers = seatNumbersField.getText().trim();
        String baggageText = baggageField.getText().trim();
        
        if (passengerName.isEmpty() || seatNumbers.isEmpty()) {
            showAlert("Please fill all required fields");
            return;
        }
        
        try {
            double baggage = baggageText.isEmpty() ? 0 : Double.parseDouble(baggageText);
            int numSeats = seatNumbers.split(",").length;
            double total = (currentSchedule.getFare() * numSeats) + (baggage * 5);
            
            int bookingId = db.bookTicket(
                SessionManager.getCurrentUser().getUserId(), 
                currentSchedule.getScheduleId(), 
                seatNumbers, 
                passengerName, 
                0,
                "Not Specified", 
                baggage, 
                total
            );
            
            if (bookingId > 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Booking Successful!");
                alert.setContentText("Booking ID: " + bookingId + "\nTotal Fare: ₱" + String.format("%.2f", total));
                alert.showAndWait();
                closeDialog();
            } else {
                showAlert("Booking failed. Please try again.");
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter valid numbers");
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) passengerNameField.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
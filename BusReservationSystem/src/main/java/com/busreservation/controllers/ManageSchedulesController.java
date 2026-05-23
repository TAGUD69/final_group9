package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Bus;
import com.busreservation.models.Route;
import com.busreservation.models.Schedule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManageSchedulesController {
    
    @FXML private ComboBox<Bus> busCombo;
    @FXML private ComboBox<Route> routeCombo;
    @FXML private DatePicker departureDate;
    @FXML private ComboBox<Integer> departureHour;
    @FXML private ComboBox<Integer> departureMinute;
    @FXML private DatePicker arrivalDate;
    @FXML private ComboBox<Integer> arrivalHour;
    @FXML private ComboBox<Integer> arrivalMinute;
    @FXML private TextField seatsField;
    @FXML private TextField multiplierField;
    @FXML private TableView<Schedule> schedulesTable;
    @FXML private TableColumn<Schedule, Integer> colId;
    @FXML private TableColumn<Schedule, String> colBus;
    @FXML private TableColumn<Schedule, String> colRoute;
    @FXML private TableColumn<Schedule, String> colDeparture;
    @FXML private TableColumn<Schedule, String> colArrival;
    @FXML private TableColumn<Schedule, Integer> colSeats;
    
    private Database db;
    private Schedule selectedSchedule;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        setupTimeComboBoxes();
        loadBusesAndRoutes();
        setupTable();
        loadSchedules();
        setupSelectionListener();
        
        departureDate.setValue(LocalDate.now().plusDays(1));
        arrivalDate.setValue(LocalDate.now().plusDays(1));
        departureHour.setValue(8);
        departureMinute.setValue(0);
        arrivalHour.setValue(12);
        arrivalMinute.setValue(0);
    }
    
    private void setupTimeComboBoxes() {
        for (int i = 0; i < 24; i++) {
            departureHour.getItems().add(i);
            arrivalHour.getItems().add(i);
        }
        for (int i = 0; i < 60; i += 5) {
            departureMinute.getItems().add(i);
            arrivalMinute.getItems().add(i);
        }
    }
    
    private void loadBusesAndRoutes() {
        List<Bus> buses = db.getAllBuses();
        busCombo.getItems().addAll(buses);
        
        List<Route> routes = db.getAllRoutes();
        routeCombo.getItems().addAll(routes);
    }
    
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("busName"));
        colRoute.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrigin() + " → " + cellData.getValue().getDestination()));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
    }
    
    private void loadSchedules() {
        List<Schedule> schedules = db.getAllSchedules();
        schedulesTable.getItems().clear();
        schedulesTable.getItems().addAll(schedules);
    }
    
    private void setupSelectionListener() {
        schedulesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedSchedule = newVal;
            if (newVal != null) {
                for (Bus b : busCombo.getItems()) {
                    if (b.getBusName().equals(newVal.getBusName())) {
                        busCombo.setValue(b);
                        break;
                    }
                }
                String dep = newVal.getDepartureTime();
                String arr = newVal.getArrivalTime();
                if (dep != null && dep.length() > 16) {
                    String depDate = dep.substring(0, 10);
                    String depTime = dep.substring(11, 16);
                    departureDate.setValue(LocalDate.parse(depDate));
                    departureHour.setValue(Integer.parseInt(depTime.substring(0, 2)));
                    departureMinute.setValue(Integer.parseInt(depTime.substring(3, 5)));
                }
                if (arr != null && arr.length() > 16) {
                    String arrDate = arr.substring(0, 10);
                    String arrTime = arr.substring(11, 16);
                    arrivalDate.setValue(LocalDate.parse(arrDate));
                    arrivalHour.setValue(Integer.parseInt(arrTime.substring(0, 2)));
                    arrivalMinute.setValue(Integer.parseInt(arrTime.substring(3, 5)));
                }
            }
        });
    }
    
    private String formatDateTime(LocalDate date, int hour, int minute) {
        LocalDateTime dateTime = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    
    @FXML
    private void handleAdd() {
        if (busCombo.getValue() == null) {
            showAlert("Error", "Please select a bus", Alert.AlertType.ERROR);
            return;
        }
        if (routeCombo.getValue() == null) {
            showAlert("Error", "Please select a route", Alert.AlertType.ERROR);
            return;
        }
        if (departureDate.getValue() == null) {
            showAlert("Error", "Please select departure date", Alert.AlertType.ERROR);
            return;
        }
        if (arrivalDate.getValue() == null) {
            showAlert("Error", "Please select arrival date", Alert.AlertType.ERROR);
            return;
        }
        
        try {
            Bus bus = busCombo.getValue();
            Route route = routeCombo.getValue();
            
            String departure = formatDateTime(departureDate.getValue(), departureHour.getValue(), departureMinute.getValue());
            String arrival = formatDateTime(arrivalDate.getValue(), arrivalHour.getValue(), arrivalMinute.getValue());
            
            int seats = Integer.parseInt(seatsField.getText());
            double multiplier = Double.parseDouble(multiplierField.getText());
            
            boolean success = db.addSchedule(bus.getBusId(), route.getRouteId(), departure, arrival, seats, multiplier);
            if (success) {
                showAlert("Success", "Schedule added successfully!", Alert.AlertType.INFORMATION);
                clearFields();
                loadSchedules();
            } else {
                showAlert("Error", "Failed to add schedule", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for seats and multiplier", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedSchedule != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete Schedule");
            confirm.setContentText("Delete schedule for bus " + selectedSchedule.getBusName() + " on " + selectedSchedule.getDepartureTime() + "?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                schedulesTable.getItems().remove(selectedSchedule);
                showAlert("Success", "Schedule deleted", Alert.AlertType.INFORMATION);
                selectedSchedule = null;
                clearFields();
            }
        } else {
            showAlert("Warning", "Please select a schedule to delete", Alert.AlertType.WARNING);
        }
    }
    
    @FXML
    private void handleClear() {
        clearFields();
        selectedSchedule = null;
        schedulesTable.getSelectionModel().clearSelection();
    }
    
    private void clearFields() {
        busCombo.setValue(null);
        routeCombo.setValue(null);
        departureDate.setValue(LocalDate.now().plusDays(1));
        arrivalDate.setValue(LocalDate.now().plusDays(1));
        departureHour.setValue(8);
        departureMinute.setValue(0);
        arrivalHour.setValue(12);
        arrivalMinute.setValue(0);
        seatsField.clear();
        multiplierField.setText("1.0");
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
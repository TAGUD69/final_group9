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

    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
        setDefaults();
    }

    private void setDefaults() {
        departureDate.setValue(LocalDate.now().plusDays(1));
        arrivalDate.setValue(LocalDate.now().plusDays(1));
        departureHour.setValue(8);
        departureMinute.setValue(0);
        arrivalHour.setValue(12);
        arrivalMinute.setValue(0);
        multiplierField.setText("1.0");
    }

    private void setupTimeComboBoxes() {
        for (int i = 0; i < 24; i++) { departureHour.getItems().add(i); arrivalHour.getItems().add(i); }
        for (int i = 0; i < 60; i += 5) { departureMinute.getItems().add(i); arrivalMinute.getItems().add(i); }
    }

    private void loadBusesAndRoutes() {
        busCombo.getItems().setAll(db.getAllBuses());
        routeCombo.getItems().setAll(db.getAllRoutes());
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        colBus.setCellValueFactory(new PropertyValueFactory<>("busName"));
        colRoute.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(cd.getValue().getOrigin() + " → " + cd.getValue().getDestination()));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
    }

    private void loadSchedules() {
        schedulesTable.getItems().setAll(db.getAllSchedulesAdmin());
    }

    private void setupSelectionListener() {
        schedulesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectedSchedule = newVal;
            if (newVal == null) return;
            for (Bus b : busCombo.getItems()) {
                if (b.getBusName().equals(newVal.getBusName())) { busCombo.setValue(b); break; }
            }
            parseAndSetTime(newVal.getDepartureTime(), departureDate, departureHour, departureMinute);
            parseAndSetTime(newVal.getArrivalTime(), arrivalDate, arrivalHour, arrivalMinute);
            seatsField.setText(String.valueOf(newVal.getAvailableSeats()));
        });
    }

    private void parseAndSetTime(String dt, DatePicker dp, ComboBox<Integer> hour, ComboBox<Integer> min) {
        if (dt == null || dt.length() < 16) return;
        try {
            // Handle both "yyyy-MM-dd HH:mm:ss" and "yyyy-MM-ddTHH:mm:ss" formats
            String normalized = dt.replace("T", " ");
            dp.setValue(LocalDate.parse(normalized.substring(0, 10)));
            hour.setValue(Integer.parseInt(normalized.substring(11, 13)));
            int m = Integer.parseInt(normalized.substring(14, 16));
            // Round to nearest 5 for the combobox
            m = (m / 5) * 5;
            hour.setValue(Integer.parseInt(normalized.substring(11, 13)));
            min.setValue(m);
        } catch (Exception ignored) {}
    }

    private String formatDateTime(LocalDate date, int hour, int minute) {
        return LocalDateTime.of(date, java.time.LocalTime.of(hour, minute)).format(DB_FORMAT);
    }

    @FXML
    private void handleAdd() {
        if (busCombo.getValue() == null) { showAlert("Error", "Please select a bus", Alert.AlertType.ERROR); return; }
        if (routeCombo.getValue() == null) { showAlert("Error", "Please select a route", Alert.AlertType.ERROR); return; }
        if (departureDate.getValue() == null || arrivalDate.getValue() == null) {
            showAlert("Error", "Please select departure and arrival dates", Alert.AlertType.ERROR); return;
        }
        try {
            int seats = Integer.parseInt(seatsField.getText().trim());
            double multiplier = Double.parseDouble(multiplierField.getText().trim());
            if (seats <= 0 || multiplier <= 0) throw new NumberFormatException();

            String dep = formatDateTime(departureDate.getValue(), departureHour.getValue(), departureMinute.getValue());
            String arr = formatDateTime(arrivalDate.getValue(), arrivalHour.getValue(), arrivalMinute.getValue());

            if (dep.compareTo(arr) >= 0) {
                showAlert("Error", "Arrival time must be after departure time", Alert.AlertType.ERROR); return;
            }

            if (db.addSchedule(busCombo.getValue().getBusId(), routeCombo.getValue().getRouteId(), dep, arr, seats, multiplier)) {
                showAlert("Success", "Schedule added successfully!", Alert.AlertType.INFORMATION);
                clearFields();
                loadSchedules();
            } else {
                showAlert("Error", "Failed to add schedule", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Enter valid numbers for seats and multiplier", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedSchedule == null) {
            showAlert("Warning", "Please select a schedule to delete", Alert.AlertType.WARNING); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete schedule for " + selectedSchedule.getBusName() + " on " + selectedSchedule.getDepartureTime() + "?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // FIX: actually delete from the database, not just the table view
            if (db.deleteSchedule(selectedSchedule.getScheduleId())) {
                showAlert("Success", "Schedule deleted", Alert.AlertType.INFORMATION);
                selectedSchedule = null;
                clearFields();
                loadSchedules();
            } else {
                showAlert("Error", "Failed to delete schedule", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClear() { clearFields(); selectedSchedule = null; schedulesTable.getSelectionModel().clearSelection(); }

    private void clearFields() {
        busCombo.setValue(null); routeCombo.setValue(null);
        seatsField.clear(); multiplierField.setText("1.0");
        setDefaults();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}

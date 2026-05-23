package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Schedule;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SearchController {
    
    @FXML private ComboBox<String> fromCombo;
    @FXML private ComboBox<String> toCombo;
    @FXML private ComboBox<String> busTypeFilter;
    @FXML private DatePicker datePicker;
    @FXML private Label resultCountLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private ProgressIndicator loadingIndicator;
    
    @FXML private TableView<Schedule> resultsTable;
    @FXML private TableColumn<Schedule, String> colBusNumber;
    @FXML private TableColumn<Schedule, String> colBusName;
    @FXML private TableColumn<Schedule, String> colBusType;
    @FXML private TableColumn<Schedule, String> colOrigin;
    @FXML private TableColumn<Schedule, String> colDestination;
    @FXML private TableColumn<Schedule, String> colDeparture;
    @FXML private TableColumn<Schedule, String> colArrival;
    @FXML private TableColumn<Schedule, String> colDuration;
    @FXML private TableColumn<Schedule, Integer> colAvailableSeats;
    @FXML private TableColumn<Schedule, String> colFare;
    @FXML private TableColumn<Schedule, Void> colAction;
    
    private Database db;
    private List<Schedule> allSchedules;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        setupCities();
        setupTable();
        setupAlternatingRowColors();
        setupDefaultValues();
        
        fromCombo.valueProperty().addListener((obs, old, val) -> handleSearch());
        toCombo.valueProperty().addListener((obs, old, val) -> handleSearch());
        datePicker.valueProperty().addListener((obs, old, val) -> handleSearch());
        busTypeFilter.valueProperty().addListener((obs, old, val) -> filterTable());
        
        loadAllBuses();
    }
    
    private void setupCities() {
        String[] cities = {"Any", "Digos City", "Sta. Cruz", "Bansalan", "Magsaysay", "Kapatagan", "Matanao", "Hagonoy", "Malalag", "Padada", "Sulop", "Kiblawan"};
        fromCombo.getItems().addAll(cities);
        toCombo.getItems().addAll(cities);
        fromCombo.setValue("Any");
        toCombo.setValue("Any");
        busTypeFilter.getItems().addAll("All Types", "AC", "Non-AC", "Sleeper", "Seater");
        busTypeFilter.setValue("All Types");
    }
    
    private void setupDefaultValues() {
        datePicker.setValue(LocalDate.now());
    }
    
    private void setupAlternatingRowColors() {
        resultsTable.setRowFactory(tv -> new TableRow<Schedule>() {
            @Override
            protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (getIndex() % 2 == 0) {
                    setStyle("-fx-background-color: #f8f9fa;");
                } else {
                    setStyle("-fx-background-color: white;");
                }
            }
        });
    }
    
    private void setupTable() {
        colBusNumber.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        colBusName.setCellValueFactory(new PropertyValueFactory<>("busName"));
        colBusType.setCellValueFactory(new PropertyValueFactory<>("busType"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colAvailableSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        
        colFare.setCellValueFactory(cellData -> {
            double fare = cellData.getValue().getFare();
            return new javafx.beans.property.SimpleStringProperty(String.format("₱%.2f", fare));
        });
        
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button bookButton = new Button("Book");
            {
                bookButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                bookButton.setOnAction(event -> {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    if (schedule.getAvailableSeats() > 0) {
                        openBookingDialog(schedule);
                    } else {
                        showAlert("No seats available");
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Schedule schedule = getTableView().getItems().get(getIndex());
                    int seatsLeft = schedule.getAvailableSeats();
                    bookButton.setDisable(seatsLeft <= 0);
                    
                    if (seatsLeft <= 0) {
                        bookButton.setText("Sold Out");
                        bookButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    } else if (seatsLeft <= 5) {
                        bookButton.setText("⚠️ Last " + seatsLeft);
                        bookButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    } else {
                        bookButton.setText("Book");
                        bookButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    }
                    setGraphic(bookButton);
                }
            }
        });
    }
    
    private void loadAllBuses() {
        loadingIndicator.setVisible(true);
        
        new Thread(() -> {
            List<Schedule> schedules = db.getAllSchedules();
            javafx.application.Platform.runLater(() -> {
                allSchedules = schedules;
                filterTable();
                loadingIndicator.setVisible(false);
                updateLastUpdatedLabel();
            });
        }).start();
    }
    
    private void updateLastUpdatedLabel() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        lastUpdatedLabel.setText(now);
    }
    
    private void updateResultCount(int count) {
        String from = fromCombo.getValue();
        String to = toCombo.getValue();
        if (from != null && !from.equals("Any") && to != null && !to.equals("Any") && !from.equals(to)) {
            resultCountLabel.setText("🎫 " + count + " buses from " + from + " to " + to);
        } else {
            resultCountLabel.setText("🚌 " + count + " available buses");
        }
    }
    
    @FXML
    private void showTodayBuses() {
        datePicker.setValue(LocalDate.now());
        handleSearch();
    }
    
    @FXML
    private void handleSearch() {
        if (allSchedules == null) {
            loadAllBuses();
            return;
        }
        
        loadingIndicator.setVisible(true);
        
        new Thread(() -> {
            String from = fromCombo.getValue();
            String to = toCombo.getValue();
            LocalDate date = datePicker.getValue();
            
            List<Schedule> filtered = allSchedules.stream()
                .filter(s -> filterByRoute(s, from, to))
                .filter(s -> filterByDate(s, date))
                .collect(Collectors.toList());
            
            javafx.application.Platform.runLater(() -> {
                allSchedules = filtered;
                filterTable();
                loadingIndicator.setVisible(false);
            });
        }).start();
    }
    
    private void filterTable() {
        if (allSchedules == null) return;
        
        List<Schedule> filtered = allSchedules.stream()
            .filter(this::filterByBusType)
            .collect(Collectors.toList());
        
        displaySchedules(filtered);
        updateResultCount(filtered.size());
    }
    
    private boolean filterByRoute(Schedule s, String from, String to) {
        boolean fromMatch = (from == null || from.equals("Any") || s.getOrigin().equals(from));
        boolean toMatch = (to == null || to.equals("Any") || s.getDestination().equals(to));
        return fromMatch && toMatch;
    }
    
    private boolean filterByDate(Schedule s, LocalDate selectedDate) {
        if (selectedDate == null) return true;
        String departure = s.getDepartureTime();
        if (departure == null || departure.isEmpty()) return true;
        try {
            String datePart = departure.split(" ")[0];
            LocalDate scheduleDate = LocalDate.parse(datePart);
            return scheduleDate.equals(selectedDate);
        } catch (Exception e) {
            return true;
        }
    }
    
    private boolean filterByBusType(Schedule s) {
        String type = busTypeFilter.getValue();
        return type == null || type.equals("All Types") || s.getBusType().equals(type);
    }
    
    @FXML
    private void resetSearch() {
        fromCombo.setValue("Any");
        toCombo.setValue("Any");
        datePicker.setValue(LocalDate.now());
        busTypeFilter.setValue("All Types");
        loadAllBuses();
    }
    
    private void displaySchedules(List<Schedule> schedules) {
        resultsTable.getItems().clear();
        resultsTable.getItems().addAll(schedules);
        
        if (schedules.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            emptyBox.setStyle("-fx-padding: 40;");
            Label emptyIcon = new Label("🚌");
            emptyIcon.setStyle("-fx-font-size: 48px;");
            Label emptyText = new Label("No buses available");
            emptyText.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            Label emptyHint = new Label("Try different date or destination");
            emptyHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
            emptyBox.getChildren().addAll(emptyIcon, emptyText, emptyHint);
            resultsTable.setPlaceholder(emptyBox);
        }
    }
    
    private void openBookingDialog(Schedule schedule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking_dialog.fxml"));
            Parent root = loader.load();
            BookingController controller = loader.getController();
            controller.setSchedule(schedule);
            Stage stage = new Stage();
            stage.setTitle("Book Ticket");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            handleSearch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Schedule;
import com.busreservation.utils.SessionManager;
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
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchController {

    @FXML private HBox statsCardsContainer;
    @FXML private Label activeSchedulesLabel;
    @FXML private Label totalUsersLabel;

    @FXML private ComboBox<String> fromCombo;
    @FXML private ComboBox<String> toCombo;
    @FXML private ComboBox<String> busTypeFilter;
    @FXML private DatePicker datePicker;
    @FXML private Label resultCountLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button searchButton;
    @FXML private Button todayButton;
    @FXML private Button resetButton;

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
    private List<Schedule> masterSchedules = new ArrayList<>();

    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        if (SessionManager.isAdmin()) {
            statsCardsContainer.setVisible(true);
            statsCardsContainer.setManaged(true);
            loadStatsCards();
        } else {
            statsCardsContainer.setVisible(false);
            statsCardsContainer.setManaged(false);
        }
        
        setupCities();
        setupTable();
        setupAlternatingRowColors();
        datePicker.setValue(null);
        datePicker.setPromptText("Any Date");
        
        addButtonAnimation(searchButton);
        addButtonAnimation(todayButton);
        addButtonAnimation(resetButton);

        loadAllSchedules();
    }
    
    private void addButtonAnimation(Button button) {
        if (button == null) return;
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), button);
            st.setToX(0.97);
            st.setToY(0.97);
            st.play();
        });
        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), button);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }
    
    private void loadStatsCards() {
        int activeSchedules = db.getAllSchedules().size();
        int totalUsers = db.getAllUsers().size();
        
        activeSchedulesLabel.setText(String.valueOf(activeSchedules));
        totalUsersLabel.setText(String.valueOf(totalUsers));
    }

    private void setupCities() {
        String[] cities = {"Any","Digos City","Sta. Cruz","Bansalan","Magsaysay","Kapatagan","Matanao","Hagonoy","Malalag","Padada","Sulop","Kiblawan"};
        fromCombo.getItems().addAll(cities);
        toCombo.getItems().addAll(cities);
        fromCombo.setValue("Any"); 
        toCombo.setValue("Any");
        busTypeFilter.getItems().addAll("All Types","AC","Non-AC","Sleeper","Seater");
        busTypeFilter.setValue("All Types");
    }

    private void setupAlternatingRowColors() {
        resultsTable.setRowFactory(tv -> new TableRow<Schedule>() {
            @Override protected void updateItem(Schedule item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (getIndex() % 2 == 0) setStyle("-fx-background-color: #f8f9fa;");
                else setStyle("-fx-background-color: white;");
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
        colFare.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(String.format("₱%.2f", cd.getValue().getFare())));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button bookButton = new Button("Book");
            {
                bookButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15 5 15; -fx-font-weight: bold");
                addBookButtonAnimation(bookButton);
                bookButton.setOnAction(e -> {
                    Schedule s = getTableView().getItems().get(getIndex());
                    if (s.getAvailableSeats() <= 0) {
                        showAlert("No seats available for this schedule.");
                        return;
                    }
                    if (isScheduleExpired(s)) {
                        showAlert("Cannot book this schedule. Departure time has already passed.");
                        return;
                    }
                    openBookingDialog(s);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Schedule s = getTableView().getItems().get(getIndex());
                int seats = s.getAvailableSeats();
                boolean expired = isScheduleExpired(s);
                
                if (expired) {
                    bookButton.setText("Expired");
                    bookButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    bookButton.setDisable(true);
                } else if (seats <= 0) {
                    bookButton.setText("Sold Out");
                    bookButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    bookButton.setDisable(true);
                } else if (seats <= 5) {
                    bookButton.setText("⚠ Last " + seats);
                    bookButton.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    bookButton.setDisable(false);
                } else {
                    bookButton.setText("Book");
                    bookButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 5 15 5 15; -fx-font-weight: bold;");
                    bookButton.setDisable(false);
                }
                setGraphic(bookButton);
            }
        });
    }
    
    private boolean isScheduleExpired(Schedule schedule) {
        try {
            String departure = schedule.getDepartureTime();
            if (departure == null || departure.isEmpty()) return false;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime departureTime = LocalDateTime.parse(departure, formatter);
            return LocalDateTime.now().isAfter(departureTime);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void addBookButtonAnimation(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), button);
            st.setToX(0.95);
            st.setToY(0.95);
            st.play();
        });
        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), button);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    private void deleteExpiredSchedules() {
        new Thread(() -> {
            int deleted = db.deleteExpiredSchedules();
            if (deleted > 0) {
                System.out.println("Deleted " + deleted + " expired schedules");
            }
        }).start();
    }

    private void loadAllSchedules() {
        deleteExpiredSchedules();
        
        loadingIndicator.setVisible(true);
        new Thread(() -> {
            List<Schedule> schedules = db.getAllSchedules();
            javafx.application.Platform.runLater(() -> {
                masterSchedules = schedules;
                displaySchedules(schedules);
                loadingIndicator.setVisible(false);
                lastUpdatedLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
                updateResultCount(schedules.size());
            });
        }).start();
    }

    @FXML 
    private void handleSearch() {
        loadingIndicator.setVisible(true);
        new Thread(() -> {
            String from = fromCombo.getValue();
            String to = toCombo.getValue();
            LocalDate date = datePicker.getValue();
            String type = busTypeFilter.getValue();

            List<Schedule> filtered = masterSchedules.stream()
                .filter(s -> (from == null || from.equals("Any") || s.getOrigin().equals(from)))
                .filter(s -> (to == null || to.equals("Any") || s.getDestination().equals(to)))
                .filter(s -> {
                    if (date == null) return true;
                    return filterByDate(s, date);
                })
                .filter(s -> (type == null || type.equals("All Types") || s.getBusType().equals(type)))
                .filter(s -> !isScheduleExpired(s))
                .collect(Collectors.toList());

            javafx.application.Platform.runLater(() -> {
                displaySchedules(filtered);
                loadingIndicator.setVisible(false);
                updateResultCount(filtered.size());
            });
        }).start();
    }

    private boolean filterByDate(Schedule s, LocalDate selectedDate) {
        if (selectedDate == null) return true;
        String dep = s.getDepartureTime();
        if (dep == null || dep.isEmpty()) return true;
        try {
            String datePart = dep.split(" ")[0];
            return LocalDate.parse(datePart).equals(selectedDate);
        } catch (Exception e) { return true; }
    }

    private void displaySchedules(List<Schedule> schedules) {
        resultsTable.getItems().setAll(schedules);
        if (schedules.isEmpty()) {
            VBox box = new VBox(8);
            box.setAlignment(javafx.geometry.Pos.CENTER);
            box.setStyle("-fx-padding: 40;");
            Label icon = new Label("🚌"); icon.setStyle("-fx-font-size: 48px;");
            Label text = new Label("No buses found"); text.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            Label hint = new Label("Click Search to find buses"); hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");
            box.getChildren().addAll(icon, text, hint);
            resultsTable.setPlaceholder(box);
        }
    }

    private void updateResultCount(int count) {
        String from = fromCombo.getValue();
        String to = toCombo.getValue();
        if (from != null && !from.equals("Any") && to != null && !to.equals("Any") && !from.equals(to))
            resultCountLabel.setText("🎫 " + count + " buses from " + from + " to " + to);
        else
            resultCountLabel.setText("🚌 " + count + " available buses");
    }

    @FXML
    private void showTodayBuses() { 
        datePicker.setValue(LocalDate.now()); 
        handleSearch();
    }

    @FXML
    private void resetSearch() {
        fromCombo.setValue("Any"); 
        toCombo.setValue("Any");
        datePicker.setValue(null);
        busTypeFilter.setValue("All Types");
        handleSearch();
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String message) {
        Alert a = new Alert(Alert.AlertType.WARNING); 
        a.setHeaderText(null); 
        a.setContentText(message); 
        a.showAndWait();
    }
}
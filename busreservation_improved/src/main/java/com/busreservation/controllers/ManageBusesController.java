package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Bus;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class ManageBusesController {
    
    @FXML private TextField busNumberField;
    @FXML private TextField busNameField;
    @FXML private TextField capacityField;
    @FXML private ComboBox<String> busTypeCombo;
    @FXML private TableView<Bus> busesTable;
    @FXML private TableColumn<Bus, Integer> colId;
    @FXML private TableColumn<Bus, String> colBusNumber;
    @FXML private TableColumn<Bus, String> colBusName;
    @FXML private TableColumn<Bus, Integer> colCapacity;
    @FXML private TableColumn<Bus, String> colType;
    
    private Database db;
    private Bus selectedBus;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        busTypeCombo.getItems().addAll("AC", "Non-AC", "Sleeper", "Seater");
        
        colId.setCellValueFactory(new PropertyValueFactory<>("busId"));
        colBusNumber.setCellValueFactory(new PropertyValueFactory<>("busNumber"));
        colBusName.setCellValueFactory(new PropertyValueFactory<>("busName"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        colType.setCellValueFactory(new PropertyValueFactory<>("busType"));
        
        loadBuses();
        
        busesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedBus = newVal;
                busNumberField.setText(selectedBus.getBusNumber());
                busNameField.setText(selectedBus.getBusName());
                capacityField.setText(String.valueOf(selectedBus.getCapacity()));
                busTypeCombo.setValue(selectedBus.getBusType());
            }
        });
    }
    
    private void loadBuses() {
        List<Bus> buses = db.getAllBuses();
        busesTable.getItems().clear();
        busesTable.getItems().addAll(buses);
    }
    
    @FXML
    private void handleAdd() {
        if (validateFields()) {
            Bus bus = new Bus(0, busNumberField.getText().trim(), busNameField.getText().trim(), Integer.parseInt(capacityField.getText()), busTypeCombo.getValue());
            if (db.addBus(bus)) {
                showAlert("Success", "Bus added", Alert.AlertType.INFORMATION);
                clearFields();
                loadBuses();
            }
        }
    }
    
    @FXML
    private void handleUpdate() {
        if (selectedBus != null && validateFields()) {
            selectedBus.setBusNumber(busNumberField.getText().trim());
            selectedBus.setBusName(busNameField.getText().trim());
            selectedBus.setCapacity(Integer.parseInt(capacityField.getText()));
            selectedBus.setBusType(busTypeCombo.getValue());
            if (db.updateBus(selectedBus)) {
                showAlert("Success", "Bus updated", Alert.AlertType.INFORMATION);
                clearFields();
                loadBuses();
                selectedBus = null;
            }
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedBus != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Delete " + selectedBus.getBusNumber() + "?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                if (db.deleteBus(selectedBus.getBusId())) {
                    showAlert("Success", "Bus deleted", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadBuses();
                    selectedBus = null;
                }
            }
        }
    }
    
    @FXML
    private void handleClear() {
        clearFields();
        selectedBus = null;
        busesTable.getSelectionModel().clearSelection();
    }
    
    private boolean validateFields() {
        if (busNumberField.getText().trim().isEmpty()) return false;
        if (busNameField.getText().trim().isEmpty()) return false;
        try {
            int cap = Integer.parseInt(capacityField.getText().trim());
            if (cap <= 0) return false;
        } catch (NumberFormatException e) {
            return false;
        }
        if (busTypeCombo.getValue() == null) return false;
        return true;
    }
    
    private void clearFields() {
        busNumberField.clear();
        busNameField.clear();
        capacityField.clear();
        busTypeCombo.setValue(null);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
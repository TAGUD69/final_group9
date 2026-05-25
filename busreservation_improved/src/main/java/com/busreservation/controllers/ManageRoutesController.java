package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.Route;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class ManageRoutesController {
    
    @FXML private ComboBox<String> originCombo;
    @FXML private ComboBox<String> destinationCombo;
    @FXML private TextField distanceField;
    @FXML private TextField fareField;
    @FXML private TableView<Route> routesTable;
    @FXML private TableColumn<Route, Integer> colId;
    @FXML private TableColumn<Route, String> colOrigin;
    @FXML private TableColumn<Route, String> colDestination;
    @FXML private TableColumn<Route, Double> colDistance;
    @FXML private TableColumn<Route, String> colFare;
    
    private Database db;
    private Route selectedRoute;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        String[] cities = {"Digos City", "Sta. Cruz", "Bansalan", "Magsaysay", "Kapatagan", "Matanao", "Hagonoy", "Malalag", "Padada", "Sulop", "Kiblawan"};
        originCombo.getItems().addAll(cities);
        destinationCombo.getItems().addAll(cities);
        
        colId.setCellValueFactory(new PropertyValueFactory<>("routeId"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colDistance.setCellValueFactory(new PropertyValueFactory<>("distanceKm"));
        colFare.setCellValueFactory(cellData -> {
            double fare = cellData.getValue().getBaseFare();
            return new javafx.beans.property.SimpleStringProperty("₱" + String.format("%.2f", fare));
        });
        
        loadRoutes();
        
        routesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedRoute = newVal;
                originCombo.setValue(selectedRoute.getOrigin());
                destinationCombo.setValue(selectedRoute.getDestination());
                distanceField.setText(String.valueOf(selectedRoute.getDistanceKm()));
                fareField.setText(String.valueOf(selectedRoute.getBaseFare()));
            }
        });
    }
    
    private void loadRoutes() {
        List<Route> routes = db.getAllRoutes();
        routesTable.getItems().clear();
        routesTable.getItems().addAll(routes);
    }
    
    @FXML
    private void handleAdd() {
        if (validateFields()) {
            Route route = new Route(0, originCombo.getValue(), destinationCombo.getValue(), Double.parseDouble(distanceField.getText()), Double.parseDouble(fareField.getText()));
            if (db.addRoute(route)) {
                showAlert("Success", "Route added", Alert.AlertType.INFORMATION);
                clearFields();
                loadRoutes();
            }
        }
    }
    
    @FXML
    private void handleUpdate() {
        if (selectedRoute != null && validateFields()) {
            selectedRoute.setOrigin(originCombo.getValue());
            selectedRoute.setDestination(destinationCombo.getValue());
            selectedRoute.setDistanceKm(Double.parseDouble(distanceField.getText()));
            selectedRoute.setBaseFare(Double.parseDouble(fareField.getText()));
            if (db.updateRoute(selectedRoute)) {
                showAlert("Success", "Route updated", Alert.AlertType.INFORMATION);
                clearFields();
                loadRoutes();
                selectedRoute = null;
            }
        } else {
            showAlert("Error", "Select a route to update", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedRoute != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Delete route?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                if (db.deleteRoute(selectedRoute.getRouteId())) {
                    showAlert("Success", "Route deleted", Alert.AlertType.INFORMATION);
                    clearFields();
                    loadRoutes();
                    selectedRoute = null;
                }
            }
        } else {
            showAlert("Error", "Select a route to delete", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleClear() {
        clearFields();
        selectedRoute = null;
        routesTable.getSelectionModel().clearSelection();
    }
    
    private boolean validateFields() {
        if (originCombo.getValue() == null || destinationCombo.getValue() == null) {
            showAlert("Error", "Select origin and destination", Alert.AlertType.ERROR);
            return false;
        }
        if (originCombo.getValue().equals(destinationCombo.getValue())) {
            showAlert("Error", "Origin and destination cannot be the same", Alert.AlertType.ERROR);
            return false;
        }
        try {
            double dist = Double.parseDouble(distanceField.getText().trim());
            double fare = Double.parseDouble(fareField.getText().trim());
            if (dist <= 0 || fare <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Error", "Valid distance and fare required", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }
    
    private void clearFields() {
        originCombo.setValue(null);
        destinationCombo.setValue(null);
        distanceField.clear();
        fareField.clear();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
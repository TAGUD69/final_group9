package com.busreservation.controllers;

import com.busreservation.App;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;

public class DashboardController {
    
    @FXML private Label userLabel;
    @FXML private Button reportsButton;
    @FXML private MenuButton adminMenu;
    @FXML private StackPane contentArea;
    
    @FXML
    public void initialize() {
        String fullName = SessionManager.getCurrentUser().getFullName();
        boolean isAdmin = SessionManager.isAdmin();
        
        userLabel.setText("Welcome, " + fullName);
        
        if (isAdmin) {
            reportsButton.setVisible(true);
            adminMenu.setVisible(true);
        } else {
            reportsButton.setVisible(false);
            adminMenu.setVisible(false);
        }
        
        showSearch();
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        App.changeScene("login.fxml");
    }
    
    @FXML
    private void showSearch() {
        loadContent("search.fxml");
    }
    
    @FXML
    private void showBookings() {
        loadContent("bookings.fxml");
    }
    
    @FXML
    private void showReports() {
        loadContent("reports.fxml");
    }
    
    @FXML
    private void showManageBuses() {
        loadContent("manage_buses.fxml");
    }
    
    @FXML
    private void showManageRoutes() {
        loadContent("manage_routes.fxml");
    }
    
    @FXML
    private void showManageSchedules() {
        loadContent("manage_schedules.fxml");
    }
    
    @FXML
    private void showManageUsers() {
        loadContent("manage_users.fxml");
    }
    
    private void loadContent(String fxml) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
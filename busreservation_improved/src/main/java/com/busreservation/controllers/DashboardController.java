package com.busreservation.controllers;

import com.busreservation.App;
import com.busreservation.models.Database;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label userLabel;
    @FXML private Button reportsButton;
    @FXML private MenuButton adminMenu;
    @FXML private StackPane contentArea;
    @FXML private Label statBookingsLabel;
    @FXML private Label statRevenueLabel;
    @FXML private Label statSchedulesLabel;
    @FXML private Label statUsersLabel;
    @FXML private Button findTripButton;
    @FXML private Button myBookingsButton;

    private Database db;

    @FXML
    public void initialize() {
        db = Database.getInstance();
        String fullName = SessionManager.getCurrentUser().getFullName();
        boolean isAdmin = SessionManager.isAdmin();

        userLabel.setText("Welcome, " + fullName);

        reportsButton.setVisible(isAdmin);
        reportsButton.setManaged(isAdmin);
        adminMenu.setVisible(isAdmin);
        adminMenu.setManaged(isAdmin);

        addButtonAnimation(findTripButton);
        addButtonAnimation(myBookingsButton);
        addButtonAnimation(reportsButton);
        
        if (isAdmin) loadStats();
        else {
            if (statBookingsLabel != null) statBookingsLabel.getParent().setVisible(false);
        }

        showSearch();
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

    private void loadStats() {
        try {
            if (statBookingsLabel != null) statBookingsLabel.setText(String.valueOf(db.getTotalBookingsToday()));
            if (statRevenueLabel != null) statRevenueLabel.setText(String.format("₱%.0f", db.getTotalRevenueToday()));
            if (statSchedulesLabel != null) statSchedulesLabel.setText(String.valueOf(db.getTotalActiveSchedules()));
            if (statUsersLabel != null) statUsersLabel.setText(String.valueOf(db.getTotalUsers()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleLogout() { SessionManager.clearSession(); App.changeScene("login.fxml"); }
    @FXML private void showSearch() { loadContent("search.fxml"); }
    @FXML private void showBookings() { loadContent("bookings.fxml"); }
    @FXML private void showReports() { loadContent("reports.fxml"); }
    @FXML private void showManageBuses() { loadContent("manage_buses.fxml"); }
    @FXML private void showManageRoutes() { loadContent("manage_routes.fxml"); }
    @FXML private void showManageSchedules() { loadContent("manage_schedules.fxml"); }
    @FXML private void showManageUsers() { loadContent("manage_users.fxml"); }

    private void loadContent(String fxml) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentArea.getChildren().setAll(content);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
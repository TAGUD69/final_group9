package com.busreservation.controllers;

import com.busreservation.models.Database;
import com.busreservation.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class ManageUsersController {
    
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colCreatedDate;
    
    @FXML private TextField fullNameField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    
    private Database db;
    private User selectedUser;
    
    @FXML
    public void initialize() {
        db = Database.getInstance();
        
        roleCombo.getItems().addAll("user", "admin");
        
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        loadUsers();
        
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                fullNameField.setText(selectedUser.getFullName());
                roleCombo.setValue(selectedUser.getRole());
                passwordField.clear();
            }
        });
    }
    
    private void loadUsers() {
        List<User> users = db.getAllUsers();
        usersTable.getItems().clear();
        usersTable.getItems().addAll(users);
    }
    
    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to update", Alert.AlertType.ERROR);
            return;
        }
        
        if (selectedUser.getUsername().equals("admin") && !roleCombo.getValue().equals("admin")) {
            showAlert("Error", "Cannot change admin role", Alert.AlertType.ERROR);
            return;
        }
        
        String newFullName = fullNameField.getText().trim();
        String newRole = roleCombo.getValue();
        
        if (newFullName.isEmpty()) {
            showAlert("Error", "Full name cannot be empty", Alert.AlertType.ERROR);
            return;
        }
        
        if (db.updateUser(selectedUser.getUserId(), newFullName, newRole)) {
            showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
            loadUsers();
        } else {
            showAlert("Error", "Failed to update user", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleResetPassword() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user", Alert.AlertType.ERROR);
            return;
        }
        
        String newPassword = passwordField.getText();
        if (newPassword.isEmpty()) {
            showAlert("Error", "Please enter a new password", Alert.AlertType.ERROR);
            return;
        }
        
        if (newPassword.length() < 4) {
            showAlert("Error", "Password must be at least 4 characters", Alert.AlertType.ERROR);
            return;
        }
        
        if (db.resetPassword(selectedUser.getUserId(), newPassword)) {
            showAlert("Success", "Password reset successfully", Alert.AlertType.INFORMATION);
            passwordField.clear();
        } else {
            showAlert("Error", "Failed to reset password", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to delete", Alert.AlertType.ERROR);
            return;
        }
        
        if (selectedUser.getUsername().equals("admin")) {
            showAlert("Error", "Cannot delete admin account", Alert.AlertType.ERROR);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete User");
        confirm.setContentText("Delete user '" + selectedUser.getUsername() + "'? This cannot be undone.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (db.deleteUser(selectedUser.getUserId())) {
                showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                handleClear();
                loadUsers();
            } else {
                showAlert("Error", "Failed to delete user", Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleClear() {
        fullNameField.clear();
        roleCombo.setValue(null);
        passwordField.clear();
        selectedUser = null;
        usersTable.getSelectionModel().clearSelection();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
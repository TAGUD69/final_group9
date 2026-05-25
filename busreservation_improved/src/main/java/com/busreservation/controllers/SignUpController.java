package com.busreservation.controllers;

import com.busreservation.App;
import com.busreservation.models.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SignUpController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField confirmPasswordField;
    
    private Database db;
    
    public SignUpController() {
        db = Database.getInstance();
    }
    
    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
            return;
        }
        
        if (password.length() < 4) {
            showAlert("Error", "Password must be at least 4 characters", Alert.AlertType.ERROR);
            return;
        }
        
        if (db.userExists(username)) {
            showAlert("Error", "Username already exists. Please choose another.", Alert.AlertType.ERROR);
            return;
        }
        
        if (db.addUser(username, password, fullName, "user")) {
            showAlert("Success", "Account created successfully! Please login.", Alert.AlertType.INFORMATION);
            closeDialog();
        } else {
            showAlert("Error", "Failed to create account. Please try again.", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
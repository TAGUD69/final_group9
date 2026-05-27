package com.busreservation.controllers;

import com.busreservation.App;
import com.busreservation.models.Database;
import com.busreservation.models.User;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.util.prefs.Preferences;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private Label errorLabel;
    
    private Database db;
    private Preferences prefs;
    
    public LoginController() {
        db = Database.getInstance();
        prefs = Preferences.userNodeForPackage(LoginController.class);
    }
    
    @FXML
    public void initialize() {
        loadSavedCredentials();
        
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
        
        visiblePasswordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });
    }
    
    private void loadSavedCredentials() {
        String savedUsername = prefs.get("remember_username", "");
        String savedPassword = prefs.get("remember_password", "");
        
        if (!savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberMeCheckbox.setSelected(true);
            
            if (!savedPassword.isEmpty()) {
                passwordField.setText(savedPassword);
                visiblePasswordField.setText(savedPassword);
            }
        }
    }
    
    private void saveCredentials(String username, String password) {
        if (rememberMeCheckbox.isSelected()) {
            prefs.put("remember_username", username);
            prefs.put("remember_password", password);
        } else {
            prefs.remove("remember_username");
            prefs.remove("remember_password");
        }
    }
    
    @FXML
    private void togglePasswordVisibility() {
        if (showPasswordCheckbox.isSelected()) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
        
        usernameField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e74c3c; -fx-border-radius: 8; -fx-border-width: 2;");
        passwordField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e74c3c; -fx-border-radius: 8; -fx-border-width: 2;");
    }
    
    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        usernameField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8;");
        passwordField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8;");
    }
    
    @FXML
    private void handleLogin() {
        clearError();
        
        String username = usernameField.getText().trim();
        String password = showPasswordCheckbox.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        
        User user = db.authenticate(username, password);
        if (user != null) {
            saveCredentials(username, password);
            SessionManager.setCurrentUser(user);
            App.changeScene("dashboard.fxml");
        } else {
            showError("Invalid username or password");
        }
    }
    
    @FXML
    private void handleSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Create Account");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
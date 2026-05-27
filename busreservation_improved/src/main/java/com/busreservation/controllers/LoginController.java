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
import java.util.prefs.Preferences;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private CheckBox showPasswordCheckbox;
    
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
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = showPasswordCheckbox.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password", Alert.AlertType.ERROR);
            return;
        }
        
        User user = db.authenticate(username, password);
        if (user != null) {
            saveCredentials(username, password);
            SessionManager.setCurrentUser(user);
            App.changeScene("dashboard.fxml");
        } else {
            showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        visiblePasswordField.clear();
        rememberMeCheckbox.setSelected(false);
        showPasswordCheckbox.setSelected(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
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
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
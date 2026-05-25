package com.busreservation.controllers;

import com.busreservation.App;
import com.busreservation.models.Database;
import com.busreservation.models.User;
import com.busreservation.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private HBox errorContainer;
    @FXML private Label errorLabel;
    
    private Database db;
    
    public LoginController() {
        db = Database.getInstance();
    }
    
    @FXML
    public void initialize() {
        setupEnterKey();
        setupButtonHoverEffect();
    }
    
    private void setupButtonHoverEffect() {
        loginButton.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();
        });
        
        loginButton.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
        });
    }
    
    private void setupEnterKey() {
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
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorContainer.setVisible(true);
        errorContainer.setManaged(true);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorContainer);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
        
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #e74c3c; -fx-border-radius: 5; -fx-border-width: 2;");
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #e74c3c; -fx-border-radius: 5; -fx-border-width: 2;");
        
        Timeline resetBorder = new Timeline(
            new KeyFrame(Duration.seconds(2), e -> {
                if (!usernameField.isFocused()) {
                    usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
                }
                if (!passwordField.isFocused()) {
                    passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
                }
            })
        );
        resetBorder.setCycleCount(1);
        resetBorder.play();
        
        Timeline hideError = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorContainer);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    errorContainer.setVisible(false);
                    errorContainer.setManaged(false);
                });
                fadeOut.play();
            })
        );
        hideError.setCycleCount(1);
        hideError.play();
    }
    
    private void clearError() {
        errorContainer.setVisible(false);
        errorContainer.setManaged(false);
        usernameField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        passwordField.setStyle("-fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
    }
    
    @FXML
    private void handleLogin() {
        clearError();
        
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }
        
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        loadingSpinner.setVisible(true);
        
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            
            User user = db.authenticate(username, password);
            
            javafx.application.Platform.runLater(() -> {
                loadingSpinner.setVisible(false);
                loginButton.setDisable(false);
                loginButton.setText("Login");
                
                if (user != null) {
                    SessionManager.setCurrentUser(user);
                    App.changeScene("dashboard.fxml");
                } else {
                    showError("Invalid username or password");
                }
            });
        }).start();
    }
    
    @FXML
    private void handleClear() {
        usernameField.clear();
        passwordField.clear();
        clearError();
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
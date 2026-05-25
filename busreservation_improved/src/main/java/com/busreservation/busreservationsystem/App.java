package com.busreservation;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {
    
    private static Stage primaryStage;
    private static StackPane transitionContainer;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        transitionContainer = new StackPane();
        Scene scene = new Scene(transitionContainer, 1200, 800);
        
        stage.setTitle("Bus Reservation System - Davao del Sur");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
        
        loadLoginScreen();
    }
    
    private static void loadLoginScreen() {
        try {
            Parent login = FXMLLoader.load(App.class.getResource("/fxml/login.fxml"));
            transitionContainer.getChildren().setAll(login);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void changeScene(String fxml) {
        try {
            Parent newContent = FXMLLoader.load(App.class.getResource("/fxml/" + fxml));
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), transitionContainer.getChildren().get(0));
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            
            fadeOut.setOnFinished(e -> {
                transitionContainer.getChildren().setAll(newContent);
                
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), newContent);
                scaleIn.setFromX(0.95);
                scaleIn.setFromY(0.95);
                scaleIn.setToX(1);
                scaleIn.setToY(1);
                
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newContent);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                
                scaleIn.play();
                fadeIn.play();
            });
            
            fadeOut.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
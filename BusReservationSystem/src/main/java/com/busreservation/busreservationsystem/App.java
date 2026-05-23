package com.busreservation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Bus Reservation System - Davao del Sur");
        stage.setScene(scene);
        
        // Make window open MAXIMIZED (fills the screen)
        stage.setMaximized(true);
        
        stage.show();
    }
    
    public static void changeScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource("/fxml/" + fxml));
            primaryStage.getScene().setRoot(root);
            
            // Keep window maximized when changing scenes
            primaryStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
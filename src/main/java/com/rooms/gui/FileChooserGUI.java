package com.rooms.gui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class FileChooserGUI extends Application {

    private ProgressBar progressBar;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox();
        root.setSpacing(20);
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Button chooseFileButton = new Button("Choose Excel File");
        chooseFileButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        chooseFileButton.setOnAction(event -> chooseFile(primaryStage));

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #FF0000;");

        root.getChildren().addAll(chooseFileButton, progressBar, statusLabel);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            progressBar.setVisible(true); // Show the progress bar
            setStatus("Processing file...", Color.BLACK); // Change status to indicate processing
            // Simulate processing (you can replace this with your actual processing logic)
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    // Simulate processing
                    for (int i = 0; i <= 100; i++) {
                        try {
                            Thread.sleep(50); // Simulate processing time
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        updateProgress(i, 100);
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                setStatus("File processed successfully.", Color.GREEN);
                progressBar.setVisible(false); // Hide the progress bar

            });

            task.setOnFailed(event -> {
                setStatus("Error processing file.", Color.RED);
                progressBar.setVisible(false); // Hide the progress bar
            });

            new Thread(task).start();
        } else {
            setStatus("File selection canceled.", Color.RED);
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

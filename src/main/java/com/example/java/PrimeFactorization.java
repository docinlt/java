package com.example.java;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrimeFactorization extends Application {

    private boolean running = false;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Pirminių dauginamųjų skaidymas");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        Label startLabel = new Label("Nuo:");
        TextField startField = new TextField();
        Label endLabel = new Label("Iki:");
        TextField endField = new TextField();
        Label stepLabel = new Label("Žingsnis:");
        TextField stepField = new TextField();

        Button startButton = new Button("Pradėti");
        Button stopButton = new Button("Baigti");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        Label statusLabel = new Label("Būsena: Pasiruošęs");

        grid.add(startLabel, 0, 0);
        grid.add(startField, 1, 0);
        grid.add(endLabel, 0, 1);
        grid.add(endField, 1, 1);
        grid.add(stepLabel, 0, 2);
        grid.add(stepField, 1, 2);
        grid.add(startButton, 0, 3);
        grid.add(stopButton, 1, 3);
        grid.add(progressBar, 0, 4, 2, 1);
        grid.add(statusLabel, 0, 5, 2, 1);

        startButton.setOnAction(_ -> {
            if (running) return;

            try {
                int start = Integer.parseInt(startField.getText());
                int end = Integer.parseInt(endField.getText());
                int step = Integer.parseInt(stepField.getText());
                startCalculation(start, end, step, progressBar, statusLabel);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Klaida: Įveskite skaičius!");
            }
        });

        stopButton.setOnAction(_ -> running = false);

        Scene scene = new Scene(grid, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startCalculation(int start, int end, int step, ProgressBar progressBar, Label statusLabel) {
        running = true;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                int total = ((end - start) / step) + 1;
                int count = 0;

                try (FileWriter writer = new FileWriter("rezultatai.txt", true)) {
                    writer.write(getTimestamp() + " Skaičiavimo pradžia. Naudojami skaičiai: nuo " + start + " iki " + end + ", kas " + step + ".\n");

                    for (int i = start; i <= end; i += step) {
                        if (!running) break;

                        int[] factors = primeFactors(i);
                        StringBuilder result = new StringBuilder(i + "=");
                        for (int factor : factors) {
                            result.append(factor).append("*");
                        }
                        result.deleteCharAt(result.length() - 1);

                        writer.write(getTimestamp() + " " + result + "\n");
                        count++;
                        updateProgress(count, total);

                        String currentStatus = "Skaidomas skaičius: " + i;
                        updateMessage(currentStatus);

                        Thread.sleep(500);
                    }

                    writer.write(getTimestamp() + " Skaičiavimo pabaiga.\n");
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }

                running = false;
                updateMessage("Skaidymas baigtas. Rezultatai faile rezultatai.txt");
                return null;
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        statusLabel.textProperty().bind(task.messageProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private int[] primeFactors(int n) {
        java.util.List<Integer> factors = new java.util.ArrayList<>();
        for (int i = 2; i <= n; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        return factors.stream().mapToInt(Integer::intValue).toArray();
    }

    private String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    public static void main(String[] args) {
        launch(args);
    }
}

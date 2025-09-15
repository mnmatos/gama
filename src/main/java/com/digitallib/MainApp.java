package com.digitallib;

import com.digitallib.swing.LoadingScreen;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    public MainApp() {
        setTitle("Main Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        startLongRunningTask();
    }

    private void startLongRunningTask() {
        LoadingScreen loadingScreen = new LoadingScreen(this, "Loading...", true);
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish("Loading...");
                DocumentList.main();
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                // Update the loading screen message on the Event Dispatch Thread (EDT)
                for (String message : chunks) {
                    loadingScreen.setMessage(message);
                }
            }

            @Override
            protected void done() {
                loadingScreen.dispose(); // Close loading screen when task is done
                JOptionPane.showMessageDialog(MainApp.this, "Task completed!");
            }
        };

        worker.execute(); // Start the SwingWorker
        loadingScreen.setVisible(true); // Show the loading screen
    }
}
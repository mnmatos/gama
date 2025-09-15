package com.digitallib.swing;

import javax.swing.*;
import java.awt.*;

public class LoadingScreen extends JDialog {

    private JProgressBar progressBar;
    private JLabel messageLabel;

    public LoadingScreen(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setSize(400, 200);
        setLocationRelativeTo(getOwner());

        ImageIcon imageIcon = new ImageIcon("src/main/resources/gama_loading.png");
        JLabel imageLabel = new JLabel(imageIcon);
        add(imageLabel, BorderLayout.CENTER);

        messageLabel = new JLabel("Loading, please wait...", SwingConstants.CENTER);
        add(messageLabel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        // progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setProgress(int value) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(value);
    }
}

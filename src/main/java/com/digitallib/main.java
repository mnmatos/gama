package com.digitallib;

import com.digitallib.utils.ConfigReader;

import javax.swing.*;

public class main {

    public static final String ACERVO = "acervo";

    public static void main(String[] args) {
        initializeAcervo();
        SwingUtilities.invokeLater(() -> {
            new MainApp().setVisible(true);
        });
    }

    private static void initializeAcervo() {
        String acervo = ConfigReader.getProperty(ACERVO);
        if (acervo == null) {
            String input = JOptionPane.showInputDialog(null, "Antes de continuar, insira a sigla de seu Acervo", "Atenção!", JOptionPane.QUESTION_MESSAGE);

            if (input != null && !input.trim().isEmpty()) {
                ConfigReader.setProperty(ACERVO, input);
                System.out.println("You entered: " + input);
            } else {
                System.out.println("No input provided.");
                throw new RuntimeException("No acervo provided");
            }
        }
    }

}

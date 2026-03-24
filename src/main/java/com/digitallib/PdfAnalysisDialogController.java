package com.digitallib;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PdfAnalysisDialogController implements Initializable {
    private static final Logger logger = LogManager.getLogger(PdfAnalysisDialogController.class);

    @FXML private Label wordCountLabel;
    @FXML private TableView<Map.Entry<String, Long>> wordCountTable;
    @FXML private TableColumn<Map.Entry<String, Long>, String> wordCol;
    @FXML private TableColumn<Map.Entry<String, Long>, Number> countCol;
    @FXML private Pagination pagination;

    private List<Map.Entry<String, Long>> wordFrequenciesList;
    private final int PAGE_SIZE = 50;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wordCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        countCol.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().getValue()));

        pagination.setPageFactory(this::createPage);
    }

    public void setFile(File file) {
        if (file != null) {
            analyzePdf(file);
        }
    }

    private void analyzePdf(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            String[] words = text.split("\\s+");

            wordCountLabel.setText("Total de palavras: " + words.length);

            // Using Set of trivial words to ignore? The original code might have had one.
            // Assuming basic counting for now.
            Map<String, Long> wordCounts = Arrays.stream(words)
                    .map(String::toLowerCase)
                    .map(s -> s.replaceAll("[^a-zA-Z0-9áàâãéèêíïóôõöúçñ]", "")) // Simple cleanup
                    .filter(s -> s.length() > 3) // Ignore short words
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            wordFrequenciesList = new ArrayList<>(wordCounts.entrySet());
            wordFrequenciesList.sort(Map.Entry.<String, Long>comparingByValue().reversed());

            int totalPages = (int) Math.ceil((double) wordFrequenciesList.size() / PAGE_SIZE);
            pagination.setPageCount(totalPages > 0 ? totalPages : 1);

            updateTable(0);

        } catch (IOException e) {
            logger.error(e);
            wordCountLabel.setText("Erro ao ler PDF.");
        }
    }

    private javafx.scene.Node createPage(int pageIndex) {
        updateTable(pageIndex);
        return new VBox(); // Dummy, table is updated directly
    }

    private void updateTable(int pageIndex) {
        if (wordFrequenciesList == null) return;

        int fromIndex = pageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, wordFrequenciesList.size());

        if (fromIndex < wordFrequenciesList.size()) {
             wordCountTable.setItems(FXCollections.observableArrayList(wordFrequenciesList.subList(fromIndex, toIndex)));
        } else {
             wordCountTable.setItems(FXCollections.emptyObservableList());
        }
    }
}

package org.g3_dev.management;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;
import org.apache.commons.io.IOUtils;
import org.g3_dev.MainApp;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe che gestisce le funzionalità necessarie all'implementazione della WordCloud.
 */
public class WordCloudManagement {
    private static final Logger LOGGER = Logger.getLogger(WordCloudManagement.class.getName());

    private WordCloudManagement() {
        throw new IllegalStateException("Classe usata come raccolta di utility");
    }

    /**
     * Genera il file temporaneo output.png contenente la Word Cloud nella directory locale del programma
     *
     * @param texts lista di string contenente tutti i testi dei tweet da analizzare
     */
    public static void buildWordCloud(List<String> texts) {
        if (!texts.isEmpty()) {
            FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
            frequencyAnalyzer.setMinWordLength(3);

            frequencyAnalyzer.setWordFrequenciesToReturn(50);

            try {
                frequencyAnalyzer.setStopWords(loadStopWords());
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Errore lettura file text/stop_words!");
            }

            List<WordFrequency> wordFrequencies = null;
            wordFrequencies = frequencyAnalyzer.load(texts);
            Dimension dimension = new Dimension(400, 300);

            WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
            wordCloud.setKumoFont(new KumoFont("Helvetica Neue", FontWeight.PLAIN));
            wordCloud.setPadding(2);
            wordCloud.setBackground(new RectangleBackground(dimension));
            wordCloud.setBackgroundColor(new Color(0x000000FF, true));
            wordCloud.setColorPalette(new ColorPalette(
                    new Color(0xf29e02),
                    new Color(0x681313),
                    new Color(0xef6b07),
                    new Color(0x685e13),
                    new Color(0x277B05),
                    new Color(0x1e662c),
                    new Color(0x1e2366),
                    new Color(0x0B10AD),
                    new Color(0x7D0586),
                    new Color(0x0E5A68),
                    new Color(0x4d1d66),
                    new Color(0x30251d),
                    new Color(0x000000)));
            wordCloud.setFontScalar(new LinearFontScalar(10, 40));
            if (!wordFrequencies.isEmpty()) {
                LOGGER.log(Level.INFO, "=====================INIZIO CREAZIONE WORD CLOUD=======================");
                try {
                    wordCloud.build(wordFrequencies);
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                }
            } else {
                LOGGER.log(Level.INFO, "=====================NON RIESCO A CREARE LA WORD CLOUD, STRINGA VUOTA=======================");
            }
            wordCloud.writeToFile(MainApp.getProgramDir() + "output.png");
            LOGGER.log(Level.INFO, "=====================TWEETS TROVATI=======================");
        } else {
            LOGGER.log(Level.INFO, "=====================NULL=========================");
        }
    }

    /**
     * Cancella il file temporaneo output.png contenente la Word Cloud nella directory locale del programma
     */
    public static void deleteWordCloud() {
        String fp = MainApp.getProgramDir() + "output.png";
        try {
            Files.delete(Path.of(fp));
            LOGGER.log(Level.INFO, () -> "Cancellato file: " + fp);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, () -> "Impossibile cancellare file: " + fp);
        }
    }

    /**
     * Carica le stopwords dal file /resources/text/stopwords
     *
     * @return set di string corrispondenti alle stopwords
     * @throws IOException
     */
    static Set<String> loadStopWords() throws IOException {
        return new HashSet<>(IOUtils.readLines(Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("text/stop_words"))));
    }

}


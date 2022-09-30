package org.g3_dev;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.embed.swing.SwingNode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringEscapeUtils;
import org.g3_dev.management.MapsManagement;
import org.g3_dev.management.RestoredTweet;
import org.g3_dev.management.WordCloudManagement;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.scene.image.ImageView;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller dell'interfaccia per la visualizzazione delle analisi principali su di una ricerca.
 */
public class AnalyzedResultsComplete {
    private ResourceBundle bundle;
    private Stage detailsStage;
    private Stage landingStage;
    private Stage analyzedStage;
    private ArrayList<RestoredTweet> lastSearchResult;
    private static final Logger LOGGER = Logger.getLogger(AnalyzedResultsComplete.class.getName());
    private static final String LBLNT = "lbl_numberTweets";
    private static final String APPCSS = "/styleCSS/application.css";
    private static final String TAPP = "title_Application";
    private int lastSearchmode;

    @FXML
    private Label lblNumTweet;

    @FXML
    private Label lblMediaOraria;

    @FXML
    private Label lblNumTweetGeo;

    @FXML
    private Label lblPercTweetGeo;

    @FXML
    private Label lblNumTweetImg;

    @FXML
    private BarChart<String, Integer> bcHistogram;

    @FXML
    private StackPane spMap;

    @FXML
    private ImageView imgWordCloud;

    @FXML
    private Button btnGeoDetails;


    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
    }


    // METODI SETTER

    public void setLastSearchResult(List<RestoredTweet> lastSearchResult) {
        this.lastSearchResult = (ArrayList<RestoredTweet>) lastSearchResult;
    }


    // REPERIMENTO STAGE

    public void setLandingStage(Stage landingStage) {
        this.landingStage = landingStage;
    }

    public void setAnalyzedStage(Stage analyzedStage) {
        this.analyzedStage = analyzedStage;
    }

    public void setLastSearchmode(int lastSearchmode) {
        this.lastSearchmode = lastSearchmode;
    }


    // AZIONI DA BUTTON

    /**
     * Apre una scena che permette di condividere i risultati delle analisi effettuale aggiungendo anche un messaggio
     *
     * @throws IOException
     */
    @FXML
    void shareAnalysis() throws IOException {
        List<String> filenames = new ArrayList<>();
        filenames.add(MainApp.getProgramDir() + "landingStage.png");
        filenames.add(MainApp.getProgramDir() + "analyzedStage.png");
        filenames.add(MainApp.getProgramDir() + "detailsStage.png");

        // effettuo gli screenshot

        if (!Objects.isNull(landingStage))
            takeSnapshot(landingStage, filenames.get(0));
        if (!Objects.isNull(analyzedStage))
            takeSnapshot(analyzedStage, filenames.get(1));
        if (!Objects.isNull(detailsStage))
            takeSnapshot(detailsStage, filenames.get(2));

        // apro il frame per l'inserimento del messaggio di condivisione
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                .getResource("/fxml/Share.fxml")), bundle);
        Parent layout = (Parent) fxmlLoader.load();
        Share controller = fxmlLoader.<Share>getController();
        controller.setImages(filenames);

        Stage shareStage = new Stage();
        shareStage.setTitle(bundle.getString(TAPP) + " - " +
                bundle.getString("btn_share"));
        shareStage.setScene(new Scene(layout));
        shareStage.setMinHeight(500);
        shareStage.setMinWidth(900);
        shareStage.setResizable(false);
        shareStage.setOnCloseRequest(event -> {
            for (String s : filenames) {
                try {
                    Files.delete(Path.of(s));
                    LOGGER.log(Level.INFO, () -> "Cancellata immagine condivisa: " + s);
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, () -> "Impossibile cancellare immagine condivisa: " + s + " (già cancellata)");
                }
            }
        });
        shareStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource(APPCSS)).toExternalForm());
        shareStage.show();
        layout.requestFocus();
        controller.setStage(shareStage);
    }

    /**
     * Apre una scena contenente maggiori dettagli riguardo i tweet geolocalizzati presenti nella ricerca
     *
     * @throws IOException
     */
    @FXML
    void openGeoDeteails() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                .getResource("/fxml/GeoDetails.fxml")), bundle);
        Parent layout = (Parent) fxmlLoader.load();
        GeoDetails controller = fxmlLoader.<GeoDetails>getController();
        controller.setTweets(lastSearchResult);

        Stage geoDetailsStage = new Stage();
        geoDetailsStage.setTitle(bundle.getString(TAPP) + " - " +
                bundle.getString("btn_geoDetails"));
        geoDetailsStage.setScene(new Scene(layout));
        geoDetailsStage.setMinHeight(400);
        geoDetailsStage.setMinWidth(700);
        geoDetailsStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource(APPCSS)).toExternalForm());
        geoDetailsStage.show();
        layout.requestFocus();
    }

    /**
     * Mostra un istogramma dettagliato con per ogni ora della giornata il numero di tweet effettuati
     */
    @FXML
    void getDetailedBarchartHourAvg() {
        if (detailsStage == null) {
            Map<String, Integer> map = new HashMap<>();
            for (int i = 0; i < 24; i++)
                if (i < 10)
                    map.put("0" + i, 0);
                else
                    map.put(String.valueOf(i), 0);

            populateHourlyMap(map, lastSearchResult);
            Map<String, Integer> sortedMap = mapSorting(map);

            detailsStage = new Stage();
            detailsStage.setTitle(bundle.getString(TAPP) + " - " +
                    bundle.getString("title_HotHours"));
            final CategoryAxis xAxis = new CategoryAxis();
            final NumberAxis yAxis = new NumberAxis();
            final BarChart<String, Integer> bc = new BarChart<>(xAxis, (Axis) yAxis);
            bc.setTitle(bundle.getString("title_HotHours"));
            xAxis.setLabel(bundle.getString("lbl_hours"));
            yAxis.setLabel(bundle.getString(LBLNT));
            xAxis.setTickLabelFont(Font.font("Helvetica Neue"));
            yAxis.setTickLabelFont(Font.font("Helvetica Neue"));

            XYChart.Series<String, Integer> dataSeries = new XYChart.Series<>();
            for (Map.Entry<String, Integer> entry : sortedMap.entrySet())
                dataSeries.getData().add(
                        new XYChart.Data<>((entry.getKey()), entry.getValue()));

            Scene scene = new Scene(bc, 800, 300);
            bc.getData().add(dataSeries);
            scene.getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource(APPCSS)).toExternalForm());
            detailsStage.setScene(scene);
            bc.setLegendVisible(false);
            detailsStage.show();
        } else if (detailsStage.isShowing()) {
            detailsStage.toFront();
        } else {
            detailsStage.show();
        }
    }


    // METODI DI RENDERING DELLE COMPONENTI

    /**
     * Renderizza l'Istogramma nella GUI
     */
    public void renderHistogram() {
        boolean days = true;
        Map<String, Integer> unsortedOccurrence = new HashMap<>();
        for (RestoredTweet t : lastSearchResult) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ITALY);
            String tDate = formatter.format(t.getTimestamp());
            if (unsortedOccurrence.containsKey(tDate)) {
                int count = unsortedOccurrence.get(tDate);
                unsortedOccurrence.put(tDate, count + 1);
            } else
                unsortedOccurrence.put(tDate, 1);
        }
        String giornoSingolo = "";
        if (unsortedOccurrence.keySet().size() == 1) {
            giornoSingolo = (String) unsortedOccurrence.keySet().toArray()[0];
            days = false;
            unsortedOccurrence = histogramByHours();
        }

        Map<String, Integer> sortedOccurrency = mapSorting(unsortedOccurrence);

        // stup label degli assi
        bcHistogram.getXAxis().setLabel(days ? bundle.getString("lbl_day") : bundle.getString("lbl_hours"));
        bcHistogram.getYAxis().setLabel(bundle.getString(LBLNT));

        // Serie
        XYChart.Series<String, Integer> dataSeries = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : sortedOccurrency.entrySet())
            dataSeries.getData().add(
                    new XYChart.Data<>(days ? reorderDate(entry.getKey()) : entry.getKey(), entry.getValue()));

        // Add Series to BarChart.
        bcHistogram.getData().add(dataSeries);
        bcHistogram.setTitle(days ? bundle.getString("lbl_tweetsOverTime") : (bundle.getString("lbl_tweets") +
                " " + reorderDate(giornoSingolo)));
        bcHistogram.setLegendVisible(false);
    }

    /**
     * Renderizza la Word Cloud nella GUI
     */
    public void renderWordCloud() {
        // recupero in una lista solo il testo di ogni tweet ritornato da una ricerca
        List<String> texts = lastSearchResult.
                stream().
                map(RestoredTweet::getTesto).
                collect(Collectors.toList());

        List<String> cleanedTexts = new ArrayList<>();
        for (String s : texts)
            cleanedTexts.add(StringEscapeUtils.escapeEcmaScript(s).replaceAll("'", "''"));

        WordCloudManagement.deleteWordCloud();
        WordCloudManagement.buildWordCloud(cleanedTexts);


        try {
            imgWordCloud.setImage(new Image(new FileInputStream(MainApp.getProgramDir() + "output.png")));
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.WARNING, "File output.png della wordcloud non trovato!");
        }
    }

    /**
     * Renderizza la Mappa nella GUI
     */
    public void renderMap() {
        final SwingNode swingNode = new SwingNode();
        this.createSwingContent(swingNode);
        spMap.getChildren().add(swingNode);
    }

    /**
     * Renderizza la sezione dei dettagli nella GUI
     */
    public void renderLabels() {
        lblNumTweet.setText(bundle.getString(LBLNT) + " : " + lastSearchResult.size());
        lblMediaOraria.setText(bundle.getString("lbl_hourlyAvg") + " : "
                + MainApp.getDecimalFormat().format(this.getTweetPerHourAvg(lastSearchResult)));
        lblNumTweetGeo.setText(bundle.getString("lbl_numberTweetsGeo") + " : "
                + this.getNumberOfGeoTweets(lastSearchResult));
        lblPercTweetGeo.setText(bundle.getString("lbl_percTweetsGeo") + " : "
                + MainApp.getDecimalFormat().format(this.getGeoTweetPerc(lastSearchResult)) + "%");
        lblNumTweetImg.setText(bundle.getString("lbl_numberTweetsImg") + " : "
                + this.countTweetsWithImage(lastSearchResult));
    }

    /**
     * Genera la Mappa in formato Java Swing
     *
     * @param swingNode spazio JavaFX dedicato alla Mappa in grado di ospitare contenuti Java Swing
     */
    private void createSwingContent(SwingNode swingNode) {
        SwingUtilities.invokeLater(() -> {
            List<GeoPosition> track = new ArrayList<>();
            List<RestoredTweet> tweetsCollection = new ArrayList<>();
            for (int i = lastSearchResult.size() - 1; i >= 0; i--) {
                RestoredTweet o = lastSearchResult.get(i);
                if (o.getLatitudine() != null && o.getLongitudine() != null) {
                    int[] lat = MapsManagement.decToDms(o.getLatitudine());
                    int[] lon = MapsManagement.decToDms(o.getLongitudine());
                    track.add(new GeoPosition(lat[0], lat[1], lat[2], lon[0], lon[1], lon[2]));
                    tweetsCollection.add(o);
                }
            }
            if (track.isEmpty()) {
                swingNode.setContent(new JLabel(bundle.getString("lbl_messageNoGeoTweets")));
                MainApp.summonAlert(bundle.getString("lbl_messageNoGeoTweets"), Alert.AlertType.WARNING);
                btnGeoDetails.setVisible(false);
                btnGeoDetails.setDisable(true);
            } else {
                JXMapViewer mapViewer = new MapsManagement().getMapVisualization(track, tweetsCollection, lastSearchmode);
                swingNode.setContent(mapViewer);
            }
        });
    }

    /**
     * Metodo di supporto al rendering che permette di riordinale una data dal formato yyyy/MM/dd
     * al più classico dd/MM/yyyy
     *
     * @param date data nel formato yyyy/MM/dd
     * @return data riformattata come dd/MM/yyyy
     */
    private String reorderDate(String date) {
        String[] split = date.split("/");
        return split[2] + "/" + split[1] + "/" + split[0];
    }

    /**
     * Metodo di supporto al rendering che permette di generare la Map contente le informazioni necessari per
     * generare l'istogramma per fasce orarie in base ai dati che si possiedono
     *
     * @return la Map contente le informazioni necessari per generare l'istogramma per fasce orarie
     */
    private Map<String, Integer> histogramByHours() {
        Map<String, Integer> unsortedOccurrence = new HashMap<>();
        unsortedOccurrence.put("00-05", 0);
        unsortedOccurrence.put("06-11", 0);
        unsortedOccurrence.put("12-17", 0);
        unsortedOccurrence.put("18-23", 0);
        for (RestoredTweet t : lastSearchResult) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH", Locale.ITALY);
            int h = Integer.parseInt(formatter.format(t.getTimestamp()));
            String range = "";
            if (h >= 0 && h < 6)
                range = "00-05";
            else if (h >= 6 && h < 12)
                range = "06-11";
            else if (h >= 12 && h < 18)
                range = "12-17";
            else if (h >= 18 && h <= 23)
                range = "18-23";
            if (unsortedOccurrence.containsKey(range)) {
                int count = unsortedOccurrence.get(range);
                unsortedOccurrence.put(range, count + 1);
            } else
                unsortedOccurrence.put(range, 1);
        }
        return unsortedOccurrence;
    }

    /**
     * Metodo che si occupa di riordinare una Map in input in base alle proprie key di tipo String
     *
     * @param unsortedOccurrence Map dalle key di tipo String e valori Integer non ordinata
     * @return Map fornita in input riordinata secondo le proprie key
     */
    private Map<String, Integer> mapSorting(Map<String, Integer> unsortedOccurrence) {
        return unsortedOccurrence.entrySet().stream().
                sorted(Map.Entry.comparingByKey()).
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }


    // METODI PER LE FUONZIONALITÀ DI ANALISI

    /**
     * Calcola il numero di tweet geolocalizzati tra quelli della ricerca in analisi
     *
     * @param lastSearchResult insieme dei tweet da analizzare
     * @return numero di tweet geolocalizzati
     */
    private int getNumberOfGeoTweets(ArrayList<RestoredTweet> lastSearchResult) {
        int count = 0;
        for (RestoredTweet t : lastSearchResult)
            if (t.getLatitudine() != null && t.getLongitudine() != null)
                count++;
        return count;
    }

    /**
     * Calcola la percentuale di tweet geolocalizzati tra quelli della ricerca in analisi
     *
     * @param lastSearchResult insieme dei tweet da analizzare
     * @return percentuale di tweet geolocalizzati
     */
    private Double getGeoTweetPerc(ArrayList<RestoredTweet> lastSearchResult) {
        Double tot = Double.parseDouble("" + lastSearchResult.size());
        Double numGeo = Double.parseDouble("" + this.getNumberOfGeoTweets(lastSearchResult));
        return numGeo / tot * 100.0;
    }

    /**
     * Calcola la media oraria di tweet pubblicati in un'ora tra quelli della ricerca in analisi
     * (calcolata come numero_tweet / 24)
     *
     * @param lastSearchResult insieme dei tweet da analizzare
     * @return media oraria di tweet pubblicati in un'ora
     */
    private Double getTweetPerHourAvg(ArrayList<RestoredTweet> lastSearchResult) {
        return lastSearchResult.size() / 24.0;
    }

    /**
     * Calcola il conteggio dei tweet con almeno una immagine
     *
     * @param lastSearchResult insieme dei tweet da analizzare
     * @return numero di tweet con almeno una immagine
     */
    private int countTweetsWithImage(ArrayList<RestoredTweet> lastSearchResult) {
        int count = 0;

        for (RestoredTweet t : lastSearchResult)
            if (t.hasImmagine())
                count++;

        return count;
    }

    /**
     * Partendo da una Map dalle chiavi String e valori Integer la popola inserendo coppie
     * (ora_della_giornata [key] - numero_tweet_a_tale_ora [value])
     *
     * @param map              Map dalle chiavi String e valori Integer da popolare
     * @param lastSearchResult insieme dei tweet da analizzare
     */
    private void populateHourlyMap(Map<String, Integer> map, ArrayList<RestoredTweet> lastSearchResult) {
        for (RestoredTweet t : lastSearchResult) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH", Locale.ITALY);
            String tDate = formatter.format(t.getTimestamp());
            if (map.containsKey(tDate)) {
                int count = map.get(tDate);
                map.put(tDate, count + 1);
            } else
                map.put(tDate, 1);
        }
    }


    // METODI PER LA FUONZIONALITÀ DI CONDIVISIONE

    /**
     * Effettua uno screenshot di una scena e lo salva nella specificata path
     *
     * @param stage    stage di cui effettuare uno screenshot
     * @param filename path di salvataggio (che comprende il nome del file) dello screenshot
     */
    private void takeSnapshot(Stage stage, String filename) {
        WritableImage snapshot = stage.getScene().snapshot(null);
        try (OutputStream outputStream = new FileOutputStream(filename)) {
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", outputStream);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, () -> "Non riesco a scrivere sul file: " + filename);
        }
    }

}

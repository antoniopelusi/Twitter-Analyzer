package org.g3_dev;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.g3_dev.management.RestoredTweet;
import org.g3_dev.management.SqliteManagement;
import org.g3_dev.management.TwitterManagement;
import org.g3_dev.management.WordCloudManagement;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller dell'interfaccia principale.
 */
public class LandingPage {
    private ResourceBundle bundle;
    private Stage landingStage;
    private Stage analyzedStage;
    private Stage historyStage;
    private Stage settingsStage;
    private Stage apiKeysFormStage;

    private static String[] searchTopicStrings;
    private List<RestoredTweet> lastSearchResult;

    private static final String TA = "title_Application";
    private static final Logger LOGGER = Logger.getLogger(LandingPage.class.getName());
    private static final String CSSPATH = "/styleCSS/application.css";

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> searchTopic;

    @FXML
    private ListView<String> listResults;

    @FXML
    private Button btnAnalyze;

    @FXML
    private CheckBox cbGeo;

    @FXML
    private Button btnDateInfo;

    @FXML
    private DatePicker dpStart;

    @FXML
    private DatePicker dpEnd;

    @FXML
    private Label lbl1;

    @FXML
    private Label lbl2;


    @FXML
    public void initialize() throws IOException {
        new SqliteManagement(); // non serve ad altro per questa classe
        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
        updateSearchTopicStrings(bundle);
        searchTopic.getItems().addAll(searchTopicStrings);
        resetGUI();
        checkForAPI();
    }


    // GETTER & SETTER

    public static String[] getSearchTopicStrings() {
        return searchTopicStrings;
    }

    public void setLastSearchResult(List<RestoredTweet> lastSearchResult) {
        this.lastSearchResult = lastSearchResult;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public ComboBox<String> getSearchTopic() {
        return searchTopic;
    }

    public ListView<String> getListResults() {
        return listResults;
    }

    public CheckBox getCbGeo() {
        return cbGeo;
    }

    public DatePicker getDpStart() {
        return dpStart;
    }

    public DatePicker getDpEnd() {
        return dpEnd;
    }

    public Label getLbl1() {
        return lbl1;
    }

    public Label getLbl2() {
        return lbl2;
    }

    public Button getBtnDateInfo() {
        return btnDateInfo;
    }

    public Button getBtnAnalyze() {
        return btnAnalyze;
    }


    // REPERIMENTO STAGE

    public void setLandingStage(Stage landingStage) {
        this.landingStage = landingStage;
    }


    // AZIONI DA BUTTON

    /**
     * Avvia una ricerca
     *
     * @throws IOException
     */
    @FXML
    void searchForResults() throws IOException {
        this.checkForAPI();
        if (!(searchField.getText() == null
                || searchField.getText().equals(""))
                && !searchTopic.getSelectionModel().isSelected(-1)) {
            TwitterManagement tm = new TwitterManagement();

            // la ricerca ha inizio
            listResults.setItems(null);
            listResults.setDisable(true);
            btnAnalyze.setDisable(true);

            LocalDate start = dpStart.getValue();
            LocalDate end = dpEnd.getValue();
            if (searchTopic.getValue().equals(searchTopicStrings[0])
                    || searchTopic.getValue().equals(searchTopicStrings[1])) {   //cerco per Trend
                Timestamp[] ts = checkTimestamp(start, end);
                Timestamp tsStart = ts[0];
                Timestamp tsEnd = ts[1];
                String search = searchField.getText();
                Optional<List<String>> tweetsFromSearch;
                if (searchTopic.getValue().equals(searchTopicStrings[0]))
                    tweetsFromSearch = tm.getListTweetByTrend(search, tsStart, tsEnd);
                else
                    tweetsFromSearch = tm.getListTweetByPosition(search, tsStart, tsEnd);
                populateListView(tweetsFromSearch);
            } else if (searchTopic.getValue().equals(searchTopicStrings[2])) {     //cerco per Username
                String username = searchField.getText();
                Optional<List<String>> tweetsFromUsername =
                        tm.getListTweetByUserId(username, cbGeo.isSelected());
                populateListView(tweetsFromUsername);
            }
            this.lastSearchResult = (ArrayList<RestoredTweet>) tm.getLastSearchResult();

            // la ricerca è terminata
            listResults.setDisable(false);
            btnAnalyze.setDisable(false);
        } else {
            MainApp.summonAlert(bundle.getString("alert_searchParametersError"), Alert.AlertType.ERROR);
            resetGUI();
        }
    }

    /**
     * Apre l'analisi della ricerca correntemente visualizzata
     *
     * @throws IOException
     */
    @FXML
    void analyzeResults() throws IOException {
        if (analyzedStage == null) {
            FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                    .getResource("/fxml/AnalyzedResultsComplete.fxml")), bundle);
            Parent layout = (Parent) fxmlLoader.load();
            AnalyzedResultsComplete controller = fxmlLoader.<AnalyzedResultsComplete>getController();
            controller.setLastSearchResult(lastSearchResult);
            controller.setLandingStage(landingStage);

            if (searchTopic.getValue().equals(searchTopicStrings[0]))   //cerco per Trend
                controller.setLastSearchmode(0);
            else if (searchTopic.getValue().equals(searchTopicStrings[1]))    //cerco per Posizione
                controller.setLastSearchmode(1);
            else if (searchTopic.getValue().equals(searchTopicStrings[2]))    //cerco per Username
                controller.setLastSearchmode(2);

            // render degli elementi chiave della sezione di analisi di una ricerca
            controller.renderMap();
            controller.renderHistogram();
            controller.renderWordCloud();
            controller.renderLabels();

            analyzedStage = new Stage();
            analyzedStage.setTitle(bundle.getString(TA) + " - " +
                    bundle.getString("title_AnalyzedResultComplete"));
            analyzedStage.setScene(new Scene(layout));
            analyzedStage.setOnHidden(e -> analyzedStage = null);
            analyzedStage.setOnCloseRequest(e -> WordCloudManagement.deleteWordCloud());
            analyzedStage.setMinHeight(600);
            analyzedStage.setMinWidth(800);
            analyzedStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource(CSSPATH)).toExternalForm());
            analyzedStage.show();
            layout.requestFocus();
            controller.setAnalyzedStage(analyzedStage);
        } else if (analyzedStage.isShowing()) {
            analyzedStage.toFront();
        } else {
            analyzedStage.show();
        }
    }

    /**
     * Apre l'interfaccia relativa alla cronologia
     *
     * @throws IOException
     */
    @FXML
    void openHistory() throws IOException {
        if (historyStage == null) {
            FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                    .getResource("/fxml/History.fxml")), bundle);
            Parent layout = (Parent) fxmlLoader.load();
            History controller = fxmlLoader.<History>getController();

            historyStage = new Stage();
            historyStage.setTitle(bundle.getString(TA) + " - " +
                    bundle.getString("title_History"));
            historyStage.setScene(new Scene(layout));
            historyStage.setMinHeight(500.0);
            historyStage.setMinWidth(600.0);
            historyStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource(CSSPATH)).toExternalForm());
            historyStage.setOnHidden(e -> historyStage = null);
            historyStage.show();

            controller.setLandingStage(landingStage);
            controller.setAnalyzedStage(analyzedStage);
            controller.setHistoryStage(historyStage);
            layout.requestFocus();
        } else if (historyStage.isShowing()) {
            historyStage.toFront();
        } else {
            historyStage.show();
        }
    }

    /**
     * Apre l'interfaccia relativa alle impostazioni
     *
     * @throws IOException
     */
    @FXML
    void openSettings() throws IOException {
        if (settingsStage == null) {
            FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                    .getResource("/fxml/Settings.fxml")), bundle);
            Parent layout = (Parent) fxmlLoader.load();
            Settings controller = fxmlLoader.<Settings>getController();
            controller.setLandingStage(landingStage);
            controller.setHistoryStage(historyStage);
            controller.setAnalyzedStage(analyzedStage);

            settingsStage = new Stage();
            settingsStage.setTitle(bundle.getString(TA) + " - " +
                    bundle.getString("title_Settings"));
            settingsStage.setScene(new Scene(layout));

            settingsStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource(CSSPATH)).toExternalForm());
            settingsStage.setResizable(false);
            settingsStage.setOnHidden(e -> settingsStage = null);
            settingsStage.show();
            layout.requestFocus();
        } else if (settingsStage.isShowing()) {
            settingsStage.toFront();
        } else {
            settingsStage.show();
        }
    }

    /**
     * Ripristina lo stato di default dell'interfaccia principale
     */
    @FXML
    private void resetGUI() {
        lastSearchResult = null;
        dpStart.setValue(null);
        dpStart.getEditor().setDisable(true);
        dpStart.getEditor().setText(null);
        dpEnd.setValue(null);
        dpEnd.getEditor().setDisable(true);
        dpEnd.getEditor().setText(null);
        searchTopic.getSelectionModel().select(null);
        searchField.setText(null);
        listResults.setDisable(true);
        listResults.setItems(null);
        btnAnalyze.setDisable(true);
        cbGeo.setDisable(true);
        cbGeo.setSelected(false);
    }

    /**
     * Mostra all'utente maggiori informazioni riguardanti la ricerca per periodo temporale
     */
    @FXML
    public void showDateInfo() {
        MainApp.summonAlert(bundle.getString("alert_dateInfo"),
                Alert.AlertType.INFORMATION);
    }

    /**
     * Mostra all'utente una visualizzazione dettagliata del tweet selezionato
     */
    @FXML
    public void openDetailedTweet() {
        RestoredTweet selected = lastSearchResult.get(listResults.getSelectionModel().getSelectedIndex());
        TweetWindow.summonTweetDetailedView(selected, bundle);
    }


    // GUI & LOGIC

    /**
     * Imposta la GUI correttamente in riferimento al tipo di ricerca selezionato
     */
    @FXML
    public void adaptGui() {
        if (getSearchTopic().getValue() == null)
            return;

        if (getSearchTopic().getValue().equals(getSearchTopicStrings()[2])) {
            getCbGeo().setDisable(false);
            getDpStart().setDisable(true);
            getDpEnd().setDisable(true);
            getLbl1().setDisable(true);
            getLbl2().setDisable(true);
            getBtnDateInfo().setDisable(true);
            getDpStart().setValue(null);
            getDpStart().getEditor().setText(null);
            getDpEnd().setValue(null);
            getDpEnd().getEditor().setText(null);
        } else {
            getCbGeo().setDisable(true);
            getCbGeo().setSelected(false);
            getDpStart().setDisable(false);
            getDpEnd().setDisable(false);
            getLbl1().setDisable(false);
            getLbl2().setDisable(false);
            getBtnDateInfo().setDisable(false);
        }
    }

    /**
     * Inserisce il contenuto della lista di testi dei tweet nella ListView
     *
     * @param tweets lista di testi dei tweet da inserire nella ListView
     */
    public void populateListView(Optional<List<String>> tweets) {
        if (tweets.isPresent())
            listResults.setItems(FXCollections.observableArrayList(tweets.get().stream().
                    map(e -> e.replaceAll("\\P{Print}", "")).
                    collect(Collectors.toList())));
        else try {
            listResults.getItems().clear();
        } catch (NullPointerException e) {
            LOGGER.log(Level.INFO, "Lista vuota, non cancello niente");
        }
    }

    /**
     * Aggiorno i valori dentro la ComboBox in base al ResourceBundle specificato
     *
     * @param bundle ResourceBundle di riferimento
     */
    private static void updateSearchTopicStrings(ResourceBundle bundle) {
        // stavamo aggiornando un attributo statico in un metodo non statico, questa pratica è bugs prone
        searchTopicStrings = new String[]{
                bundle.getString("type_trend"),
                bundle.getString("type_position"),
                bundle.getString("type_username")};
    }

    /**
     * Controlla le date in input e le converte in timestamp
     *
     * @param start data di inizio (può essere null)
     * @param end   data di fine (può essere null)
     * @return array di timestamp, all'indice 0 il timestamp di inizio e all'indice 1 quello di fine
     */
    private Timestamp[] checkTimestamp(LocalDate start, LocalDate end) {
        Timestamp tsStart = null;

        if (start != null)
            tsStart = Timestamp.valueOf(start.atStartOfDay());

        Timestamp tsEnd = null;
        if (end != null)
            tsEnd = Timestamp.valueOf(end.atTime(23, 59, 59));

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if ((tsStart != null && tsStart.after(now))
                || (tsEnd != null && tsStart != null && (tsStart.equals(tsEnd) || tsStart.after(tsEnd)))) {
            MainApp.summonAlert(bundle.getString("alert_dateNotFormallyGivenError"), Alert.AlertType.ERROR);
            return new Timestamp[0];
        }
        return new Timestamp[]{tsStart, tsEnd};
    }

    /**
     * Implementa la logica di primo avvio con richiesta di chiavi API
     *
     * @throws IOException
     */
    private void checkForAPI() throws IOException {
        if (!(new File(MainApp.getProgramDir() + "APIkeys.json").exists())) {
            if (apiKeysFormStage == null) {
                FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                        .getResource("/fxml/ApiKeysForm.fxml")), bundle);
                Parent layout = (Parent) fxmlLoader.load();
                ApiKeysForm controller = fxmlLoader.<ApiKeysForm>getController();

                apiKeysFormStage = new Stage();
                apiKeysFormStage.setTitle(bundle.getString(TA) + " - " +
                        bundle.getString("title_ApiKeysForm"));
                apiKeysFormStage.setScene(new Scene(layout));

                apiKeysFormStage.setOnCloseRequest(e -> {
                    if (!(new File(MainApp.getProgramDir() + "APIkeys.json").exists()))
                        MainApp.summonCloseAlert(bundle.getString("alert_mustInsertKeys"),
                                Alert.AlertType.ERROR);
                });

                apiKeysFormStage.setResizable(false);
                apiKeysFormStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource(CSSPATH)).toExternalForm());
                apiKeysFormStage.show();
                controller.setStage(apiKeysFormStage);
                layout.requestFocus();
                apiKeysFormStage.setAlwaysOnTop(true);
            } else if (apiKeysFormStage.isShowing()) {
                apiKeysFormStage.toFront();
            } else {
                apiKeysFormStage.show();
            }
        }
    }

}

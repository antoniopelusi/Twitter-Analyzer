package org.g3_dev;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.g3_dev.management.RestoredSearch;
import org.g3_dev.management.RestoredTweet;
import org.g3_dev.management.SqliteManagement;
import org.g3_dev.management.TwitterManagement;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller dell'interfaccia per la visualizzazione e gestione della cronologia delle ricerche.
 */
public class History {
    private ResourceBundle bundle;
    private SqliteManagement database;
    private List<RestoredSearch> historyList;
    private Stage historyStage;
    private Stage landingStage;
    private Stage analyzedStage;

    @FXML
    public ListView<String> listViewHistory;



    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
        database = new SqliteManagement();
        historyList = database.restoreHistory();
        listViewHistory.setItems(FXCollections.observableArrayList(this.formatSearchesToString(historyList)));
    }


    // REPERIMENTO STAGE

    public void setHistoryStage(Stage historyStage) {
        this.historyStage = historyStage;
    }

    public void setLandingStage(Stage landingStage) {
        this.landingStage = landingStage;
    }

    public void setAnalyzedStage(Stage analyzedStage) {
        this.analyzedStage = analyzedStage;
    }


    // AZIONI DA BUTTON

    /**
     * Cancella tutta la cronologia
     */
    @FXML
    void deleteHistory() {
        MainApp.summonConfirmAlert(bundle.getString("alert_confirmDelete"),
                bundle.getString("alert_areYouSureDelete"),
                () -> {
                    for (RestoredSearch s : historyList)
                        database.deleteSearch(s);

                    historyStage.close();
                    MainApp.summonAlert(bundle.getString("alert_delHistoryCompleted"), Alert.AlertType.CONFIRMATION);
                },
                () -> MainApp.summonAlert(bundle.getString("alert_delHistoryCancelled"), Alert.AlertType.INFORMATION));
    }

    /**
     * Cancella una specifica ricerca dalla cronologia
     */
    @FXML
    void deleteSearch() {
        RestoredSearch selected = historyList.get(listViewHistory.getSelectionModel().getSelectedIndex());
        database.deleteSearch(selected);
        historyList.remove(listViewHistory.getSelectionModel().getSelectedIndex());
        listViewHistory.setItems(FXCollections.observableArrayList(this.formatSearchesToString(historyList)));
        MainApp.summonAlert(bundle.getString("alert_searchDelCompleted"), Alert.AlertType.INFORMATION);
    }

    /**
     * Ripristina una specifica ricerca dalla cronologia
     * @throws IOException
     */
    @FXML
    void revisitSearch() throws IOException {
        RestoredSearch selected = historyList.get(listViewHistory.getSelectionModel().getSelectedIndex());
        List<RestoredTweet> resultingTweets = database.restoreTweetsFromRestoredSearch(selected);
        if (landingStage != null) {
            landingStage.close();

            // aggiorno il ResourceBundle al Locale attuale
            bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());

            // avvio nuovamente la landing page
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                    .getResource("/fxml/LandingPage.fxml")), bundle);
            Parent root = (Parent) fxmlLoader.load();
            LandingPage controller = fxmlLoader.<LandingPage>getController();

            controller.setLandingStage(stage);

            Calendar cal = Calendar.getInstance();

            cal.setTime(selected.getTsS());
            LocalDate start = LocalDate.of(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DAY_OF_MONTH));
            if (!start.equals(LocalDate.of(1970, 1, 1)))
                controller.getDpStart().setValue(start);

            cal.setTime(selected.getTsE());
            LocalDate end = LocalDate.of(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal.get(Calendar.DAY_OF_MONTH));
            if (!end.equals(LocalDate.of(1970, 1, 1)))
                controller.getDpEnd().setValue(end);

            controller.getSearchTopic().getSelectionModel().select(selected.getTipo());
            controller.getSearchField().setText(selected.getQuery());
            controller.getCbGeo().setSelected(selected.isGeo());
            controller.setLastSearchResult(resultingTweets);
            controller.populateListView(TwitterManagement.formatTweetsToBeDisplayed(resultingTweets));
            controller.getBtnAnalyze().setDisable(false);
            controller.getListResults().setDisable(false);
            controller.adaptGui();

            stage.setTitle(bundle.getString("title_Application"));
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("/styleCSS/application.css")).toExternalForm());
            stage.setMinHeight(500.0);
            stage.setMinWidth(550.0);
            stage.show();
            root.requestFocus();
        }
        if (analyzedStage != null)
            analyzedStage.close();
        if (historyStage != null)
            historyStage.close();

    }


    // METODI INTERNI

    /**
     * Formatta le ricerche per renderle correttamente visualizzabili all'interno della ListView
     * @param restoredSearch lista di ricerche da formattare
     * @return lista di String da inserire nella ListView
     */
    private List<String> formatSearchesToString(List<RestoredSearch> restoredSearch) {
        return restoredSearch.
                stream().
                map(rs -> "[" + TwitterManagement.convertFromDateToString(rs.getTimestamp()) + " " +
                        TwitterManagement.convertFromDateToStringTime(rs.getTimestamp()) +
                        "] : " + rs.getQuery() + " - " + TwitterManagement.numToStrTipoRicerca(rs.getTipo()) +
                        " - nr. " + rs.getNumberOfResults()).
                collect(Collectors.toList());
    }

}


package org.g3_dev;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.g3_dev.management.SqliteManagement;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controller dell'interfaccia per le impostazioni.
 */
public class Settings {
    private ResourceBundle bundle;
    private String[] tweetLanguagesLabels;
    private String[] languagesLabels;
    private SqliteManagement database;

    private Stage landingStage;
    private Stage analyzedStage;
    private Stage historyStage;

    @FXML
    private ComboBox<String> cbLang;

    @FXML
    private Button btnSaveSettings;

    @FXML
    private Spinner<Integer> spnTweetLimit;

    @FXML
    private ComboBox<String> cbTweetsLang;


    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
        tweetLanguagesLabels = new String[]{
                bundle.getString("txt_all"),
                bundle.getString("txt_ita"),
                bundle.getString("txt_eng")
        };
        languagesLabels = new String[]{
                bundle.getString("txt_ita"),
                bundle.getString("txt_eng")
        };

        database = new SqliteManagement();
        int actualTweetLimit = database.getLimiteTweet();
        String actualTweetsLang = database.getLinguaTweet();
        String actualApplicationLang = database.getLingua();

        // setup combobox
        cbTweetsLang.getItems().addAll(tweetLanguagesLabels);
        if (actualTweetsLang.equals("**"))
            cbTweetsLang.getSelectionModel().select(0);
        if (actualTweetsLang.equals("it"))
            cbTweetsLang.getSelectionModel().select(1);
        if (actualTweetsLang.equals("en"))
            cbTweetsLang.getSelectionModel().select(2);

        cbLang.getItems().addAll(languagesLabels);
        if (actualApplicationLang.equals("it"))
            cbLang.getSelectionModel().select(0);
        if (actualApplicationLang.equals("en"))
            cbLang.getSelectionModel().select(1);

        // setup spinner
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, actualTweetLimit);
        spnTweetLimit.setValueFactory(valueFactory);
    }


    // REPERIMENTO STAGE

    public void setLandingStage(Stage landingStage) {
        this.landingStage = landingStage;
    }

    public void setAnalyzedStage(Stage analyzedStage) {
        this.analyzedStage = analyzedStage;
    }

    public void setHistoryStage(Stage historyStage) {
        this.historyStage = historyStage;
    }


    // AZIONI DA BUTTON

    /**
     * Salva le impostazioni modificate
     *
     * @throws IOException
     */
    @FXML
    public void saveSettings() throws IOException {
        if (cbTweetsLang.getValue().equals(tweetLanguagesLabels[0]))
            database.setLinguaTweet("**");
        if (cbTweetsLang.getValue().equals(tweetLanguagesLabels[1]))
            database.setLinguaTweet("it");
        if (cbTweetsLang.getValue().equals(tweetLanguagesLabels[2]))
            database.setLinguaTweet("en");

        String actLang = database.getLingua();
        if (actLang.equals("it"))
            actLang = languagesLabels[0];
        if (actLang.equals("en"))
            actLang = languagesLabels[1];

        if (cbLang.getValue().equals(languagesLabels[0]) && !cbLang.getValue().equals(actLang)) {
            database.setLingua("it");
            Locale.setDefault(new Locale("it"));
            this.rebootChangeLang(landingStage, analyzedStage, historyStage);
        }
        if (cbLang.getValue().equals(languagesLabels[1]) && !cbLang.getValue().equals(actLang)) {
            database.setLingua("en");
            Locale.setDefault(new Locale("en"));
            this.rebootChangeLang(landingStage, analyzedStage, historyStage);
        }

        Locale.setDefault(new Locale(database.getLingua()));

        database.setLimiteTweet(spnTweetLimit.getValue());
        Stage stage = (Stage) btnSaveSettings.getScene().getWindow();
        stage.close();
    }

    /**
     * Apre l'interfaccia dedicata alla modifica e visualizzazione della api keys
     *
     * @throws IOException
     */
    @FXML
    public void openChangeKeys() throws IOException {
        Stage apiKeysFormStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                .getResource("/fxml/ApiKeysForm.fxml")), bundle);
        Parent layout = (Parent) fxmlLoader.load();
        ApiKeysForm controller = fxmlLoader.<ApiKeysForm>getController();

        apiKeysFormStage.setTitle(bundle.getString("title_Application") + " - " +
                bundle.getString("title_ApiKeysForm"));
        apiKeysFormStage.setScene(new Scene(layout));

        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(MainApp.getProgramDir() + "APIkeys.json")) {
            // leggo dal file JSON
            JSONObject credentials = (JSONObject) parser.parse(reader);
            controller.setTxtApiKeyText(credentials.get("apiKey").toString());
            controller.setTxtApiSecretKeyText(credentials.get("apiSecretKey").toString());
            controller.setTxtAccessTokenText(credentials.get("accessToken").toString());
            controller.setTxtAccessTokenSecretText(credentials.get("accessTokenSecret").toString());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        apiKeysFormStage.setMaxHeight(275);
        apiKeysFormStage.setMinHeight(275);
        apiKeysFormStage.setMinWidth(600);
        apiKeysFormStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/styleCSS/application.css")).toExternalForm());
        apiKeysFormStage.show();
        controller.setStage(apiKeysFormStage);
        layout.requestFocus();
        apiKeysFormStage.setAlwaysOnTop(true);
    }


    // GUI & LOGIC

    /**
     * Riavvia l'interfaccia al fine di ricaricare i ResourceBundle per la funzionalità multilingua
     *
     * @param landingStage  stage principale
     * @param analyzedStage stage di analisi
     * @param historyStage  stage di cronologia
     * @throws IOException
     */
    private void rebootChangeLang(Stage landingStage, Stage analyzedStage, Stage historyStage) throws IOException {
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
            stage.setTitle(bundle.getString("title_Application"));
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                    .getResource("/styleCSS/application.css")).toExternalForm());
            stage.setMinHeight(500.0);
            stage.setMinWidth(550.0);
            stage.setOnCloseRequest(e -> System.exit(0));
            stage.show();
            root.requestFocus();
        }
        if (analyzedStage != null)
            analyzedStage.close();
        if (historyStage != null)
            historyStage.close();
    }
}

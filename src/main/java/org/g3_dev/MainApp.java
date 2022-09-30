package org.g3_dev;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.g3_dev.management.AlertPopularTrend;
import org.g3_dev.management.SqliteManagement;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principale di avvio del programma
 */
public class MainApp extends Application {
    private static final String PROGRAM_DIR =
            System.getProperty("user.home") + File.separator + "AnalizzatoreTweet" + File.separator;
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final String TWAN = "g3_dev_twitter_analyzer";
    private static final String CSSAPP = "/styleCSS/application.css";


    // METODI PER L'AVVIO

    @Override
    public void start(Stage primaryStage) throws Exception {
        //controllo della directory principale del programma
        File mainProgramDir = new File(PROGRAM_DIR);
        if (!mainProgramDir.exists() && mainProgramDir.mkdir())
            LOGGER.log(Level.INFO, () -> "\"Directory del programma creata!\\n\\tPosizone: \"" + PROGRAM_DIR);

        //inizializzazione DB
        SqliteManagement database = new SqliteManagement();
        database.createDB();

        Locale.setDefault(new Locale(database.getLingua()));
        ResourceBundle bundle = ResourceBundle.getBundle(TWAN, Locale.getDefault());

        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                .getResource("/fxml/LandingPage.fxml")), bundle);
        Parent root = (Parent) fxmlLoader.load();
        LandingPage controller = fxmlLoader.<LandingPage>getController();
        controller.setLandingStage(primaryStage);

        primaryStage.setTitle(bundle.getString("title_Application"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource(CSSAPP)).toExternalForm());
        primaryStage.setMinHeight(500.0);
        primaryStage.setMinWidth(550.0);
        primaryStage.show();
        root.requestFocus();

        if (new File(MainApp.getProgramDir() + "APIkeys.json").exists()) {
            AlertPopularTrend apt = new AlertPopularTrend();
            apt.start();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    // METODI STATICI

    /**
     * Fornisce la directory locale del programma
     *
     * @return path della directory locale del programma
     */
    public static String getProgramDir() {
        return PROGRAM_DIR;
    }

    /**
     * Mostra un alert personalizzato
     *
     * @param message testo dell'alert
     * @param type    tipologia dell'alert
     */
    public static void summonAlert(String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert error = new Alert(type, message);
            DialogPane dialogPane = error.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(MainApp.class
                    .getResource(CSSAPP)).toExternalForm());
            dialogPane.setMinHeight(Region.USE_PREF_SIZE);
            Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
            okBtn.setId("ok-btn");
            error.show();
        });
    }

    /**
     * Mostra un alert personalizzato con caratteristiche funzionalità di conferma e annullamento
     *
     * @param title   titolo dell'alert
     * @param message testo dell'alert
     * @param yes     funzione lambda nel caso si confermi l'alert
     * @param cancel  funzione lambda nel caso si annulli l'alert
     */
    public static void summonConfirmAlert(String title, String message, Lambda yes, Lambda cancel) {
        ResourceBundle bundle = ResourceBundle.getBundle(TWAN, Locale.getDefault());
        summonConfirmAlert(title, message, yes, cancel, "Ok", bundle.getString("btn_cancel"));
    }

    /**
     * Mostra un alert personalizzato con caratteristiche funzionalità di conferma e annullamento
     *
     * @param title   titolo dell'alert
     * @param message testo dell'alert
     * @param yes     funzione lambda nel caso si confermi l'alert
     * @param cancel  funzione lambda nel caso si annulli l'alert
     * @param ts      testo personalizzato Button Ok
     * @param fs      testo personalizzato Button Cancel
     */
    public static void summonConfirmAlert(String title, String message, Lambda yes, Lambda cancel, String ts, String fs) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(MainApp.class
                    .getResource(CSSAPP)).toExternalForm());
            dialogPane.setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle(title);
            alert.setContentText(message);
            ButtonType okButton = new ButtonType(ts, ButtonBar.ButtonData.YES);
            ButtonType cancelButton = new ButtonType(fs, ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(okButton, cancelButton);
            alert.showAndWait().ifPresent(type -> {
                if (type.getButtonData() == ButtonBar.ButtonData.YES) {
                    yes.execute();
                } else {
                    cancel.execute();
                }
            });
        });
    }

    /**
     * Mostra un alert personalizzato che provoca la chiusura del programma
     *
     * @param message testo dell'alert
     * @param type    tipologia dell'alert
     */
    public static void summonCloseAlert(String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            ResourceBundle bundle = ResourceBundle.getBundle(TWAN, Locale.getDefault());
            Alert alert = new Alert(type);
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(MainApp.class
                    .getResource(CSSAPP)).toExternalForm());
            dialogPane.setMinHeight(Region.USE_PREF_SIZE);
            alert.setContentText(message);
            ButtonType okButton = new ButtonType(bundle.getString("btn_exit"), ButtonBar.ButtonData.YES);
            alert.getButtonTypes().setAll(okButton);
            alert.showAndWait().ifPresent(t -> System.exit(0));
        });
    }

    /**
     * Ritorna il formattatore di valori decimali standard all'interno del programma
     *
     * @return formattatore di valori decimali standard
     */
    public static DecimalFormat getDecimalFormat() {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df;
    }

}
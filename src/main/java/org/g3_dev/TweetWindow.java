package org.g3_dev;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.g3_dev.management.RestoredTweet;
import org.g3_dev.management.TwitterManagement;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller dell'interfaccia per la visualizzazione di un tweet.
 */
public class TweetWindow {
    private static final Logger LOGGER = Logger.getLogger(TweetWindow.class.getName());

    @FXML
    public ImageView imgUserProPic;

    @FXML
    public Label lblUsername;

    @FXML
    public Label lblDate;

    @FXML
    public Label lblPosto;

    @FXML
    public TextArea txtaTweetContent;


    // GUI & LOGIC

    /**
     * Imposta la scena con i valori del tweet passati come parametro
     *
     * @param imageUrl     URL dell'immagine di profilo dell'utente autore del tweet
     * @param username     nome utente dell'autore del tweet
     * @param date         timestamp di pubblicazione del tweet
     * @param posto        nome testuale del posto di pubblicazione del tweet
     * @param tweetContent testo del tweet
     */
    public void setUpTweetScene(String imageUrl, String username, Timestamp date, String posto, String tweetContent) {
        this.imgUserProPic.setImage(new Image(imageUrl));
        this.imgUserProPic.setFitHeight(69);
        this.imgUserProPic.setFitWidth(69);
        this.lblUsername.setText(username);
        this.lblUsername.setUnderline(true);
        this.lblDate.setText(TwitterManagement.convertFromDateToString(date) + " " +
                TwitterManagement.convertFromDateToStringTime(date));
        this.lblPosto.setText(posto);
        this.txtaTweetContent.setText(tweetContent);
        this.txtaTweetContent.setEditable(false);
    }

    /**
     * Mostra la visualizzazione di un tweet in una interfaccia dettagliata
     *
     * @param tweet  tweet da visualizzare
     * @param bundle ResourceBundle lingua da adoperare
     */
    public static void summonTweetDetailedView(RestoredTweet tweet, ResourceBundle bundle) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                        .getResource("/fxml/TweetWindow.fxml")), bundle);
                Parent layout = null;
                try {
                    layout = fxmlLoader.load();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Non sono riuscito a caricare il file /fxml/TweetWindow.fxml");
                }
                TweetWindow controller = fxmlLoader.getController();
                controller.setUpTweetScene(
                        tweet.getUserProPic(),
                        tweet.getUserName(),
                        tweet.getTimestamp(),
                        tweet.getPosto(),
                        tweet.getTesto());
                Stage tweetStage = new Stage();
                tweetStage.setTitle(bundle.getString("title_Application") + " - Tweet");
                assert layout != null;
                tweetStage.setScene(new Scene(layout));
                tweetStage.setMinHeight(150);
                tweetStage.setMinWidth(500);
                tweetStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                        .getResource("/styleCSS/application.css")).toExternalForm());
                tweetStage.show();
                Objects.requireNonNull(layout);
                layout.requestFocus();
            }
        });
    }

}

package org.g3_dev;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.g3_dev.management.TwitterManagement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller dell'interfaccia per la condivisione delle analisi di una ricerca.
 */
public class Share {
    private ResourceBundle bundle;
    private List<String> images;
    private Stage stage;
    private Stage shareStage;
    private static final Logger LOGGER = Logger.getLogger(Share.class.getName());

    @FXML
    private BorderPane paneImg1;

    @FXML
    private BorderPane paneImg2;

    @FXML
    private BorderPane paneImg3;

    @FXML
    private TextArea txtTweetBody;


    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
    }


    // REPERIMENTO STAGE

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setShareStage(Stage shareStage) {
        this.shareStage = shareStage;
    }


    // AZIONI DA BUTTON

    /**
     * Avvia la condivisione del tweet di statistica
     */
    @FXML
    void shareOnTwitter() {
        new TwitterManagement().postTweetWithImage(this.images, this.txtTweetBody.getText());
        MainApp.summonAlert(bundle.getString("alert_tweetShared"), Alert.AlertType.CONFIRMATION);
        for (String s : images) {
            try {
                Files.delete(Path.of(s));
                LOGGER.log(Level.INFO, () -> "Cancellata immagine condivisa: " + s);
            } catch (IOException e) {
                LOGGER.log(Level.INFO, () -> "Impossibile cancellare immagine condivisa: " + s);
            }
        }
        if (stage != null)
            stage.close();
        if (shareStage != null)
            shareStage.close();
    }


    // LOGIC

    /**
     * Imposta le immagini degli screenshot nella GUI
     *
     * @param images lista dei nomi dei file contenenti le immagini da mostrare
     * @throws IOException nel caso i file non vengano trovati
     */
    public void setImages(List<String> images) throws IOException {
        this.images = images;
        BorderPane[] bp = new BorderPane[]{paneImg1, paneImg2, paneImg3};
        for (int i = 0; i < images.size(); i++) {
            File file = new File(images.get(i));
            if (file.exists()) {
                ImageView img = new ImageView(new Image(file.toURI().toString()));
                img.fitHeightProperty();
                img.setFitHeight(207);
                img.setFitWidth(300);
                bp[i].setCenter(img);
            }
        }
    }
}


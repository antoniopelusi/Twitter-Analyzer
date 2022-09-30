package org.g3_dev;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller dell'interfaccia per l'inserimento delle chiavi API.
 */
public class ApiKeysForm {
    private Stage stage;
    private static final Logger LOGGER = Logger.getLogger(ApiKeysForm.class.getName());

    @FXML
    private TextField txtApiKey;

    @FXML
    private TextField txtApiSecretKey;

    @FXML
    private TextField txtAccessToken;

    @FXML
    private TextField txtAccessTokenSecret;


    // METODI SETTER

    public void setTxtApiKeyText(String txtApiKey) {
        this.txtApiKey.setText(txtApiKey);
    }

    public void setTxtApiSecretKeyText(String txtApiSecretKey) {
        this.txtApiSecretKey.setText(txtApiSecretKey);
    }

    public void setTxtAccessTokenText(String txtAccessToken) {
        this.txtAccessToken.setText(txtAccessToken);
    }

    public void setTxtAccessTokenSecretText(String txtAccessTokenSecret) {
        this.txtAccessTokenSecret.setText(txtAccessTokenSecret);
    }


    // REPRIMENTO STAGE

    public void setStage(Stage stage) {
        this.stage = stage;
    }


    // AZIONI DA BUTTON

    /**
     * Salva le chiavi inserite nel file json nella directory locale del programma
     */
    @FXML
    void saveAPIkeys() {
        ResourceBundle bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
        if (txtApiKey.getText() != null
                && txtApiSecretKey.getText() != null
                && txtAccessToken.getText() != null
                && txtAccessTokenSecret.getText() != null) {
            JSONObject obj = new JSONObject();
            obj.put("apiKey", txtApiKey.getText().trim());
            obj.put("apiSecretKey", txtApiSecretKey.getText().trim());
            obj.put("accessToken", txtAccessToken.getText().trim());
            obj.put("accessTokenSecret", txtAccessTokenSecret.getText().trim());

            try (FileWriter file = new FileWriter(MainApp.getProgramDir() + "APIkeys.json")) {
                file.write(obj.toJSONString());
                file.flush();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, () -> "Non riesco a scrivere su: " + MainApp.getProgramDir() + "APIkeys.json");
            }
            MainApp.summonAlert(bundle.getString("alert_keysSaveCorrect"), Alert.AlertType.CONFIRMATION);
            stage.close();
        } else
            MainApp.summonAlert(bundle.getString("alert_keysMissing"), Alert.AlertType.ERROR);
    }
}

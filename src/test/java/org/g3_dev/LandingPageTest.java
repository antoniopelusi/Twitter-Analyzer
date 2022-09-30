package org.g3_dev;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.robot.Motion;
import org.testfx.service.query.NodeQuery;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class LandingPageTest {
    private LandingPage lp;
    private Stage primaryStage;


    @Start
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        // imposto il locale a it
        Locale.setDefault(new Locale("it"));
        ResourceBundle bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());

        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass()
                .getResource("/fxml/LandingPage.fxml")), bundle);
        Parent root = fxmlLoader.load();
        lp = fxmlLoader.<LandingPage>getController();
        lp.setLandingStage(stage);

        stage.setTitle(bundle.getString("title_Application"));
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest((e) -> System.exit(0));
        stage.getScene().getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/styleCSS/application.css")).toExternalForm());
        stage.setMinHeight(500.0);
        stage.setMinWidth(550.0);
        stage.show();
        root.requestFocus();
    }

    @Test
    @DisplayName("Testing del setup iniziale della GUI (in lingua Italiana)")
    public void firstSetupGUI(FxRobot robot) {
        // richiamo esplicitamente l'inizializzazione
        robot.clickOn("#searchTopic");

        assertNull(lp.getSearchField().getText());
        assertTrue(lp.getSearchTopic().getItems().size() > 0);
        List<String> idStrings = lp.getSearchTopic().getItems();
        assertThat(idStrings).containsExactly(
                "Trend",
                "Posizione",
                "Username"
        );
        assertFalse(lp.getCbGeo().isSelected());
        assertFalse(lp.getDpEnd().isDisabled());
        assertFalse(lp.getDpStart().isDisabled());
        FxAssert.verifyThat("#btnSearch", LabeledMatchers.hasText("Cerca"));
        FxAssert.verifyThat("#btnClear", LabeledMatchers.hasText("Cancella"));
        FxAssert.verifyThat("#btnHistory", LabeledMatchers.hasText("Cronologia ricerche"));
        FxAssert.verifyThat("#btnAnalyze", LabeledMatchers.hasText("Analizza"));
        FxAssert.verifyThat("#lbl1", LabeledMatchers.hasText("da"));
        FxAssert.verifyThat("#lbl2", LabeledMatchers.hasText("a"));
    }

    @Test
    @DisplayName("Testing del metodo checkTimestamp")
    public void getNumberOfGeoTweetsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // recupero il metodo altrimenti privato
        Method method = LandingPage.class.getDeclaredMethod("checkTimestamp", LocalDate.class, LocalDate.class);
        method.setAccessible(true);

        LocalDate start = LocalDate.of(2021, 05, 28);
        LocalDate end = LocalDate.of(2021, 05, 29);

        // richiamo il metodo
        Timestamp[] output = (Timestamp[]) method.invoke(lp, start, end);

        assertEquals(Timestamp.valueOf("2021-05-28 00:00:00.0"), output[0]);
        assertEquals(Timestamp.valueOf("2021-05-29 23:59:59.0"), output[1]);
    }

}
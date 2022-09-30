package org.g3_dev.management;

import org.g3_dev.GenerateMockData;
import org.g3_dev.LandingPage;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

public class TwitterManagementTest {
    private static ArrayList<RestoredSearch> rs;
    private static ArrayList<RestoredTweet> mo;
    private static TwitterManagement tm;
    private static int limiteTweet = 6;

    @BeforeEach
    public void setUp() {
        rs = GenerateMockData.generateSearches();
        mo = new GenerateMockObjects().generateTweets();
        tm = new TwitterManagement();
    }

    @Test
    @DisplayName("Controllo rimozione tweet non geolocalizzati")
    public void removeNotGeolocatedTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Optional<ArrayList<RestoredTweet>> art = Optional.of(mo);
        Method method = TwitterManagement.class.getDeclaredMethod("removeNotGeolocated", Optional.class);
        method.setAccessible(true);
        Optional<ArrayList<RestoredTweet>> output = (Optional<ArrayList<RestoredTweet>>) method.invoke(tm, art);

        if (output.isPresent()) {
            ArrayList<RestoredTweet> toIterate = output.get();
            for (RestoredTweet t : toIterate) {
                assertNotNull(t.getLatitudine());
                assertNotNull(t.getLongitudine());
                assertNotEquals("", t.getPosto());
            }
        }
    }

    @Test
    @DisplayName("Controllo riduzione numero risultati")
    public void trimResultsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Optional<ArrayList<RestoredTweet>> ort = Optional.of(mo);
        Method method = TwitterManagement.class.getDeclaredMethod("trimResults", Optional.class, int.class);
        method.setAccessible(true);
        Optional<ArrayList<RestoredTweet>> output = (Optional<ArrayList<RestoredTweet>>) method.invoke(tm, ort, limiteTweet);

        if (output.isPresent()) {
            ArrayList<RestoredTweet> toCount = output.get();
            assertTrue(toCount.size() <= limiteTweet);
        }
    }

    @Test
    @DisplayName("Controllo rimozione tweet con date non inerenti")
    public void removeNotInTimeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Optional<ArrayList<RestoredTweet>> ort = Optional.of(mo);
        Timestamp tsS = Timestamp.valueOf("2021-04-11 00:00:00");
        Timestamp tsE = Timestamp.valueOf("2021-04-30 23:59:59");
        Method method = TwitterManagement.class.getDeclaredMethod("removeNotInTime", Optional.class, Timestamp.class, Timestamp.class);
        method.setAccessible(true);
        Optional<ArrayList<RestoredTweet>> output = (Optional<ArrayList<RestoredTweet>>) method.invoke(tm, ort, tsS, tsE);

        assertEquals(6, output.get().size());
    }

    @Test
    @DisplayName("Controllo restituzione stringhe")
    public void formatTweetsToBeDisplayedTest() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ArrayList<RestoredTweet> art = new ArrayList<>();
        Method method = TwitterManagement.class.getDeclaredMethod("formatTweetsToBeDisplayed", List.class);
        method.setAccessible(true);
        Optional<List<String>> output = (Optional<List<String>>) method.invoke(tm, mo);

        if(output.isPresent()){
            List<String> ls = output.get();
            for (String s : ls){
                assertEquals(String.class, s.getClass());
            }
        }
    }

    @Test
    @DisplayName("Formula di Haversine")
    public void haversineTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = TwitterManagement.class.getDeclaredMethod("haversine", Double.class, Double.class, Double.class, Double.class);
        method.setAccessible(true);
        Double ret = (Double)method.invoke(tm, 41.9109, 12.4818, 43.7874, 11.2499);
        assertEquals(231.5600867559484, ret);
    }

    @Test
    @DisplayName("Controllo conversione da Date a String (calendario)")
    public void convertFromDateToStringTest() {
        Date d = new Date(121, Calendar.APRIL, 1);
        String output = TwitterManagement.convertFromDateToString(d);
        assertEquals("01/04/2021", output);
    }

    @Test
    @DisplayName("Controllo conversione da Date a String (orario)")
    public void convertFromDateToStringTimeTest(){
        Date d = new Date(121, Calendar.APRIL, 1, 12,34);
        String output = TwitterManagement.convertFromDateToStringTime(d);
        assertEquals("12:34", output);
    }

    @Test
    @DisplayName("Controllo conversione da numero a tipo di ricerca")
    public void numToStrTipoRicercaTest(){
        assertEquals("Trend", tm.numToStrTipoRicerca(0));
        assertEquals("Posizione", tm.numToStrTipoRicerca(1));
        assertEquals("Username", tm.numToStrTipoRicerca(2));
    }

    @AfterAll
    public static void reset() {

    }
}

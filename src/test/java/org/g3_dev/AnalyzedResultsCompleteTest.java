package org.g3_dev;

import org.g3_dev.management.GenerateMockObjects;
import org.g3_dev.management.RestoredSearch;
import org.g3_dev.management.RestoredTweet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzedResultsCompleteTest {
    private ArrayList<RestoredSearch> mockRS;
    private ArrayList<RestoredTweet> mockRT;
    private AnalyzedResultsComplete arc;
    private int limiteTweet = 6;

    @BeforeEach
    public void setUp() {
        mockRS = GenerateMockData.generateSearches();
        mockRT = new GenerateMockObjects().generateTweets();
        arc = new AnalyzedResultsComplete();
    }

    @Test
    @DisplayName("Testing del metodo getNumberOfGeoTweets")
    public void getNumberOfGeoTweetsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // recupero il metodo altrimenti privato
        Method method = AnalyzedResultsComplete.class.getDeclaredMethod("getNumberOfGeoTweets", ArrayList.class);
        method.setAccessible(true);

        // richiamo il metodo
        int output = (int) method.invoke(arc, mockRT);

        assertEquals(8, output);
    }

    @Test
    @DisplayName("Testing del metodo getGeoTweetPerc")
    public void getGeoTweetPercTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // recupero il metodo altrimenti privato
        Method method = AnalyzedResultsComplete.class.getDeclaredMethod("getGeoTweetPerc", ArrayList.class);
        method.setAccessible(true);

        // richiamo il metodo
        Double output = (Double) method.invoke(arc, mockRT);

        assertEquals(80.0, output);
    }

    @Test
    @DisplayName("Testing del metodo getTweetPerHourAvg")
    public void getTweetPerHourAvgTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // recupero il metodo altrimenti privato
        Method method = AnalyzedResultsComplete.class.getDeclaredMethod("getTweetPerHourAvg", ArrayList.class);
        method.setAccessible(true);

        // richiamo il metodo
        Double output = (Double) method.invoke(arc, mockRT);

        assertEquals(10.0 / 24.0, output);
    }

    @Test
    @DisplayName("Testing del metodo countTweetsWithImage")
    public void countTweetsWithImageTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // recupero il metodo altrimenti privato
        Method method = AnalyzedResultsComplete.class.getDeclaredMethod("countTweetsWithImage", ArrayList.class);
        method.setAccessible(true);

        // richiamo il metodo
        int output = (int) method.invoke(arc, mockRT);

        assertEquals(5 , output);
    }

    @Test
    @DisplayName("Testing del metodo populateHourlyMap")
    public void populateHourlyMapTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // recupero il metodo altrimenti privato
        Map<String, Integer> map = new HashMap<>();
        Method method = AnalyzedResultsComplete.class.getDeclaredMethod("populateHourlyMap", Map.class, ArrayList.class);
        method.setAccessible(true);

        // richiamo il metodo
        method.invoke(arc, map, mockRT);

        assertTrue(map.containsKey("09"));
        assertTrue(map.containsKey("10"));
        assertTrue(map.containsKey("11"));
        assertTrue(map.containsKey("15"));
        assertEquals(6, map.get("09"));
        assertEquals(2, map.get("10"));
        assertEquals(1, map.get("11"));
        assertEquals(1, map.get("15"));
    }

}
package org.g3_dev;

import org.g3_dev.management.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeoDetailsTest {
    private static ArrayList<RestoredTweet> mo;
    private static GeoDetails gd;

    @BeforeEach
    public void setUp() {
        mo = new GenerateMockObjects().generateTweets();
        gd = new GeoDetails();
    }

    @Test
    void detailedGeoStats() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = GeoDetails.class.getDeclaredMethod("detailedGeoStats", List.class);
        method.setAccessible(true);
        Map<String, Map<String, List<String>>> ret = (Map<String, Map<String, List<String>>>) method.invoke(gd, mo);
        assertEquals(1, ret.keySet().size());
        assertEquals("[Italia]", ret.keySet().toString());
        assertEquals(true, ret.get("Italia").containsKey("Emilia Romagna"));
        assertEquals(2, ret.get("Italia").get("Emilia Romagna").stream().filter(e -> e.equals("Bologna")).count());
    }
}
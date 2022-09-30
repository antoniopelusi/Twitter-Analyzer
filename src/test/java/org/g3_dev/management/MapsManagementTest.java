package org.g3_dev.management;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapsManagementTest {
    private static MapsManagement mm;

    @BeforeAll
    public static void setUp() {
        mm = new MapsManagement();
    }

    @Test
    void middlePoint() {
        List<Double[]> toTest = new ArrayList<>();
        toTest.add(new Double[]{44.64783, 10.92539}); //Modena
        toTest.add(new Double[]{44.84346, 11.60868}); //Ferrara
        toTest.add(new Double[]{44.22054, 12.05245}); //Forlì
        toTest.add(new Double[]{44.79935, 10.32618}); //Parma

        Double[] ret = mm.middlePoint(toTest);
        assertEquals(44.627795, ret[0]);
        assertEquals(11.228175, ret[1]);
    }

    @Test
    void decToDms() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Modena Lat
        double testLat = 44.64783;
        int[] ret = mm.decToDms(testLat);
        assertEquals(44, ret[0]);
        assertEquals(38, ret[1]);
        assertEquals(52, ret[2]);
        // Modena Lon
        double testLon = 10.92539;
        ret = (int[]) mm.decToDms(testLon);
        assertEquals(10, ret[0]);
        assertEquals(55, ret[1]);
        assertEquals(31, ret[2]);
    }
}
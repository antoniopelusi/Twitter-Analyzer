package org.g3_dev;

import org.g3_dev.management.RestoredSearch;

import java.sql.Timestamp;
import java.util.ArrayList;

public class GenerateMockData {
    private static ArrayList<RestoredSearch> searches = new ArrayList<RestoredSearch>(30);

    public static void addRestoredSearch(RestoredSearch rs) {
        searches.add(rs);
    }

    public static ArrayList<RestoredSearch> generateSearches() {
        //String query, int geo, int tipo, Timestamp tsS, Timestamp tsE, Timestamp timestamp

        // Da testare a parte initialize() e deleteHistory() della classe History

        //1 return user geo no
        RestoredSearch toAdd = new RestoredSearch("g3_dev", 0, 2, null, null, Timestamp.valueOf("2021-05-20 07:00:00"));
        addRestoredSearch(toAdd);
        //2 return user geo sì
        toAdd = new RestoredSearch("g3_dev", 1, 2, null, null, Timestamp.valueOf("2021-05-20 08:00:00"));
        addRestoredSearch(toAdd);
        //3 return trend geo sì + getNumberOfGeoTweets() + getGeoTweetAvg()
        toAdd = new RestoredSearch("pds2021", 1, 0, null, null, Timestamp.valueOf("2021-05-20 09:00:00"));
        addRestoredSearch(toAdd);
        //4 return trend geo no + formatTweetsToBeDisplayed()
        toAdd = new RestoredSearch("pds2021", 0, 0, null, null, Timestamp.valueOf("2021-05-20 10:00:00"));
        addRestoredSearch(toAdd);
        //5 return da una certa data
        toAdd = new RestoredSearch("pds2021", 0, 0, Timestamp.valueOf("2021-04-11 10:00:00"), null, Timestamp.valueOf("2021-04-19 11:00:00"));
        addRestoredSearch(toAdd);
        //6 return fino a una certa data
        toAdd = new RestoredSearch("pds2021", 0, 0, null, Timestamp.valueOf("2021-04-30 10:00:00"), Timestamp.valueOf("2021-05-03 12:00:00"));
        addRestoredSearch(toAdd);
        //7 return posizione in un intervallo di date
        toAdd = new RestoredSearch("Modena", 1, 0, Timestamp.valueOf("2021-04-25 10:00:00"), Timestamp.valueOf("2021-04-30 13:00:00"), Timestamp.valueOf("2021-05-02 10:00:00"));
        addRestoredSearch(toAdd);
        //8 trim results + getTweetPerHourAvg()
        toAdd = new RestoredSearch("ChiaraFerragni", 0, 3, null, null, Timestamp.valueOf("2021-05-15 14:00:00"));
        addRestoredSearch(toAdd);

        return searches;
    }
}

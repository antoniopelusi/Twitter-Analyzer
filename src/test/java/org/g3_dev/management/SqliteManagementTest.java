package org.g3_dev.management;

import org.g3_dev.MainApp;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SqliteManagementTest {
    private static final Logger LOGGER = Logger.getLogger(SqliteManagementTest.class.getName());
    private SqliteManagement sql;


    @BeforeAll
    public static void runBeforeClass() {
        try {
            String fp = MainApp.getProgramDir() + "test.db";
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + fp);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.join("\n", Files.readAllLines(Paths.get("database/dump.sql"))));
            LOGGER.log(Level.INFO, () -> "Creato database test: " + fp);
            stmt.close();
            conn.close();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }


    @AfterAll
    public static void runAfterClass() {
        String fp = MainApp.getProgramDir() + "test.db";
        try {
            Files.delete(Path.of(fp));
            LOGGER.log(Level.INFO, () -> "Cancellato database test file: " + fp);
        } catch (IOException e) {
            LOGGER.log(Level.INFO, () -> "Impossibile cancellare database test file: " + fp);
        }
    }


    void getLimiteTweet() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            int value = sql.getLimiteTweet();
            assertEquals(100, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void setLimiteTweet() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            sql.setLimiteTweet(50);
            assertEquals(50, sql.getLimiteTweet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("Controllo limite dei tweet")
    void testLimiteTweet() {
        getLimiteTweet();
        setLimiteTweet();
    }


    void getLinguaTweet() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            String value = sql.getLinguaTweet();
            assertEquals("**", value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void setLinguaTweet() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            sql.setLinguaTweet("en");
            assertEquals("en", sql.getLinguaTweet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("Controllo caratteri per la lingua di ricerca tweet")
    void testLinguaTweet() {
        getLinguaTweet();
        setLinguaTweet();
    }


    void getLingua() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            String value = sql.getLingua();
            assertEquals("it", value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void setLingua() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            sql.setLingua("en");
            assertEquals("en", sql.getLingua());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("Controllo caratteri per la lingua dell'applicazione")
    void testLingua() {
        getLingua();
        setLingua();
    }


    @Test
    @DisplayName("Controllo il recupero della lista di tutti i trend cercati precedentemente in cronologia")
    void getTrendSearched() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            List<String> expected = new ArrayList<>();
            expected.add("#pds2021");
            assertEquals(expected, sql.getTrendSearched());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void restoreHistory() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            RestoredSearch e = new RestoredSearch("g3_dev", 0, 2, null, null,
                    Timestamp.valueOf("2021-05-24 23:35:28.78"));
            RestoredSearch p = new RestoredSearch("#pds2021", 0, 0, null, null,
                    Timestamp.valueOf("2021-05-24 23:39:05.56"));
            RestoredSearch r = new RestoredSearch("modena", 0, 1, null, null,
                    Timestamp.valueOf("2021-05-24 23:39:37.589"));
            List<RestoredSearch> retourned = sql.restoreHistory();
            ArrayList<Timestamp> times = new ArrayList<>();
            for(RestoredSearch rs : retourned)
                times.add(rs.getTimestamp());
            assertEquals(3, retourned.size());
            assertTrue(times.contains(e.getTimestamp()));
            assertTrue(times.contains(p.getTimestamp()));
            assertTrue(times.contains(r.getTimestamp()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void deleteSearch() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            RestoredSearch value = new RestoredSearch("g3_dev", 0, 2, null, null, Timestamp.valueOf("2021-05-24 23:35:28.78"));
            sql.deleteSearch(value);
            List<RestoredSearch> retourned = sql.restoreHistory();
            ArrayList<Timestamp> times = new ArrayList<>();
            for(RestoredSearch rs : retourned)
                times.add(rs.getTimestamp());
            assertFalse(times.contains(value.getTimestamp()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void saveSearch() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            ArrayList<RestoredTweet> arr = new ArrayList<>();
            RestoredTweet  value = new RestoredTweet(-2083102719, "g3_dev",
                    "http://pbs.twimg.com/profile_images/1381366425135239174/5h-syGdh_400x400.jpg",
                    "Sprint 0 finito! Lasciate un like e un commento! https:\\/\\/t.co\\/BK7007AbFZ #PdS2021",
                    0, 0.0, 0.0, "", "it", Timestamp.valueOf("2021-04-18 22:58:59.0"));
            arr.add(value);
            sql.saveSearch("g3_dev", false, 2, null, null, arr);
            List<RestoredSearch> retourned = sql.restoreHistory();
            for(RestoredSearch rs : retourned){
                if(rs.getQuery().equals("g3_dev")){
                    assertEquals("g3_dev" , rs.getQuery());
                    assertEquals(false, rs.isGeo());
                    assertEquals(2, rs.getTipo());
                    assertEquals(Timestamp.valueOf("1970-01-01 00:00:00.0"), rs.getTsS());
                    assertEquals(Timestamp.valueOf("1970-01-01 00:00:00.0"), rs.getTsE());
                    assertEquals(-2083102719, arr.get(0).getTweetId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void restoreTweetsFromRestoredSearch() {
        if (Objects.isNull(sql))
            sql = new SqliteManagement(MainApp.getProgramDir() + "test.db");
        try {
            List<RestoredSearch> retourned = sql.restoreHistory();
            for(RestoredSearch rs : retourned){
                if(rs.getQuery().equals("PDS")){
                    List<RestoredTweet> tweets = sql.restoreTweetsFromRestoredSearch(rs);
                    assertEquals(-2083102719, tweets.get(0).getTweetId());
                    assertEquals("PDS" , tweets.get(0).getUserName());
                    assertEquals("http://pbs.twimg.com/profile_images/1381366425135239174/5h-syGdh_400x400.jpg",
                            tweets.get(0).getUserProPic());
                    assertEquals("Sprint 0 finito! Lasciate un like e un commento! https:\\/\\/t.co\\/BK7007AbFZ #PdS2021",
                            tweets.get(0).getTesto());
                    assertEquals(false, tweets.get(0).hasImmagine());
                    assertEquals(0.0, tweets.get(0).getLatitudine());
                    assertEquals(0.0, tweets.get(0).getLongitudine());
                    assertEquals("", tweets.get(0).getPosto());
                    assertEquals("it", tweets.get(0).getLingua());
                    assertEquals(Timestamp.valueOf("2021-04-18 22:58:59.0"), tweets.get(0).getTimestamp());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    @DisplayName("Controllo funzionalità di reperimento cronologia delle ricerche e dei tweet appartenenti ad una ricerca, " +
            "salvataggio e cancellazione di una ricerca")
    void testSaveDelete(){
        restoreHistory();
        deleteSearch();
        saveSearch();
        restoreTweetsFromRestoredSearch();
    }
}
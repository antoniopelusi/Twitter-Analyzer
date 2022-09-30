package org.g3_dev.management;

import org.apache.commons.lang3.StringEscapeUtils;
import org.g3_dev.MainApp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe che gestisce le funzionalità necessarie per la gestione del Database del programma.
 */
public class SqliteManagement {
    private static Connection conn;
    private static Statement stmt;
    private static final Logger LOGGER = Logger.getLogger(SqliteManagement.class.getName());


    /**
     * Creazione della connessione nel costruttore
     */
    public SqliteManagement(String dir) {
        try {
            if (Objects.isNull(conn) || conn.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                //non rimuoviamo questo assegnamento ad attributo statico perché ci serve
                //per mantenere la connessione al db, una sorta di stato.
                conn = DriverManager.getConnection("jdbc:sqlite:" + dir);
                //non rimuoviamo questo assegnamento ad attributo statico perché ci serve
                //per mantenere la connessione al db, una sorta di stato.
                stmt = conn.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, () -> "Non riesco a creare il database file: " + dir);
        }
    }

    /**
     * Creazione della connessione nel costruttore
     */
    public SqliteManagement() {
        try {
            if (Objects.isNull(conn) || conn.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                //non rimuoviamo questo assegnamento ad attributo statico perché ci serve
                //per mantenere la connessione al db, una sorta di stato.
                conn = DriverManager.getConnection("jdbc:sqlite:" + MainApp.getProgramDir() + "g3_dev.db");
                //non rimuoviamo questo assegnamento ad attributo statico perché ci serve
                //per mantenere la connessione al db, una sorta di stato.
                stmt = conn.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, () -> "Non riesco a creare il database file: " + MainApp.getProgramDir() + "g3_dev.db");
        }
    }

    /**
     * Creazione del database
     */
    public void createDB() {
        try {
            // CREAZIONE TABELLE DB PROGRAMMA
            // IMPOSTAZIONI
            String sql = "CREATE TABLE IF NOT EXISTS IMPOSTAZIONI " +
                    "(NOME TEXT PRIMARY KEY, " +
                    "VALORE TEXT NOT NULL )";

            stmt.executeUpdate(sql);
            // RICERCA
            sql = "CREATE TABLE IF NOT EXISTS RICERCA " +
                    "(QUERY TEXT, " +
                    "GEO BIT, " +
                    "TIPO INTEGER DEFAULT 0, " +
                    "DATA_INIZIO TIMESTAMP, " +
                    "DATA_FINE TIMESTAMP, " +
                    "TIME_STAMP TIMESTAMP PRIMARY KEY)";
            stmt.executeUpdate(sql);
            // TWEET
            sql = "CREATE TABLE IF NOT EXISTS TWEET " +
                    "(TWEETID INTEGER PRIMARY KEY, " +
                    "USERNAME TEXT NOT NULL, " +
                    "USERPROPIC TEXT NOT NULL, " +
                    "TESTO TEXT NOT NULL, " +
                    "IMMAGINE BIT NOT NULL, " +
                    "GEOAVGLATITUDE REAL NOT NULL, " +
                    "GEOAVGLONGITUDE REAL NOT NULL, " +
                    "POSTO TEXT NOT NULL, " +
                    "LINGUA TEXT NOT NULL, " +
                    "TIME_STAMP TIMESTAMP NOT NULL )";
            stmt.executeUpdate(sql);
            // RELRICERCATWEET
            sql = "CREATE TABLE IF NOT EXISTS RELRICERCATWEET " +
                    "(TIME_STAMP TIMESTAMP, " +
                    "TWEETID INTEGER, " +
                    "FOREIGN KEY (TIME_STAMP) REFERENCES RICERCA(TIME_STAMP) " +
                    "ON DELETE CASCADE, " +
                    "FOREIGN KEY (TWEETID) REFERENCES TWEET(TWEETID) " +
                    "ON DELETE CASCADE, " +
                    "PRIMARY KEY (TIME_STAMP, TWEETID))";
            stmt.executeUpdate(sql);
            initSettings();
        } catch (SQLException throwables) {
            LOGGER.log(Level.SEVERE, () -> "Eccezione  SQL: " + throwables.getMessage());
        }
    }

    /**
     * Inserimento settings di default nel database
     */
    public void initSettings() {
        int defaultLimiteTweet = 20;
        String defaultLinguatweet = "it";
        String defaultLingua = "it";
        try {
            // Setup impostazioni di default del programma
            String sql = "SELECT * FROM IMPOSTAZIONI WHERE NOME='limiteTweet' LIMIT 1";
            ResultSet ret = stmt.executeQuery(sql);
            if (!ret.next()) {
                sql = "INSERT INTO IMPOSTAZIONI (NOME, VALORE) VALUES ('limiteTweet', " + defaultLimiteTweet + ")";
                stmt.executeUpdate(sql);
                LOGGER.log(Level.INFO, () -> "Impostazione valore di default per il limite dei tweet " +
                        "[default " + defaultLimiteTweet + "].");
            }

            sql = "SELECT * FROM IMPOSTAZIONI WHERE NOME='linguaTweet' LIMIT 1";
            ret = stmt.executeQuery(sql);
            if (!ret.next()) {
                sql = "INSERT INTO IMPOSTAZIONI (NOME, VALORE) VALUES ('linguaTweet', '" + defaultLinguatweet + "')";
                stmt.executeUpdate(sql);
                LOGGER.log(Level.INFO, () -> "Impostazione valore di default per la lingua dei tweet " +
                        "[default " + defaultLinguatweet + "].");
            }

            sql = "SELECT * FROM IMPOSTAZIONI WHERE NOME='linguaApplicazione' LIMIT 1";
            ret = stmt.executeQuery(sql);
            if (!ret.next()) {
                sql = "INSERT INTO IMPOSTAZIONI (NOME, VALORE) VALUES ('linguaApplicazione', '" + defaultLingua + "')";
                stmt.executeUpdate(sql);
                LOGGER.log(Level.INFO, () -> "Impostazione valore di default per la lingua dei tweet " +
                        "[default " + defaultLingua + "].");
            }
        } catch (SQLException throwables) {
            LOGGER.log(Level.SEVERE, () -> "Eccezione  SQL: " + throwables.getMessage());
        }
    }


    // GETTER e SETTER valori nelle Impostazioni

    /**
     * Ritorna il limite dei tweet attualmente memorizzato nel sistema (DB)
     *
     * @return numero intero maggiore di zero che rappresenta il limite di tweet per ogni ricerca
     */
    public int getLimiteTweet() {
        int ret = 0;
        try {
            String sql = "SELECT VALORE FROM IMPOSTAZIONI WHERE NOME='limiteTweet' LIMIT 1";

            ResultSet rs = stmt.executeQuery(sql);
            ret = Integer.parseInt(rs.getString("VALORE"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return ret;
    }

    /**
     * Imposta il limite di tweet da ritornare in caso di ricerca
     *
     * @param value numero corrispondente al imite
     */
    public void setLimiteTweet(int value) {
        if (value > 0) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPOSTAZIONI SET VALORE = ? WHERE NOME='limiteTweet'")) {
                ps.setInt(1, value);
                ps.executeUpdate();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
    }

    /**
     * Ritorna i caratteri di riferimento per la lingua di ricerca
     *
     * @return stringa di solitamente due caratteri che rappresenta la lingua di ricerca
     */
    public String getLinguaTweet() {
        String ret = "";
        try {
            String sql = "SELECT VALORE FROM IMPOSTAZIONI WHERE NOME='linguaTweet' LIMIT 1";

            ResultSet rs = stmt.executeQuery(sql);
            ret = rs.getString("VALORE");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return ret;
    }

    /**
     * Imposta la lingua di ricerca dei tweet
     *
     * @param value stringa di solitamente due caratteri che rappresenta la lingua di ricerca
     */
    public void setLinguaTweet(String value) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPOSTAZIONI SET VALORE = ? WHERE NOME='linguaTweet'")) {
            ps.setString(1, value);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Ritorna i caratteri di riferimento per la lingua dell'applicazione
     *
     * @return stringa di solitamente due caratteri che rappresenta la lingua dell'applicazione
     */
    public String getLingua() {
        String ret = "";
        try {
            String sql = "SELECT VALORE FROM IMPOSTAZIONI WHERE NOME='linguaApplicazione' LIMIT 1";

            ResultSet rs = stmt.executeQuery(sql);
            ret = rs.getString("VALORE");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return ret;
    }

    /**
     * Imposta la lingua dell'applicazione
     *
     * @param value stringa di solitamente due caratteri che rappresenta la lingua dell'applicazione
     */
    public void setLingua(String value) {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE IMPOSTAZIONI SET VALORE = ? WHERE NOME='linguaApplicazione'")) {
            ps.setString(1, value);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }


    // METODI LEGATI ALLE RICERCHE

    /**
     * Memorizza una ricerca e tutti i suoi tweet nel DB
     *
     * @param query      stringa oggetto della ricerca
     * @param geo        indica se la ricerca richiede solo tweet geolocalizzati
     * @param searchType indica il tipo di ricerca (0 - Trend, 1 - Posizione, 2 - Username)
     * @param tsS        indica la data di partenza della ricerca
     * @param tsE        indica la data di fine della ricerca
     * @param tweets     lista di RestoredTweets appartenenti alla ricerca
     */
    public void saveSearch(String query, boolean geo, int searchType, Timestamp tsS, Timestamp tsE, List<RestoredTweet> tweets) {
        String tsSString = "1970-01-01 00:00:00";
        if (tsS != null && !(tsS.toString().split(":")[0].equals(
                new Timestamp(System.currentTimeMillis()).toString().split(":")[0])))
            tsSString = tsS.toString();

        String tsEString = "1970-01-01 00:00:00";
        if (tsE != null && !(tsE.toString().split(":")[0].equals(
                new Timestamp(System.currentTimeMillis()).toString().split(":")[0])))
            tsEString = tsE.toString();

        int geol = 0;
        if (geo)
            geol = 1;

        String timestamp = new Timestamp(System.currentTimeMillis()).toString();

        // inseriamo la ricerca
        String sql = "INSERT INTO RICERCA (QUERY, GEO, TIPO, DATA_INIZIO, DATA_FINE, TIME_STAMP) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, query);
            ps.setInt(2, geol);
            ps.setInt(3, searchType);
            ps.setString(4, tsSString);
            ps.setString(5, tsEString);
            ps.setString(6, timestamp);
            ps.executeUpdate();

            // inseriamo tutti i tweet ritornati dalla ricerca e li relazioniamo ad essa
            sql = "INSERT INTO TWEET (TWEETID, USERNAME, USERPROPIC, TESTO, IMMAGINE, GEOAVGLATITUDE, GEOAVGLONGITUDE, POSTO, LINGUA, TIME_STAMP)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            for (RestoredTweet t : tweets) {
                try (PreparedStatement ps1 = conn.prepareStatement(sql)) {
                    // inseriamo il tweet
                    ps1.setLong(1, t.getTweetId());
                    // effettuiamo un fix sui caratteri che necessitano l'escape char
                    ps1.setString(2, StringEscapeUtils.escapeEcmaScript(t.getUserName()).replaceAll("'", "''"));
                    ps1.setString(3, t.getUserProPic());
                    // effettuiamo un fix sui caratteri che necessitano l'escape char
                    ps1.setString(4, StringEscapeUtils.escapeEcmaScript(t.getTesto()).replaceAll("'", "''"));
                    ps1.setInt(5, (t.hasImmagine() ? 1 : 0));
                    ps1.setDouble(6, (Objects.isNull(t.getLatitudine()) ? 0.0 : t.getLatitudine()));
                    ps1.setDouble(7, (Objects.isNull(t.getLongitudine()) ? 0.0 : t.getLongitudine()));
                    // effettuiamo un fix sui caratteri che necessitano l'escape char
                    ps1.setString(8, StringEscapeUtils.escapeEcmaScript(t.getPosto()).replaceAll("'", "''"));
                    ps1.setString(9, t.getLingua());
                    ps1.setString(10, t.getTimestamp().toString());
                    ps1.executeUpdate();
                } catch (SQLException e) {
                    if (e.getMessage().equals("PRIMARY KEY must be unique") || e.getMessage().equals("[SQLITE_CONSTRAINT]  Abort due to constraint violation ()") || e.getMessage().equals("[SQLITE_CONSTRAINT]  Abort due to constraint violation (PRIMARY KEY must be unique)"))
                        LOGGER.log(Level.WARNING, "Tweet già precedentemente memorizzato.");
                    else {
                        LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
                        System.exit(0);
                    }
                }

                // inseriamo la relazione tra la ricerca e il tweet
                PreparedStatement ps2 = conn.prepareStatement("INSERT INTO RELRICERCATWEET (TIME_STAMP, TWEETID) VALUES (?, ?)");
                ps2.setString(1, timestamp);
                ps2.setLong(2, t.getTweetId());
                ps2.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Recupera la cronologia delle ricerche
     *
     * @return lista di ricerche presenti nel DB
     */
    public List<RestoredSearch> restoreHistory() {
        List<RestoredSearch> history = new ArrayList<>();
        try {
            String sql = "SELECT * FROM RICERCA " +
                    "ORDER BY TIME_STAMP DESC";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String query = rs.getString("QUERY");
                int geo = rs.getInt("GEO");
                int tipo = rs.getInt("TIPO");
                Timestamp tsS = Timestamp.valueOf(rs.getString("DATA_INIZIO"));
                Timestamp tsE = Timestamp.valueOf(rs.getString("DATA_FINE"));
                Timestamp timestamp = Timestamp.valueOf(rs.getString("TIME_STAMP"));
                RestoredSearch restoredSearch = new RestoredSearch(query, geo, tipo, tsS, tsE, timestamp);
                history.add(restoredSearch);
            }

            for (RestoredSearch r : history)
                r.setNumberOfResults(countResultingTweets(r));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return history;
    }

    /**
     * Ritorna la lista di tweet appartenenti ad una ricerca
     *
     * @param rSearch ricerca soggetta al calcolo
     * @return lista di RestoredTweets appartenenti alla ricerca
     */
    public List<RestoredTweet> restoreTweetsFromRestoredSearch(RestoredSearch rSearch) {
        List<RestoredTweet> tweets = new ArrayList<>();
        String sql = "SELECT TWEET.TWEETID, TWEET.USERNAME, TWEET.USERPROPIC, TWEET.TESTO, TWEET.IMMAGINE, " +
                "TWEET.GEOAVGLATITUDE, TWEET.GEOAVGLONGITUDE, TWEET.POSTO, TWEET.LINGUA, TWEET.TIME_STAMP " +
                "FROM RELRICERCATWEET " +
                "INNER JOIN RICERCA " +
                "ON RELRICERCATWEET.TIME_STAMP = RICERCA.TIME_STAMP " +
                "INNER JOIN TWEET " +
                "ON RELRICERCATWEET.TWEETID = TWEET.TWEETID " +
                "WHERE RICERCA.TIME_STAMP = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rSearch.getTimestamp().toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                long tweetid = rs.getLong("TWEETID");
                String username = rs.getString("USERNAME");
                String propic = rs.getString("USERPROPIC");
                String testo = rs.getString("TESTO");
                int immagine = rs.getInt("IMMAGINE");
                Double lat = rs.getDouble("GEOAVGLATITUDE");
                Double lon = rs.getDouble("GEOAVGLONGITUDE");
                String posto = rs.getString("POSTO");
                String lang = rs.getString("LINGUA");
                Timestamp timestamp = Timestamp.valueOf(rs.getString("TIME_STAMP"));
                RestoredTweet restoredTweet =
                        new RestoredTweet(tweetid, username, propic, testo, immagine, lat, lon, posto, lang, timestamp);
                tweets.add(restoredTweet);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return tweets;
    }

    /**
     * Conta il numero di tweet di una ricerca
     *
     * @param timestamp il timestamp di riferimento per tale ricerca
     * @return il numero di tweet raccolti da tale ricerca
     */
    private int countResultingTweets(Timestamp timestamp) {
        int count = 0;
        String sql = "SELECT COUNT(*) AS 'numero_tweet' FROM  RELRICERCATWEET " +
                "INNER JOIN RICERCA " +
                "ON RELRICERCATWEET.TIME_STAMP = RICERCA.TIME_STAMP " +
                "WHERE RICERCA.TIME_STAMP = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, timestamp.toString());
            ResultSet rs = ps.executeQuery();
            if (rs != null)
                count = rs.getInt("numero_tweet");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return count;
    }

    /**
     * Conta il numero di tweet di una ricerca
     *
     * @param rSearch ricerca soggetta al calcolo
     * @return il numero di tweet raccolti da tale ricerca
     */
    private int countResultingTweets(RestoredSearch rSearch) {
        return countResultingTweets(rSearch.getTimestamp());
    }

    /**
     * Ritorna tutti i trend ricercati precedentemente
     *
     * @return lista di String dei trend ricercati in passato
     */
    public List<String> getTrendSearched() {
        List<String> lastTrendSearch = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT QUERY FROM RICERCA WHERE TIPO='0'";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
                lastTrendSearch.add(rs.getString(1));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        return lastTrendSearch;
    }

    /**
     * Cancella la ricerca specificata dal DB e i relativi tweet se risultano inutilizzati nelle altre ricerche
     *
     * @param search la ricerca da cancellare
     */
    public void deleteSearch(RestoredSearch search) {
        Timestamp timestamp = search.getTimestamp();
        String sql = "DELETE " +
                "FROM RICERCA " +
                "WHERE TIME_STAMP = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, timestamp.toString());
            ps.executeUpdate();
            sql = "DELETE " +
                    "FROM TWEET " +
                    "WHERE TWEETID IN ( " +
                    "SELECT TWEETID " +
                    "FROM TWEET " +
                    "EXCEPT " +
                    "SELECT DISTINCT RELRICERCATWEET.TWEETID " +
                    "FROM RELRICERCATWEET " +
                    "INNER JOIN TWEET " +
                    "ON RELRICERCATWEET.TWEETID = TWEET.TWEETID" +
                    ")";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, () -> e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

}
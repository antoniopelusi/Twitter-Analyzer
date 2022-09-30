package org.g3_dev.management;

import javafx.scene.control.Alert;
import org.g3_dev.LandingPage;
import org.g3_dev.MainApp;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Classe che gestisce le funzionalità necessarie al reperimento dei risultati di una ricerca su twitter.
 */
public class TwitterManagement {
    private ResourceBundle bundle;
    private final ConfigurationBuilder cb;
    private final int limiteTweet;    // limite tweet recuperati
    private ArrayList<RestoredTweet> lastSearchResult;
    private final SqliteManagement database;


    /**
     * Costruttore della classe
     */
    public TwitterManagement() {
        // necessario per le api twitter4j
        // autenticazione dei token API in modalità ConfigurationBuilder
        // leggo i token keys dal file /resources/json/chiavi-api.json
        String consumerKey = "";
        String consumerSecret = "";
        String accessKey = "";
        String accessSecret = "";

        // nella generazione del file Jar non riusciamo a far includere il file JSON, una soluzione temporanea
        // potrebbe essere quella di aggiungere direttamente le API nel codice e consegnare la versione compilata.
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(MainApp.getProgramDir() + "APIkeys.json")) {
            // leggo dal file JSON
            JSONObject credentials = (JSONObject) parser.parse(reader);
            consumerKey = credentials.get("apiKey").toString();
            consumerSecret = credentials.get("apiSecretKey").toString();
            accessKey = credentials.get("accessToken").toString();
            accessSecret = credentials.get("accessTokenSecret").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessKey)
                .setOAuthAccessTokenSecret(accessSecret);

        // recupero dal DB il limite dei tweet
        database = new SqliteManagement();
        limiteTweet = database.getLimiteTweet();

        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
    }


    /**
     * Ritorna la lista dei tweet raccolti all'ultima ricerca
     *
     * @return lista di RestoredTweet appartenenti all'ultima ricerca
     */
    public List<RestoredTweet> getLastSearchResult() {
        return this.lastSearchResult;
    }

    /**
     * Imposta la lista dei tweet raccolti all'ultima ricerca
     *
     * @param lastSearchResult la lista di RestoredTweet dei tweet appartenenti all'ultima ricerca
     */
    public void setLastSearchResult(List<RestoredTweet> lastSearchResult) {
        this.lastSearchResult = (ArrayList<RestoredTweet>) lastSearchResult;
    }


    // METODI RICERCA
    // metodi utilizzabili esternamente per svolgere le ricerche tramite API

    private boolean tweetsFromApi(List<Status> tweets, Twitter twitter, String username, Paging pg, long lastID) {
        while (tweets.size() < this.limiteTweet) {
            int attuali = tweets.size();
            int nuoviAggiunti;
            try {
                // le api ci permettono di raccogliere 20 tweet per volta dalla timeline di un utente
                tweets.addAll(twitter.getUserTimeline(username, pg));
                nuoviAggiunti = tweets.size() - attuali;
                for (Status t : tweets)
                    if (t.getId() < lastID) lastID = t.getId();
            } catch (TwitterException te) {
                MainApp.summonAlert(bundle.getString("alert_failedUserSearch"), Alert.AlertType.ERROR);
                return false;
            }
            pg.setMaxId(lastID - 1);
            if (nuoviAggiunti == 0)
                break;
        }
        return true;
    }

    /**
     * Ricerca per Username (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @param username username ricercato
     * @param geo      filtro per tweet solo geolocalizzati
     * @return lista delle String rappresentanti i tweet risultanti dalla ricerca
     */
    public Optional<List<String>> getListTweetByUserId(String username, boolean geo) {
        // controllo se l'utente ha inserito o meno il simbolo @ per indicare l'utente, se lo ha messo lo tolgo
        if (username.contains("@"))
            username = username.split("@")[1];

        username = username.strip();    // se presenti spazi al prima e dopo
        String pattern = "^[a-zA-Z]([a-zA-Z0-9_]){2,13}[a-zA-Z0-9]$";
        if (!username.matches(pattern)) {
            MainApp.summonAlert(bundle.getString("alert_correctUsernameFormat"),
                    Alert.AlertType.ERROR);
            return Optional.empty();
        } else {
            Paging pg = new Paging();
            long lastID = Long.MAX_VALUE;
            Twitter twitter = new TwitterFactory(cb.build()).getInstance();
            ArrayList<Status> tweets = new ArrayList<>();
            if (!tweetsFromApi(tweets, twitter, username, pg, lastID)) //ridotta Cognitive Complexity
                return Optional.empty();

            // CONVERSIONE IN RestoredTweet
            Optional<ArrayList<RestoredTweet>> tweetsCollection = RestoredTweet.statusToRestoredtweet(Optional.of(tweets));

            // rimuovo se richiesto i tweet senza geolocalizzazione
            if (geo)
                tweetsCollection = this.removeNotGeolocated(tweetsCollection);

            tweetsCollection = this.trimResults(tweetsCollection, limiteTweet);

            if (tweetsCollection.isPresent()) {
                database.saveSearch(username, geo, 2, null, null, tweetsCollection.get());
                this.setLastSearchResult(tweetsCollection.get());
                return formatTweetsToBeDisplayed(tweetsCollection.get());
            } else
                return Optional.empty();
        }
    }

    /**
     * Ricerca per Trend (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @param trend trend / tag ricercato
     * @param tsS   filtro data di partenza (può essere null)
     * @param tsE   filtro data di fine (può essere null)
     * @return lista delle String rappresentanti i tweet risultanti dalla ricerca
     */
    public Optional<List<String>> getListTweetByTrend(String trend, Timestamp tsS, Timestamp tsE) {
        trend = trend.strip();
        String pattern = "^#?[a-zA-Z0-9][a-zA-Z0-9-_.]*$";
        if (!trend.matches(pattern))
            MainApp.summonAlert("Formato con o senza hashtag davanti: testo, numeri, simboli \".-_\"",
                    Alert.AlertType.ERROR);
        else {
            // controllo se l'utente ha inserito o meno il simbolo # per indicare il trend, se non lo ha messo lo aggiungo
            if (trend.charAt(0) != '#')
                trend = "#" + trend;

            Query query = new Query(trend);     // inserisco la query di ricerca

            Optional<ArrayList<RestoredTweet>> tweetsCollection = this.searchQuery(query, tsS, tsE, false);
            if (tweetsCollection.isPresent()) {
                // salvo la ricerca sul DB
                database.saveSearch(trend, false, 0, tsS, tsE, tweetsCollection.get());
                this.setLastSearchResult(tweetsCollection.get());
                return formatTweetsToBeDisplayed(tweetsCollection.get());
            }
        }
        return Optional.empty();
    }

    /**
     * Ricerca per Posizione (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @param position posizione testuale ricercata
     * @param tsS      filtro data di partenza (può essere null)
     * @param tsE      filtro data di fine (può essere null)
     * @return lista delle String rappresentanti i tweet risultanti dalla ricerca
     */
    public Optional<List<String>> getListTweetByPosition(String position, Timestamp tsS, Timestamp tsE) {
        Optional<GeoLocation> coordinates = this.getGeoLocationFromPlaceString(position);
        Double radius = this.getRadiusFromPlaceString(position);
        if (coordinates.isPresent() && radius != 0) {
            // creo la query di ricerca
            Query query = new Query();
            query.setGeoCode(coordinates.get(), radius, Query.Unit.km);

            Optional<ArrayList<RestoredTweet>> tweetsCollection = this.searchQuery(query, tsS, tsE, false);
            if (tweetsCollection.isPresent()) {
                // salvo la ricerca sul DB
                database.saveSearch(position, false, 1, tsS, tsE, tweetsCollection.get());
                this.setLastSearchResult(tweetsCollection.get());
                return formatTweetsToBeDisplayed(tweetsCollection.get());
            } else
                return Optional.empty();
        } else
            return Optional.empty();
    }


    // METODI INTERNI
    // metodi utili alla modularità del software

    /**
     * A partire dalla stringa contenente il nome di una località geografica ne ricava il punto geografico che
     * la rappresenta maggiormente (sfrutta i servizi di OpenStreetMap)
     *
     * @param position stringa contenente il nome di una località geografica
     * @return il punto geografico che rappresenta maggiormente la località geografica ricercata
     */
    private Optional<GeoLocation> getGeoLocationFromPlaceString(String position) {
        // ottenere la geolocation della nostra ricerca partendo dal nome della città
        Optional<GeoLocation> coordinates;
        Scanner scanner = null;
        try {
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + position + "&format=json");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\Z").next();
            JSONParser parser = new JSONParser();
            JSONArray returnedJson = (JSONArray) parser.parse(response);
            if (returnedJson.size() > 0) {
                JSONObject first = (JSONObject) returnedJson.get(0);
                coordinates = Optional.of(new GeoLocation(Double.parseDouble(first.get("lat").toString()),
                        Double.parseDouble(first.get("lon").toString())));
            } else
                coordinates = Optional.empty();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            coordinates = Optional.empty();
        } finally {
            //non possiamo togliere questo finally e sostituirlo con un try-with-resources dato
            //che scanner dipende da url che non è AutoCloseable (https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)
            Objects.requireNonNull(scanner);
            scanner.close();
        }
        return coordinates;
    }

    /**
     * A partire dalla stringa contenente il nome di una località geografica ne ricava la dimensione del raggio
     * a partire dal punto che più la rappresenta (sfrutta i servizi di OpenStreetMap)
     *
     * @param position stringa contenente il nome di una località geografica
     * @return il raggio a partire dal punto che più rappresenta la località geografica ricercata
     */
    private Double getRadiusFromPlaceString(String position) {
        Double distance = 0.0;
        Scanner scanner = null;
        try {
            URL url = new URL("https://nominatim.openstreetmap.org/search?q=" + position + "&format=json");
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\Z").next();
            JSONParser parser = new JSONParser();
            JSONArray returnedJson = (JSONArray) parser.parse(response);
            if (returnedJson.size() > 0) {
                JSONObject first = (JSONObject) returnedJson.get(0);
                Double centerLat = Double.parseDouble(first.get("lat").toString());
                Double centerLon = Double.parseDouble(first.get("lon").toString());
                JSONArray boundingbox = (JSONArray) first.get("boundingbox");
                Double externalLat = Double.parseDouble(boundingbox.get(0).toString());
                Double externalLon = Double.parseDouble(boundingbox.get(2).toString());

                // la dimezziamo in quanto sperimentalmente si è verificato andare troppo oltre il limite di interesse
                distance = this.haversine(centerLat, centerLon, externalLat, externalLon) / 2;
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return 0.0;
        } finally {
            //non possiamo togliere questo finally e sostituirlo con un try-with-resources dato
            //che scanner dipende da url che non è AutoCloseable (https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)
            Objects.requireNonNull(scanner);
            scanner.close();
        }

        return distance;
    }

    /**
     * Rimuove i tweet senza geolocalizzazione
     *
     * @param tweetsIn lista di RestoredTweet dei tweet di partenza
     * @return lista di RestoredTweet dei tweet soltanto geolocalizzati a partire da quelli in input
     */
    private Optional<ArrayList<RestoredTweet>> removeNotGeolocated(Optional<ArrayList<RestoredTweet>> tweetsIn) {
        if (tweetsIn.isPresent()) {
            Optional<ArrayList<RestoredTweet>> tweetsOut = Optional.of(new ArrayList<>());
            for (int i = 0; i < tweetsIn.get().size(); i++)
                if (tweetsIn.get().get(i).getLatitudine() != null && tweetsIn.get().get(i).getLongitudine() != null)
                    tweetsOut.get().add(tweetsIn.get().get(i));
            return tweetsOut;
        } else
            return Optional.empty();
    }

    /**
     * Rimuove i tweet che non sono compresi nei vincoli di tempo della ricerca
     *
     * @param tweetsIn lista di RestoredTweet dei tweet di partenza
     * @param tsS      filtro data di partenza (può essere null)
     * @param tsE      filtro data di fine (può essere null)
     * @return lista di RestoredTweet dei tweet che soddisfano i criteri di tempo della ricerca a partire da
     * quelli in input
     */
    private Optional<ArrayList<RestoredTweet>> removeNotInTime(
            Optional<ArrayList<RestoredTweet>> tweetsIn,
            Timestamp tsS,
            Timestamp tsE
    ) {
        if (tweetsIn.isPresent()) {
            Optional<ArrayList<RestoredTweet>> tweetsOut = Optional.of(new ArrayList<>());
            for (int i = 0; i < tweetsIn.get().size(); i++) {
                if ((tsS != null && tsE != null && tweetsIn.get().get(i).getTimestamp().after(tsS)
                        && tweetsIn.get().get(i).getTimestamp().before(tsE)) ||
                        (tsS != null && tsE == null && tweetsIn.get().get(i).getTimestamp().after(tsS)) ||
                        (tsS == null && tsE != null && tweetsIn.get().get(i).getTimestamp().before(tsE)))
                    tweetsOut.get().add(tweetsIn.get().get(i));
            }
            return tweetsOut;
        } else
            return Optional.empty();
    }

    /**
     * Rimuove i tweet che sforano la ricerca se presenti
     *
     * @param tweetsIn lista di RestoredTweet dei tweet di partenza
     * @return lista di RestoredTweet di dimensione pari o minore al limite dei tweet richiesti dal sistema
     */
    private Optional<ArrayList<RestoredTweet>> trimResults(Optional<ArrayList<RestoredTweet>> tweetsIn, int limiteTweet) {
        if (tweetsIn.isPresent()) {
            if (tweetsIn.get().size() < limiteTweet)
                return tweetsIn;

            Optional<ArrayList<RestoredTweet>> tweetsOut = Optional.of(new ArrayList<>());
            for (int i = 0; i < limiteTweet; i++)
                tweetsOut.get().add(tweetsIn.get().get(i));
            return tweetsOut;
        } else
            return Optional.empty();
    }

    /**
     * Configurazioni di base per una ricerca via query (o Posizione o Trend)
     *
     * @param query oggetto Query con già applicati i criteri di ricerca specifici (es. il trend oppure la posizione)
     * @param tsS   filtro data di partenza (può essere null)
     * @param tsE   filtro data di fine (può essere null)
     */
    private void prepareSearchQuery(Query query, Timestamp tsS, Timestamp tsE) {
        query.setCount(limiteTweet < 100 ? limiteTweet : 100); // 20 come unità approssimativa per query che richiedono una grande quantità di risultati

        // applico i vari filtri
        String requestedLang = database.getLinguaTweet();
        if (!requestedLang.equals("**"))
            query.setLang(requestedLang);
        if (tsS != null)
            query.setSince(tsS.toString().split(" ")[0]);   // il toString ritorna in formato 'yyyy-mm-dd hh:mm:ss.f...'
        if (tsE != null)
            query.setUntil(tsE.toString().split(" ")[0]);
    }

    /**
     * Si occupa di effettuare la ricerca data al query (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @param query oggetto Query con già applicati i criteri di ricerca specifici (es. il trend oppure la posizione)
     * @param tsS   filtro data di partenza (può essere null)
     * @param tsE   filtro data di fine (può essere null)
     * @param geo   filtro per tweet solo geolocalizzati
     * @return lista di RestoredTweet ritornati da tale ricerca
     */
    private Optional<ArrayList<RestoredTweet>> searchQuery(Query query, Timestamp tsS, Timestamp tsE, boolean geo) {
        prepareSearchQuery(query, tsS, tsE);

        // creo l'oggetto che mi permette di eseguire i comandi di richieste con le API
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();

        QueryResult qr = null;
        ArrayList<Status> tweets = new ArrayList<>();
        do {
            try {
                qr = twitter.search(query);
            } catch (TwitterException e) {
                MainApp.summonAlert(bundle.getString("alert_failedSearch"), Alert.AlertType.ERROR);
            }

            if (qr != null)
                tweets.addAll(qr.getTweets());
            else
                break;
        } while ((limiteTweet - tweets.size()) > 0 && (query = Objects.requireNonNull(qr).nextQuery()) != null);

        // CONVERSIONE IN RestoredTweet
        Optional<ArrayList<RestoredTweet>> tweetsCollection = RestoredTweet.statusToRestoredtweet(Optional.of(tweets));

        if (tsS != null || tsE != null)
            tweetsCollection = this.removeNotInTime(tweetsCollection, tsS, tsE);

        if (geo)
            tweetsCollection = this.removeNotGeolocated(tweetsCollection);

        tweetsCollection = this.trimResults(tweetsCollection, limiteTweet);

        return tweetsCollection;
    }

    /**
     * Formatta una lista di stringhe che rappresentano i tweet all'interno di una lista di RestoredTweet
     *
     * @param tweets lista di RestoredTweet dei tweet di partenza
     * @return lista di stringhe che rappresentano i tweet dalla lista di partenza
     */
    public static Optional<List<String>> formatTweetsToBeDisplayed(List<RestoredTweet> tweets) {
        final List<String> text = tweets.
                stream().
                map(t -> "[" + t.getUserName() + "] : " + t.getTesto()).
                collect(Collectors.toList());

        return (text.isEmpty() ? Optional.empty() : Optional.of(
                IntStream.range(1, text.size() + 1)
                        .mapToObj(index -> (text.size() + 1 - index) + " " + text.get(index - 1))
                        .collect(Collectors.toList())
        ));
    }

    /**
     * Metodo che si occupa di calcolare la distanza tra due punti geografici
     *
     * @param lat1 latitudine punto 1
     * @param lon1 longitudine punto 1
     * @param lat2 latitudine punto 2
     * @param lon2 longitudine punto 2
     * @return distanza in km tra i due punti
     */
    private Double haversine(Double lat1, Double lon1, Double lat2, Double lon2) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        Double rad = 6371.0;
        Double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }


    // METODI ESTERNI
    // metodi utili per gestire i dati forniti dai tweet anche all'eseterno di questa classe

    /**
     * A partire da una data ritorna la rappresentazione di essa sotto forma di stringa (dd/mm/yyyy)
     *
     * @param d data di riferimento
     * @return data ricavata sotto forma di stringa
     */
    public static String convertFromDateToString(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        String day;
        if (c.get(Calendar.DAY_OF_MONTH) < 10)
            day = "0" + c.get(Calendar.DAY_OF_MONTH);
        else
            day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));

        String month;
        if ((c.get(Calendar.MONTH) + 1) < 10)
            month = "0" + (c.get(Calendar.MONTH) + 1);
        else
            month = String.valueOf(c.get(Calendar.MONTH) + 1);

        String year = String.valueOf(c.get(Calendar.YEAR));

        return day + "/" + month + "/" + year;
    }

    /**
     * A partire da una data ritorna l'orario contenuto sotto forma di stringa (formato 24h, hh:mm)
     *
     * @param d data di riferimento
     * @return orario ricavato sotto forma di stringa
     */
    public static String convertFromDateToStringTime(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        String hour;
        if (c.get(Calendar.HOUR_OF_DAY) < 10)
            hour = "0" + c.get(Calendar.HOUR_OF_DAY);
        else
            hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));

        String min;
        if (c.get(Calendar.MINUTE) < 10)
            min = "0" + c.get(Calendar.MINUTE);
        else
            min = String.valueOf(c.get(Calendar.MINUTE));

        return hour + ":" + min;
    }

    /**
     * Converte dal numero intero del tipo di ricerca al nome in stringa corrispondente
     *
     * @param type valore intero che rappresenta il tipo di ricerca
     * @return stringa che rappresenta il nome del tipo di ricerca
     */
    public static String numToStrTipoRicerca(int type) {
        if (type == 1)
            return "Posizione";
        else if (type == 2)
            return "Username";
        else
            return "Trend";
    }

    /**
     * Converte dal nome in stringa del tipo di ricerca al numero intero corrispondente
     *
     * @param type stringa del nome del tipo di ricerca
     * @return intero che rappresenta il tipo di ricerca
     */
    public static int strToNumTipoRicerca(String type) {
        if (type.equals(LandingPage.getSearchTopicStrings()[0]))
            return 0;
        else if (type.equals(LandingPage.getSearchTopicStrings()[1]))
            return 1;
        else
            return 2;
    }

    /**
     * Ritorna i 50 trend più popolari al momento (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @return lista di String contenente i 50 trend top al momento
     * @throws TwitterException problema nel reperimento dei dati dalla libreria twitter4j
     */
    public List<String> getPopularTrends50() throws TwitterException {
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        Trends trends = twitter.getPlaceTrends(1); // 1 == tutto il mondo
        List<String> popularTrends = new ArrayList<>();
        for (Trend tr : trends.getTrends())
            popularTrends.add(tr.getName());
        return popularTrends;
    }

    /**
     * Posta un tweet con un immagine (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @param images lista di stringhe che rappresentano i path delle immagini
     * @param msg    stringa che rappresenta il messaggio del tweet
     */
    public void postTweetWithImage(List<String> images, String msg) {
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        long[] mediaIds = new long[images.size()];
        for (int i = 0; i < images.size(); i++) {
            File imageFile = new File(images.get(i));
            try {
                mediaIds[i] = twitter.uploadMedia(imageFile).getMediaId();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        }
        StatusUpdate statusUpdate = new StatusUpdate(msg);
        statusUpdate.setMediaIds(mediaIds);
        try {
            twitter.updateStatus(statusUpdate);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Posta un tweet solo testuale (Sfrutta le API di Twitter mediante la libreria twitter4j)
     *
     * @param msg stringa che rappresenta il messaggio del tweet
     */
    public void postTweetText(String msg) {
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        try {
            twitter.updateStatus(msg);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

}
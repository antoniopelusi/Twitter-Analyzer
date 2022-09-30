package org.g3_dev.management;

import twitter4j.Status;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Classe che ha il compito di rappresentare un tweet all'interno del programma.
 */
public class RestoredTweet {
    private final long tweetId;
    private final String username;
    private final String userProPic;
    private final String testo;
    private final int immagine;
    private final Double latitudine;
    private final Double longitudine;
    private final String posto;
    private final String lingua;
    private final Timestamp timestamp;


    /**
     * Costruttore della classe
     *
     * @param tweetId     id del tweet
     * @param username    nome del utente
     * @param userProPic  link alla immagine di profilo dell'utente
     * @param testo       testo del tweet
     * @param immagine    indica se è presente un media nel tweet
     * @param latitudine  se il tweet è geolocalizzato la latitudine
     * @param longitudine se il tweet è geolocalizzato la longitudine
     * @param posto       se il tweet è geolocalizzato il nome esteso del luogo
     * @param lingua      lingua del tweet
     * @param timestamp   timestamp di pubblicazione del tweet
     */
    public RestoredTweet(
            long tweetId,
            String username,
            String userProPic,
            String testo,
            int immagine,
            Double latitudine,
            Double longitudine,
            String posto,
            String lingua,
            Timestamp timestamp
    ) {
        this.tweetId = tweetId;
        this.username = username;
        this.userProPic = userProPic;
        this.testo = testo;
        this.immagine = immagine;
        if (latitudine == 0.0 && longitudine == 0.0) {
            this.latitudine = null;
            this.longitudine = null;
            this.posto = "";
        } else {
            this.latitudine = latitudine;
            this.longitudine = longitudine;
            this.posto = posto;
        }
        this.lingua = lingua;
        this.timestamp = timestamp;
    }

    /**
     * @return se è presente o meno un media (immagine) nel tweet
     */
    public boolean hasImmagine() {
        return this.immagine > 0;
    }

    /**
     * @return id del tweet
     */
    public long getTweetId() {
        return tweetId;
    }

    /**
     * @return nome del utente
     */
    public String getUserName() {
        return username;
    }

    /**
     * @return testo del tweet
     */
    public String getTesto() {
        return testo;
    }

    /**
     * @return se il tweet è geolocalizzato la latitudine
     */
    public Double getLatitudine() {
        return latitudine;
    }

    /**
     * @return se il tweet è geolocalizzato la longitudine
     */
    public Double getLongitudine() {
        return longitudine;
    }

    /**
     * @return lingua del tweet
     */
    public String getLingua() {
        return lingua;
    }

    /**
     * @return timestamp di pubblicazione del tweet
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * @return se il tweet è geolocalizzato il nome esteso del luogo
     */
    public String getPosto() {
        return posto;
    }

    /**
     * @return link alla immagine di profilo dell'utente
     */
    public String getUserProPic() {
        return userProPic;
    }


    // METODI DI CONVERSIONE

    /**
     * Converte un oggetto di tipo Status in un oggetto di tipo RestoredTweet
     *
     * @param s oggetto Status da convertire
     * @return oggetto RestoredTweet risultante
     */
    public static RestoredTweet statusToRestoredtweet(Status s) {
        Double[] middlePoint = {0.0, 0.0};
        String nomePosto = "";
        if (s.getPlace() != null) {
            middlePoint = MapsManagement.getCenterPoint(s);
            nomePosto = s.getPlace().getFullName() + ", " + s.getPlace().getCountry();
        }

        return new RestoredTweet(
                s.getId(),
                s.getUser().getName(),
                s.getUser().get400x400ProfileImageURL(),
                s.getText(),
                ((s.getMediaEntities().length > 0) ? 1 : 0),
                middlePoint[0],
                middlePoint[1],
                nomePosto,
                s.getLang(),
                new Timestamp(s.getCreatedAt().getTime()));
        //Sappiamo che l'uso di Date è deprecato ma la libreria twitter4j non ci fornisce altri
        //strumenti per accedere a questa informazione.
        //codice incriminato -> s.getCreatedAt()
    }

    /**
     * Converte una lista di oggetti di tipo Status in una lista di oggetti di tipo RestoredTweet
     *
     * @param s lista di Status da convertire
     * @return lista di RestoredTweet risultante
     */
    public static Optional<ArrayList<RestoredTweet>> statusToRestoredtweet(Optional<ArrayList<Status>> s) {
        if (s.isPresent()) {
            Optional<ArrayList<RestoredTweet>> out = Optional.of(new ArrayList<>());
            for (Status t : s.get()) {
                RestoredTweet rt = statusToRestoredtweet(t);
                out.get().add(rt);
            }
            return out;
        } else
            return Optional.empty();
    }

}

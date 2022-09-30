package org.g3_dev.management;

import java.sql.Timestamp;

/**
 * Classe che ha il compito di rappresentare una ricerca all'interno del programma.
 */
public class RestoredSearch {
    private final String query;
    private final boolean geo;
    private final int tipo;
    private final Timestamp tsS;
    private final Timestamp tsE;
    private final Timestamp timestamp;
    private int numberOfResults;

    /**
     * Costruttore completo
     *
     * @param query           la query testuale di ricerca
     * @param geo             maggiore di zero se la ricerca era solo geolocalizzata
     * @param tipo            indica il tipo di ricerca (0 - Trend, 1 - Posizione, 2 - Username)
     * @param tsS             indica la data di partenza della ricerca
     * @param tsE             indica la data di fine della ricerca
     * @param timestamp       indica il momento in cui la ricerca è stata effettuata
     * @param numberOfResults indica il numero di tweet risultanti da tale ricerca
     */
    public RestoredSearch(
            String query,
            int geo,
            int tipo,
            Timestamp tsS,
            Timestamp tsE,
            Timestamp timestamp,
            int numberOfResults
    ) {
        this.query = query;
        this.geo = geo > 0;
        this.tipo = tipo;
        this.tsS = tsS;
        this.tsE = tsE;
        this.timestamp = timestamp;
        this.numberOfResults = numberOfResults;
    }

    /**
     * Costruttore parziale che non include il numero di tweet
     *
     * @param query     la query testuale di ricerca
     * @param geo       maggiore di zero se la ricerca era solo geolocalizzata
     * @param tipo      indica il tipo di ricerca (0 - Trend, 1 - Posizione, 2 - Username)
     * @param tsS       indica la data di partenza della ricerca
     * @param tsE       indica la data di fine della ricerca
     * @param timestamp indica il momento in cui la ricerca è stata effettuata
     */
    public RestoredSearch(
            String query,
            int geo,
            int tipo,
            Timestamp tsS,
            Timestamp tsE,
            Timestamp timestamp
    ) {
        this.query = query;
        this.geo = geo > 0;
        this.tipo = tipo;
        this.tsS = tsS;
        this.tsE = tsE;
        this.timestamp = timestamp;
        this.numberOfResults = 0;
    }


    // GETTER & SETTER

    public String getQuery() {
        return query;
    }

    public boolean isGeo() {
        return geo;
    }

    public int getTipo() {
        return tipo;
    }

    public Timestamp getTsS() {
        return tsS;
    }

    public Timestamp getTsE() {
        return tsE;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int value) {
        this.numberOfResults = value;
    }

}

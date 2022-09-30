package org.g3_dev;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.g3_dev.management.RestoredTweet;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller dell'interfaccia per la visualizzazione dettagliata delle informazioni
 * geografiche dei tweet in una ricerca.
 */
public class GeoDetails {
    private ResourceBundle bundle;
    private String selectedCountry;
    private List<CountryWithStat> geoDetailedRepr;
    private static final Logger LOGGER = Logger.getLogger(GeoDetails.class.getName());

    @FXML
    private ListView<String> lvCountry;

    @FXML
    private ListView<String> lvRegion;

    @FXML
    private ListView<String> lvTown;


    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
    }


    public void setTweets(List<RestoredTweet> tweets) {
        // genero la struttura dati necessaria
        this.geoDetailedRepr = calculateGeoStats(tweets);
        // mostro la lista degli stati
        Optional<List<String>> countries = findCountries(this.geoDetailedRepr);
        if (countries.isPresent()) {
            this.lvCountry.setItems(FXCollections.observableArrayList(countries.get()));
        } else
            LOGGER.log(Level.WARNING, "Non ci sono stati (non si dovrebbe mai logicamente arrivare qui).");

        this.lvRegion.setItems(null);
        this.lvTown.setItems(null);
    }

    @FXML
    void selectCountry(MouseEvent event) {
        this.selectedCountry = lvCountry.getSelectionModel().getSelectedItem();
        Optional<CountryWithStat> country = findCountry(this.geoDetailedRepr, this.selectedCountry);
        if (country.isPresent()) {
            List<String> regionsStr = country.get().getRegionsGUI();
            this.lvRegion.setItems(FXCollections.observableArrayList(regionsStr));
        } else
            LOGGER.log(Level.WARNING, "Nessuna regione nello stato (logicamente non si dovrebbe mai verificare).");

        this.lvTown.setItems(null);
    }

    @FXML
    void selectRegion(MouseEvent event) {
        String selectedRegion = lvRegion.getSelectionModel().getSelectedItem();
        Optional<CountryWithStat> country = findCountry(this.geoDetailedRepr, this.selectedCountry);
        if (country.isPresent()) {
            Optional<RegionWithStat> region = findRegion(country.get().getRegions(), selectedRegion);
            if (region.isPresent()) {
                List<String> citiesStr = region.get().getCitiesGUI();
                this.lvTown.setItems(FXCollections.observableArrayList(citiesStr));
            } else {
                this.lvTown.setItems(null);
                LOGGER.log(Level.WARNING, "Nessuna città nella regione (logicamente non si dovrebbe mai verificare).");
            }
        } else {
            this.lvTown.setItems(null);
            LOGGER.log(Level.WARNING, "Nessuna regione nello stato (logicamente non si dovrebbe mai verificare).");
        }
    }

    /**
     * Classe nested che rappresenta il dato città con la percentuale corrispettiva.
     */
    static class CityWithStat {
        private String name;
        private float perc;

        public CityWithStat(String name, float perc) {
            this.name = name;
            this.perc = perc;
        }

        public String getName() {
            return name;
        }

        public float getPerc() {
            return perc;
        }
    }

    /**
     * Classe nested che rappresenta il dato regione con la percentuale corrispettiva.
     */
    static class RegionWithStat {
        private String name;
        private float perc;
        private List<CityWithStat> cities;

        public RegionWithStat(String name, float perc, List<CityWithStat> cities) {
            this.name = name;
            this.perc = perc;
            this.cities = cities;
        }

        public String getName() {
            return name;
        }

        public float getPerc() {
            return perc;
        }

        public List<CityWithStat> getCities() {
            return cities;
        }

        /**
         * Metodo che restituisce le città con percentuali da passare alla GUI
         *
         * @return lista di stringhe
         */
        public List<String> getCitiesGUI() {
            List<String> ret = new ArrayList<>();
            for (CityWithStat cws : cities)
                ret.add(cws.getName() + " [" + MainApp.getDecimalFormat().format(cws.perc) + "%]");
            return ret;
        }
    }

    /**
     * Classe nested che rappresenta il dato stato con la percentuale corrispettiva.
     */
    static class CountryWithStat {
        private String name;
        private float perc;
        private List<RegionWithStat> regions;

        public CountryWithStat(String name, float perc, List<RegionWithStat> regions) {
            this.name = name;
            this.perc = perc;
            this.regions = regions;
        }

        public String getName() {
            return name;
        }

        public float getPerc() {
            return perc;
        }

        public List<RegionWithStat> getRegions() {
            return regions;
        }

        /**
         * Metodo che restituisce le regioni con percentuali da passare alla GUI
         *
         * @return lista di stringhe
         */
        public List<String> getRegionsGUI() {
            List<String> ret = new ArrayList<>();
            for (RegionWithStat rws : regions)
                ret.add(rws.getName() + " [" + MainApp.getDecimalFormat().format(rws.perc) + "%]");
            return ret;
        }
    }

    /**
     * Creazione struttura dati che ospita le statistiche del territorio.
     *
     * @return Map<String, Map < String, List < String>>>, raffigurazione di seguito
     * {
     * Stato1={ Regione1=[città1, città2], Regione2=[città1, città2, città3] }
     * Stato2={ Regione1=[città1, città2], Regione2=[città1], Regione3=[città1] }
     * ...
     * }
     */
    private Map<String, Map<String, List<String>>> detailedGeoStats(List<RestoredTweet> tweets) {
        boolean hasExceptions = false;
        Map<String, Map<String, List<String>>> details = new HashMap<>();
        for (RestoredTweet rt : tweets) {
            if (!rt.getPosto().equals("") && !Objects.isNull(rt.getPosto())) {
                String[] geoData = rt.getPosto().split(", "); // Es: Carpi, Emilia Romagna, Italia
                if (geoData.length == 3) {
                    if (details.containsKey(geoData[2])) { // esiste già lo stato
                        if (details.get(geoData[2]).containsKey(geoData[1])) // esiste già la regione
                            details.get(geoData[2]).get(geoData[1]).add(geoData[0]);
                        else { // non esiste la regione
                            List<String> cities = new ArrayList<>();
                            cities.add(geoData[0]);
                            details.get(geoData[2]).put(geoData[1], cities);
                        }
                    } else { // non esiste questo stato
                        Map<String, List<String>> region = new HashMap<>();
                        List<String> cities = new ArrayList<>();
                        cities.add(geoData[0]);
                        region.put(geoData[1], cities);
                        details.put(geoData[2], region);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "FORMATO LUOGO NON SUPPORTATO : " + rt.getPosto() +
                            " è stato IGNORATO.");
                    hasExceptions = true;
                }
            }
        }

        if (hasExceptions)
            MainApp.summonAlert(bundle.getString("alert_someGeoNotSupportedFormat"), Alert.AlertType.ERROR);

        return details;
    }

    /**
     * Metodo per ottenere il numero di posti totali in generale.
     *
     * @return numero del conteggio
     */
    private int countAllPlaces(Map<String, Map<String, List<String>>> dgs) {
        int total = 0;
        for (Map.Entry<String, Map<String, List<String>>> country : dgs.entrySet())
            for (Map.Entry<String, List<String>> region : country.getValue().entrySet())
                total += region.getValue().size();
        return total;
    }

    /**
     * Metodo per ottenere il numero di posti di uno stato.
     *
     * @return numero del conteggio
     */
    private int countPlacesInCountry(Map<String, List<String>> country) {
        int totalCountry = 0;
        for (Map.Entry<String, List<String>> region : country.entrySet())
            totalCountry += region.getValue().size();
        return totalCountry;
    }

    /**
     * Metodo che naviga la struttura dati delle statistiche del territorio e calcola le percentuali.
     *
     * @return lista di oggetti CountryWithStat, qua ci sono tutte le informazioni necessarie per accedere e stampare.
     */
    private List<CountryWithStat> calculateGeoStats(List<RestoredTweet> tweets) {
        Map<String, Map<String, List<String>>> dgs = detailedGeoStats(tweets);
        List<CountryWithStat> toDisplay = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> country : dgs.entrySet()) { // stati
            int totalCountry = countPlacesInCountry(country.getValue());
            int allPlacesNum = countAllPlaces(dgs);
            float countryPerc = 0;
            if (allPlacesNum > 0) countryPerc = totalCountry * 100.0F / allPlacesNum;
            List<RegionWithStat> lrws = new ArrayList<>();
            for (Map.Entry<String, List<String>> region : country.getValue().entrySet()) { // regioni
                int totalRegion = region.getValue().size();
                float regionPerc = 0;
                if (totalCountry > 0) regionPerc = totalRegion * 100.0F / totalCountry;
                List<CityWithStat> lcws = new ArrayList<>();
                for (String city : region.getValue()) { // città
                    int totalCity = (int) region.getValue().stream().filter(e -> e.equals(city)).count();
                    float cityPerc = totalCity * 100.0F / totalRegion;
                    if (lcws.stream().noneMatch(e -> e.getName().equals(city))) // non duplico la percentuale
                        lcws.add(new CityWithStat(city, cityPerc));
                }
                lrws.add(new RegionWithStat(region.getKey(), regionPerc, lcws));
            }
            toDisplay.add(new CountryWithStat(country.getKey(), countryPerc, lrws));
        }
        return toDisplay;
    }

    /**
     * Metodo che restituisce gli stati con percentuali da passare alla GUI
     *
     * @return lista di stringhe
     */
    private Optional<List<String>> findCountries(List<CountryWithStat> lcws) {
        List<String> ret = new ArrayList<>();
        for (CountryWithStat cws : lcws)
            ret.add(cws.getName() + " [" + MainApp.getDecimalFormat().format(cws.getPerc()) + "%]");
        if (!ret.isEmpty())
            return Optional.of(ret);
        return Optional.empty();
    }

    /**
     * Cerca un oggetto stato che ha come nome la stringa passata come parametro
     *
     * @param lcws    lista degli stati con statistiche
     * @param country stato da cercare
     * @return oggetto che rappresenta se ha trovato un match
     */
    private Optional<CountryWithStat> findCountry(List<CountryWithStat> lcws, String country) {
        List<CountryWithStat> filtered = lcws.stream().
                filter(e -> e.getName().equals(country.split("\\[")[0].strip())).
                collect(Collectors.toList());
        if (!filtered.isEmpty())
            return Optional.of(filtered.get(0));
        return Optional.empty();
    }

    /**
     * Cerca un oggetto regione che ha come nome la stringa passata come parametro
     *
     * @param lrws   lista delle regioni con statistiche
     * @param region regione da cercare
     * @return oggetto che rappresenta se ha trovato un match
     */
    private Optional<RegionWithStat> findRegion(List<RegionWithStat> lrws, String region) {
        List<RegionWithStat> filtered = lrws.stream().
                filter(e -> e.getName().equals(region.split("\\[")[0].strip())).
                collect(Collectors.toList());
        if (!filtered.isEmpty())
            return Optional.of(filtered.get(0));
        return Optional.empty();
    }

}


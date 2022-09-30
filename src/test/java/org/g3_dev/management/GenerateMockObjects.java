package org.g3_dev.management;

import java.sql.Timestamp;
import java.util.ArrayList;

public class GenerateMockObjects {
    private ArrayList<RestoredTweet> tweets = new ArrayList<>();

    public void addRestoredTweet(RestoredTweet mo){
        tweets.add(mo);
    }

    public ArrayList<RestoredTweet> generateTweets(){
        int i=1;
        Double[] modena = {44.6568, 10.9202};
        Double[] teramo = {42.6659, 13.7188};
        Double[] bologna = {44.5075, 11.3514};
        Double[] firenze = {43.7874, 11.2499};
        Double[] milano = {45.4773, 9.1815};
        Double[] roma = {41.9109, 12.4818};
        Double[] venezia = {45.4398, 12.3319};

        Timestamp da = Timestamp.valueOf("2021-04-11 07:00:00");
        Timestamp finoA = Timestamp.valueOf("2021-04-30 10:00:00");
        // 1 - g3_dev - Buongiornissimo - noimm - geo no - 2021-04-09 9:00:00
        RestoredTweet mo = new RestoredTweet(i++, "g3_dev", "", "Buongiornissimo", 0, 0.0, 0.0, "", "it", Timestamp.valueOf("2021-04-09 9:00:00"));
        addRestoredTweet(mo);

        // 2 - g3_dev - Caffe - immsi - geo Modena - 2021-04-10 9:00:00
        mo = new RestoredTweet(i++, "g3_dev", "", "Caffe", 1, modena[0], modena[1], "Modena, Emilia Romagna, Italia", "it", Timestamp.valueOf("2021-04-10 9:00:00"));
        addRestoredTweet(mo);

        // 3 - g3_dev - Al lavoro! #pds2021 - immno - geo Bologna - 2021-04-11 9:59:59
        mo = new RestoredTweet(i++, "g3_dev", "", "Al lavoro! #pds2021", 0, bologna[0], bologna[1], "Bologna, Emilia Romagna, Italia", "it", Timestamp.valueOf("2021-04-11 9:59:59"));
        addRestoredTweet(mo);

        // 4 - ChiaraFerragni - Family first #Family #Love - immsi - geo Milano - 2021-04-11 10:00:00
        mo = new RestoredTweet(i++, "ChiaraFerragni", "", "Family first #family #love", 1, milano[0], milano[1], "Milano, Lombardia, Italia", "en", Timestamp.valueOf("2021-04-11 10:00:00"));
        addRestoredTweet(mo);

        // 5 - g3_dev - Pausa, zzz #pds2021 - immno - geo Bologna - 2021-04-11 11:00:00
        mo = new RestoredTweet(i++, "g3_dev", "", "Pausa, zzz #pds2021", 0, bologna[0], bologna[1], "Bologna, Emilia Romagna, Italia", "it", Timestamp.valueOf("2021-04-11 11:00:00"));
        addRestoredTweet(mo);

        // 6 - g3_dev - Finalmente a casa #xbox - immso - geo no - 2021-04-14 15:00:00
        mo = new RestoredTweet(i++, "g3_dev", "", "Finalmente a casa #xbox", 0, 0.0, 0.0, "", "it", Timestamp.valueOf("2021-04-14 15:00:00"));
        addRestoredTweet(mo);

        // 7 - g3_dev - Bellaaaa - immno - geo Teramo - 7 2021-04-30 9:59:59
        mo = new RestoredTweet(i++, "g3_dev", "", "Bellaaaa", 0, teramo[0], teramo[1], "Teramo, Abruzzo, Italia", "it", Timestamp.valueOf("2021-04-30 9:59:59"));
        addRestoredTweet(mo);

        //8 - ChiaraFerragni - Visiting Uffizi #Firenze #art - geo Firenze - 2021-04-30 10:00:00
        mo = new RestoredTweet(i++, "ChiaraFerragni", "", "Visiting Uffizi #Firenze #art", 1, firenze[0], firenze[1], "Firenze, Toscana, Italia", "en", Timestamp.valueOf("2021-04-30 10:00:00"));
        addRestoredTweet(mo);

        //9 - ChiaraFerragni - Visiting Colosseo #Colosseo #Rome #art - geo Roma - 2021-05-01 9:00:00
        mo = new RestoredTweet(i++, "ChiaraFerragni", "", "Visiting Colosseo #Colosseo #Rome #art", 1, roma[0], roma[1], "Roma, Lazio, Italia", "en", Timestamp.valueOf("2021-05-01 9:00:00"));
        addRestoredTweet(mo);

        //10- ChiaraFerragni - Visiting Venice #Venezia #Venice #art - geo Venezia - 2021-05-02 9:00:00
        mo = new RestoredTweet(i++, "ChiaraFerragni", "", "Visiting Venice #Venezia #Venice #art", 1, venezia[0], venezia[1], "Venezia, Veneto, Italia", "en", Timestamp.valueOf("2021-05-02 9:00:00"));
        addRestoredTweet(mo);

        return tweets;
    }
}

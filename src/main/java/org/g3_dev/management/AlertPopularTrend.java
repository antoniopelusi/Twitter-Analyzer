package org.g3_dev.management;

import org.g3_dev.MainApp;
import twitter4j.TwitterException;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe che gestisce le funzionalità necessarie all'implementazione delle notifiche.
 */
public class AlertPopularTrend extends Thread {
    private static final SqliteManagement database = new SqliteManagement();
    private static final Logger LOGGER = Logger.getLogger(AlertPopularTrend.class.getName());

    @Override
    public void run() {
        ResourceBundle bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer", Locale.getDefault());
        try {
            while (true) {
                TwitterManagement tm = new TwitterManagement();
                List<String> lastTrendSearch = database.getTrendSearched();
                List<String> popularTrends = tm.getPopularTrends50();
                for (String lts : lastTrendSearch)
                    for (String pt : popularTrends)
                        if (pt.contains(lts))
                            MainApp.summonConfirmAlert(bundle.getString("titleAlert_tweetAlarm_p1") + " " + pt + " " + bundle.getString("titleAlert_tweetAlarm_p2"),
                                    bundle.getString("alert_shareOnTwitter"),
                                    () -> new TwitterManagement().postTweetText(bundle.getString("txt_sharedAlarm") + " " + pt),
                                    () -> {}, bundle.getString("btn_share"), bundle.getString("btn_exit"));
                sleep((long) 5 * 60 * 1000); // ogni 5 minuti
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interruzione imprevista della scansione allarmi!");
            Thread.currentThread().interrupt();
        }
    }
}

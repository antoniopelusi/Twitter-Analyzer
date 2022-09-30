package org.g3_dev.management;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.g3_dev.MainApp;
import org.g3_dev.TweetWindow;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.Painter;
import twitter4j.GeoLocation;
import twitter4j.Status;

/**
 * Classe che gestisce le funzionalità necessarie all'implementazione della mappa geografica.
 */
public class MapsManagement {
    private static final Logger LOGGER = Logger.getLogger(MapsManagement.class.getName());

    // Apri la mappa nel punto medio di tutte le coordinate
    // matrice NX2 dove la prima colonna è la latitudine e la seconda è la longitudine
    // (N = numero di punti di input)

    /**
     * Metodo che calcola il punto medio a partire da una serie di punti in input.
     *
     * @param points Lista contenente tutti i punti di cui calcolare il punto medio
     * @return punto medio su più punti
     */
    public static Double[] middlePoint(List<Double[]> points) {
        Double[] mid = {0.0, 0.0};
        for (Double[] point : points) {
            mid[0] += point[0];
            mid[1] += point[1];
        }
        mid[0] /= points.size();
        mid[1] /= points.size();
        return mid;
    }

    // METODI ESTERNI

    /**
     * Metodo che a partire da un tweet ottiene la sua boundingBox e chiama la funzione middlePoint() per
     * ottenere il centro del quadrato.
     *
     * @param t Tweet
     * @return punto medio della boundingBox relativa
     */
    public static Double[] getCenterPoint(Status t) {
        GeoLocation[][] boundingBox = t.getPlace().getBoundingBoxCoordinates();
        List<Double[]> boundingBoxCoordinates = new ArrayList<>();
        boundingBoxCoordinates.add(new Double[]{boundingBox[0][0].getLatitude(), boundingBox[0][0].getLongitude()});
        boundingBoxCoordinates.add(new Double[]{boundingBox[0][1].getLatitude(), boundingBox[0][1].getLongitude()});
        boundingBoxCoordinates.add(new Double[]{boundingBox[0][2].getLatitude(), boundingBox[0][2].getLongitude()});
        boundingBoxCoordinates.add(new Double[]{boundingBox[0][3].getLatitude(), boundingBox[0][3].getLongitude()});
        return middlePoint(boundingBoxCoordinates);
    }

    /**
     * Metodo che trasforma un numero decimale nel formato degree/min/sec.
     *
     * @param decimalDegree numero decimale
     * @return array contenente tre elementi [degree,min,sec]
     */
    public static int[] decToDms(Double decimalDegree) {
        int degree = (int) Math.floor(decimalDegree);
        int min = (int) Math.floor((decimalDegree - degree) * 60);
        int sec = (int) Math.floor((decimalDegree - degree - (double)min/(double)60) * 3600);
        return new int[]{degree, min, sec};
    }

    // METODI INTERNI E CLASSI PER GESTIONE MAPPA

    /**
     * Classe che overrida il metodo doPaint per disegnare la mappa.
     */
    private static class SwingWaypointOverlayPainter extends WaypointPainter<SwingWaypoint> {
        @Override
        protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int width, int height) {
            for (SwingWaypoint swingWaypoint : getWaypoints()) {
                Point2D point = jxMapViewer.getTileFactory().geoToPixel(
                        swingWaypoint.getPosition(), jxMapViewer.getZoom());
                Rectangle rectangle = jxMapViewer.getViewportBounds();
                int buttonX = (int) (point.getX() - rectangle.getX());
                int buttonY = (int) (point.getY() - rectangle.getY());
                JButton button = swingWaypoint.getButton();
                button.setLocation(buttonX - button.getWidth() / 2, buttonY - button.getHeight() / 2);
            }
        }
    }

    /**
     * Classe che rappresenta un punto sulla mappa con tutte le relative informazioni.
     */
    private static class SwingWaypoint extends DefaultWaypoint {
        private final JButton button;
        private final String imageUrl;
        private final long tweetId;
        private final String username;
        private final String testo;
        private final boolean immagine;
        private final Double latitudine;
        private final Double longitudine;
        private final String posto;
        private final String lingua;
        private final Timestamp timestamp;

        public SwingWaypoint(
                String imageUrl,
                long tweetId,
                String username,
                String testo,
                boolean immagine,
                Double latitudine,
                Double longitudine,
                String posto,
                String lingua,
                Timestamp timestamp,
                GeoPosition coord
        ) {
            super(coord);
            this.imageUrl = imageUrl;
            this.tweetId = tweetId;
            this.username = username;
            this.testo = testo;
            this.immagine = immagine;
            this.latitudine = latitudine;
            this.longitudine = longitudine;
            this.lingua = lingua;
            this.timestamp = timestamp;
            this.posto = posto;
            button = new JButton(new ImageIcon(Objects.requireNonNull(
                    MapsManagement.class.getResource("/images/geoicon.png"))));
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setSize(50, 50);
            button.setPreferredSize(new Dimension(50, 50));
            button.addMouseListener(new SwingWaypointMouseListener());
            button.setVisible(true);
        }

        JButton getButton() {
            return button;
        }

        private class SwingWaypointMouseListener implements MouseListener {

            @Override
            public void mouseClicked(MouseEvent e) {
                ResourceBundle bundle = ResourceBundle.getBundle("g3_dev_twitter_analyzer",
                        Locale.getDefault());
                RestoredTweet tweet = new RestoredTweet(
                        tweetId,
                        username,
                        imageUrl,
                        testo,
                        (immagine ? 1 : 0),
                        latitudine,
                        longitudine,
                        posto,
                        lingua,
                        timestamp
                );
                TweetWindow.summonTweetDetailedView(tweet, bundle);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // non implementato
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // non implementato
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // non implementato
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // non implementato
            }
        }
    }

    /**
     * Classe che rappresenta un tragitto sulla mappa.
     */
    private static class RoutePainter implements Painter<JXMapViewer> {
        private final List<GeoPosition> track;

        public RoutePainter(List<GeoPosition> track) {
            this.track = new ArrayList<>(track);
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            g = (Graphics2D) g.create();

            // convert from viewport to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            // contorno linea
            g.setColor(Color.black);
            g.setStroke(new BasicStroke(4));
            drawRoute(g, map);
            g.dispose();
        }

        private void drawRoute(Graphics2D g, JXMapViewer map) {
            int lastX = 0;
            int lastY = 0;
            int currentX;
            int currentY;

            boolean first = true; // non collego l'origine col primo punto
            for (GeoPosition gp : track) {
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                if (first) {
                    first = false;
                    currentX = (int) pt.getX();
                    currentY = (int) pt.getY();
                } else {
                    currentX = (int) pt.getX();
                    currentY = (int) pt.getY();

                    Double arrowAngle = Math.PI / 12.0d;
                    int dx = currentX - lastX;
                    int dy = currentY - lastY;
                    Double angle = Math.atan2(dy, dx);
                    double x1 = 20 * Math.cos(angle - arrowAngle);
                    double y1 = 20 * Math.sin(angle - arrowAngle);
                    double x2 = 20 * Math.cos(angle + arrowAngle);
                    double y2 = 20 * Math.sin(angle + arrowAngle);

                    GeneralPath polygon = new GeneralPath();
                    polygon.moveTo(currentX, currentY);
                    polygon.lineTo(currentX - x1, currentY - y1);
                    polygon.lineTo(currentX - x2, currentY - y2);
                    polygon.closePath();
                    g.fill(polygon);

                    g.drawLine(lastX, lastY, currentX, currentY);
                }

                lastX = currentX;
                lastY = currentY;
            }
        }
    }

    /**
     * Crezione oggetto mappa, set focus sul primo punto, aggiunti button marker, aggiunte freccie nel caso in cui sia
     * una ricerca per username.
     *
     * @param track  Lista di posizioni che rappresentano il tragitto dell'utente.
     * @param tweets Lista di tweet da cui otteniamo le informazioni per mostrare il popup a
     *               partire dal button marker della posizione.
     * @param mode   ricerca username (2) ha le freccie mentre le altre due no (0 ed 1).
     * @return oggetto JXMapViewer, mappa in swing da aggiungere alla scena di javaFx.
     */
    public JXMapViewer getMapVisualization(List<GeoPosition> track, List<RestoredTweet> tweets, int mode) {
        // Create a TileFactoryInfo for OSM
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);

        // Setup local file cache
        File cacheDir = new File(MainApp.getProgramDir() + "jxmapviewer2Cache");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

        // Setup JXMapViewer
        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        RoutePainter routePainter = new RoutePainter(track);

        // Set the focus
        mapViewer.setZoom(10);
        int[] lat = decToDms(track.get(0).getLatitude());
        int[] lon = decToDms(track.get(0).getLongitude());
        mapViewer.setAddressLocation(new GeoPosition(lat[0], lat[1], lat[2], lon[0], lon[1], lon[2]));
        LOGGER.log(Level.INFO, () -> "Focus della mappa: " + track.get(0).getLatitude() + ", " + track.get(0).getLongitude());

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));

        // Create waypoints from the geo-positions
        Set<SwingWaypoint> waypoints = new HashSet<>();
        for (int i = 0; i < track.size(); i++)
            waypoints.add(new SwingWaypoint(
                    tweets.get(i).getUserProPic(),
                    tweets.get(i).getTweetId(),
                    tweets.get(i).getUserName(),
                    tweets.get(i).getTesto(),
                    tweets.get(i).hasImmagine(),
                    tweets.get(i).getLatitudine(),
                    tweets.get(i).getLongitudine(),
                    tweets.get(i).getPosto(),
                    tweets.get(i).getLingua(),
                    tweets.get(i).getTimestamp(),
                    track.get(i)));

        if (mode == 2) {
            // VERSIONE CON FRECCE
            // Set the overlay painter
            WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
            swingWaypointPainter.setWaypoints(waypoints);

            // Create a compound painter that uses both the route-painter and the waypoint-painter
            List<Painter<JXMapViewer>> painters = new ArrayList<>();
            painters.add(swingWaypointPainter);
            painters.add(routePainter);

            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
        } else if (mode == 0 || mode == 1) {
            // VERSIONE SENZA FRECCE
            // Set the overlay painter
            WaypointPainter<SwingWaypoint> swingWaypointPainter = new SwingWaypointOverlayPainter();
            swingWaypointPainter.setWaypoints(waypoints);
            mapViewer.setOverlayPainter(swingWaypointPainter);
        }

        // Add the JButtons to the map viewer
        for (SwingWaypoint w : waypoints) {
            mapViewer.add(w.getButton());
        }

        return mapViewer;
    }
}

package com.bukkit.wedtm.mcsl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

public class MCSLUpdater {

    private final Logger log = Logger.getLogger("Minecraft");

    private final MCSL   plugin;

    private Player[]     Players;

    public MCSLUpdater(MCSL instance) {
        this.plugin = instance;
    }

    public void Update(String data) {
        try {
            log.info("[MCSL] Data - " + data);

            URL url = new URL("http://dev.mcserverlist.net/api/update");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(10000); // Time out of 10 Seconds, 1000 = 1 Second.
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            HttpURLConnection httpConnection = (HttpURLConnection) conn;

            // int code = httpConnection.getResponseCode();
            // log.info("[MCSL] Status Code - " + code);

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                // log.info(line);
            }

            wr.close();
            rd.close();

            log.info("[MCSL] Updated Server Listing.");
        }
        catch (Exception e) {
            log.info("[MCSL] Error - " + e);
        }

    }

    public void UpdatePlayerCount() {

        Players = this.plugin.getServer().getOnlinePlayers();

        String players_data;

        if (Players.length > 0) {
            players_data = "";
            for (Player p : Players) {
                players_data += p.getDisplayName() + ", ";
            }
        }
        else {
            players_data = "  ";
        }

        players_data = players_data.substring(0, players_data.length() - 2);

        try {
            String data = URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(plugin.properties.get("mcsl-key"), "UTF-8");
            data += "&" + URLEncoder.encode("player_count", "UTF-8") + "=" + Integer.toString(Players.length);
            data += "&" + URLEncoder.encode("player_list", "UTF-8") + "=" + URLEncoder.encode(players_data, "UTF-8");
            this.Update(data);
        }
        catch (UnsupportedEncodingException e) {
            log.info("[MCSL] Error - " + e);
        }
    }

    public void timedUpdate() {
        int delay = 10;
        int period = Integer.parseInt(plugin.properties.get("update-interval"));

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                UpdatePlayerCount();
            }
        }, delay * 1000, period * 1000);
    }

}
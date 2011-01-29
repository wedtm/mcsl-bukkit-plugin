package com.bukkit.wedtm.mcsl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

public class MCSLUpdater {

    private final Logger log = Logger.getLogger("Minecraft");

    // private final MCSL plugin;

    // public MCSLUpdater(MCSL instance) {
    // this.plugin = instance;
    // }

    public void Update(String data) {
        try {
            log.info("[MCSL] Data - " + data);
            // Send data
            URL url = new URL("http://dev.mcserverlist.net/api/update");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(10000); // Time out of 10 Seconds, 1000 = 1 Second.
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            HttpURLConnection httpConnection = (HttpURLConnection) conn;

            int code = httpConnection.getResponseCode();

            log.info("[MCSL] Status Code - " + code);

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                log.info(line);
            }

            wr.close();
            rd.close();

            log.info("[MCSL] Updated Server Listing.");
        }
        catch (Exception e) {
            // log.info("[MCSL] Error - " + e);
        }

    }
}

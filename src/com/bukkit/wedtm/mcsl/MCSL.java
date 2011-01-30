package com.bukkit.wedtm.mcsl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MCSL for Bukkit
 * 
 * @author <Your Name>
 */
public class MCSL extends JavaPlugin {
    private final MCSLPlayerListener playerListener = new MCSLPlayerListener(this);

    public HashMap<String, String>   properties     = new HashMap<String, String>();

    public final MCSLUpdater         updater        = new MCSLUpdater(this);

    private final Logger             log            = Logger.getLogger("Minecraft");

    private final File               config;

    private Boolean                  enabled        = true;

    public MCSL(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) throws IOException {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        log.info("[MCSL] Performing checks");

        this.config = new File(folder + "/MCSL.properties");

        if (this.config.exists()) {
            if (!loadProperties()) {
                log.info("[MCSL] Error reading from MCSL.properties file.");
                this.enabled = false;
            }
            if (!this.properties.containsKey("mcsl-key")) {
                log.info("[MCSL] mcsl-key is not set in MCSL.properties.");
                this.enabled = false;
            }

            if (!this.properties.containsKey("update-interval")) {
                log.info("[MCSL] update-interval is not set in MCSL.properties.");
                this.enabled = false;
            }

            if (!this.properties.containsKey("max-players")) {
                log.info("[MCSL] max-players is not set in server.properties.");
                this.enabled = false;
            }
        }
        else {
            if (!setupProperties(folder)) {
                log.info("[MCSL] Error trying to create and setup MCSL Properties file.");
                this.enabled = false;
            }
        }
    }

    @Override
    public void onEnable() {
        if (this.enabled == false) {
            this.setEnabled(false);
        }
        else {
            PluginManager pm = getServer().getPluginManager();

            pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Monitor, this);
            pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);

            log.info(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " is enabled!");

            startData(); // First we send off the initial data which contains information about the server and what plugins are running.
            updater.timedUpdate(); // Start off our timed action which will constantly send updates to MCSL.
        }
    }

    @Override
    public void onDisable() {
        log.info("[MCSL] Disabled.");
    }

    public boolean loadProperties() {
        Scanner serverScanner = null;
        try {
            serverScanner = new Scanner(new FileInputStream("server.properties"));
            while (serverScanner.hasNextLine()) {
                String line = serverScanner.nextLine();
                if (line.startsWith("#"))
                    continue;

                if (line.startsWith("max-players="))
                    this.properties.put("max-players", line.split("=")[1].trim().toString());

                if (line.startsWith("pvp="))
                    this.properties.put("pvp", line.split("=")[1].trim());

                if (line.startsWith("hellworld="))
                    this.properties.put("hellworld", line.split("=")[1].trim());

                if (line.startsWith("spawn-monsters="))
                    this.properties.put("spawn-monsters", line.split("=")[1].trim());

                if (line.startsWith("spawn-animals="))
                    this.properties.put("spawn-animals", line.split("=")[1].trim());

                if (line.startsWith("online-mode="))
                    this.properties.put("online-mode", line.split("=")[1].trim());
            }
        }
        catch (Exception e) {
            log.info("[MCSL] Error (server.properties) - " + e);
            return false;
        }
        finally {
            serverScanner.close();
        }

        Scanner msclScanner = null;
        try {
            msclScanner = new Scanner(new FileInputStream("./" + this.config));
            while (msclScanner.hasNextLine()) {
                String line = msclScanner.nextLine();
                String[] split = line.split("=");

                if (line.startsWith("#"))
                    continue;

                if (split.length < 2)
                    continue;

                if (line.startsWith("mcsl-key=")) {
                    this.properties.put("mcsl-key", split[1].trim().toString());
                }

                if (line.startsWith("update-interval=")) {
                    this.properties.put("update-interval", split[1].trim().toString());
                }
            }
        }
        catch (Exception e) {
            log.info("[MCSL] Error (MCSL.properties) - " + e);
            return false;
        }
        finally {
            msclScanner.close();
        }

        return true;
    }

    public boolean setupProperties(File folder) {
        folder.mkdirs();

        try {
            this.config.createNewFile();

            FileWriter fstream = new FileWriter(this.config);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("# Unique API-Key for your server from the MCSL Website.\n");
            out.write("mcsl-key=\n");
            out.write("# How long of an interval between each MCSL Update.\n");
            out.write("# Default = 30 Seconds, anything lower than 10 Seconds is ignored.\n");
            out.write("update-interval=30\n");

            out.close();
            fstream.close();

            log.info("[MCSL] Default properties file created, please edit the file to include your API-Key to allow MCSL to Enable");
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "[MCSL] Error - " + e);
            return false;
        }

        return true;
    }

    public void startData() {
        try {
            String data = "";

            data += URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(this.properties.get("mcsl-key"), "UTF-8") + "&";

            for (String key : this.properties.keySet()) {
                String value = this.properties.get(key);
                if ((!key.equalsIgnoreCase("mcsl-key")) && (!key.equalsIgnoreCase("update-interval"))) {
                    data += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";
                }
            }

            data += "plugins=";

            String plugins = "";

            for (int pos = 0; pos < getServer().getPluginManager().getPlugins().length; pos++) {
                plugins += getServer().getPluginManager().getPlugins()[pos].getDescription().getName() + ", ";
            }

            data += URLEncoder.encode(plugins, "UTF-8");

            data = data.substring(0, data.length() - 2);

            updater.Update(data);
        }
        catch (UnsupportedEncodingException e) {
            log.info("[MCSL] Error - " + e);
        }
    }
}
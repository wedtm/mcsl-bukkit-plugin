package com.bukkit.wedtm.mcsl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;
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
    // private final MCSLBlockListener blockListener = new MCSLBlockListener(this);

    private final MCSLUpdater        updater        = new MCSLUpdater();

    private final Logger             log            = Logger.getLogger("Minecraft");

    private final File               config;

    public HashMap<String, String>   properties     = new HashMap<String, String>();

    public String                    mcsl_key;
    public Integer                   max_players;

    public MCSL(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
            throws IOException {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);

        folder.mkdirs();

        this.config = new File(folder + "/MCSL.properties");
        this.config.createNewFile();

        if (!loadProperties()) {
            this.setEnabled(false);
        }
    }

    @Override
    public void onEnable() {

        if (!this.properties.containsKey("mcsl-key")) {
            log.info("mcsl-key is not set in MCSL.properties, MCSL is disabled.");
            this.setEnabled(false);
            return;
        }

        if (!this.properties.containsKey("max-players")) {
            log.info("max-players is not set in server.properties, MCSL is disabled.");
            this.setEnabled(false);
            return;
        }

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

        try {
            String data = "";
            for (String key : this.properties.keySet()) {
                String value = this.properties.get(key);

                data += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8") + "&";

            }

            data = data.substring(0, data.length() - 1);

            updater.Update(data);
        }
        catch (UnsupportedEncodingException e) {
            log.info("[MCSL] Error - " + e);
        }
    }

    @Override
    public void onDisable() {
        log.info("[MCSL] Unloaded.");
    }

    public boolean loadProperties() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream("server.properties"));
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
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
            // Now we scan the MCSL Properties file for further settings.
            scanner = new Scanner(this.config);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.startsWith("#"))
                    continue;

                if (line.startsWith("mcsl-key="))
                    this.properties.put("mcsl-key", line.split("=")[1].trim().toString());
            }
            return true;
        }
        catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        finally {
            scanner.close();
        }
    }
}
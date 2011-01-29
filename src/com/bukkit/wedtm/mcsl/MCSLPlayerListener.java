package com.bukkit.wedtm.mcsl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Handle events for all Player related events
 * 
 * @author WedTM
 */
public class MCSLPlayerListener extends PlayerListener {
    private final MCSL        plugin;

    private Player[]          Players;

    private final Logger      log     = Logger.getLogger("Minecraft");

    private final MCSLUpdater updater = new MCSLUpdater();

    public MCSLPlayerListener(MCSL instance) {
        plugin = instance;
        // Players = new ArrayList<Player>();
        int delay = 30; // Delay first run by 10 Seconds
        int period = 30; // Repeat function every 60 Seconds
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                UpdatePlayerCount();
            }
        }, delay * 1000, period * 1000);
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        String pName = event.getPlayer().getName();
        log.info("[MCSL] " + pName + " logged in.");
        // if (Players.contains(event.getPlayer()) == false) {
        // Players.add(event.getPlayer());
        // }
        UpdatePlayerCount();
    }

    @Override
    public void onPlayerQuit(PlayerEvent event) {
        String pName = event.getPlayer().getName();
        log.info("[MCSL] " + pName + " logged out.");

        // Players.remove(event.getPlayer());

        UpdatePlayerCount();
    }

    public void UpdatePlayerCount() {

        Players = plugin.getServer().getOnlinePlayers();

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
            updater.Update(data);
        }
        catch (UnsupportedEncodingException e) {
            // log.info("[MCSL] Error - " + e);
        }
    }
}

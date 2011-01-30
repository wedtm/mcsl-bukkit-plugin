package com.bukkit.wedtm.mcsl;

import java.util.logging.Logger;

import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * Handle events for all Player related events
 * 
 * @author WedTM
 */
public class MCSLPlayerListener extends PlayerListener {
    private final MCSL   plugin;

    private final Logger log = Logger.getLogger("Minecraft");

    public MCSLPlayerListener(MCSL instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerLogin(PlayerLoginEvent event) {
        String pName = event.getPlayer().getName();
        log.info("[MCSL] " + pName + " logged in.");
        plugin.updater.UpdatePlayerCount();
    }

    @Override
    public void onPlayerQuit(PlayerEvent event) {
        String pName = event.getPlayer().getName();
        log.info("[MCSL] " + pName + " logged out.");
        plugin.updater.UpdatePlayerCount();
    }
}
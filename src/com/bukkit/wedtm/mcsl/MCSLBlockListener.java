package com.bukkit.wedtm.mcsl;

import org.bukkit.Server;
import org.bukkit.event.block.BlockListener;

/**
 * MCSL block listener
 * 
 * @author WedTM
 */
@SuppressWarnings("unused")
public class MCSLBlockListener extends BlockListener {

    private final MCSL   plugin;
    private final Server server;

    public String        stuff = "";

    public MCSLBlockListener(final MCSL plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

}

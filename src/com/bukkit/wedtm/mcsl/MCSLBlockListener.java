package com.bukkit.wedtm.mcsl;

import org.bukkit.block.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;

/**
 * MCSL block listener
 * @author WedTM
 */
public class MCSLBlockListener extends BlockListener {
    private final MCSL plugin;
    public String stuff ="";

    public MCSLBlockListener(final MCSL plugin) {
        this.plugin = plugin;
    }
    
   
    
}

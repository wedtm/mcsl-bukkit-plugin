package com.bukkit.wedtm.mcsl;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.Server;
import org.bukkit.entity.Player;
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
	private final MCSLBlockListener blockListener = new MCSLBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    public String mcsl_key;
    public Integer max_players;
    
    public MCSL(PluginLoader pluginLoader, Server instance,
            PluginDescriptionFile desc, File folder, File plugin,
            ClassLoader cLoader) throws IOException {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
        Scanner scanner = null;
        try {
          scanner = new Scanner(new FileInputStream("server.properties"));
          while (scanner.hasNextLine()){
        	
            String line = scanner.nextLine();
            if(line.startsWith("#"))
            	continue;
            
            if(line.startsWith("mcsl-key="))
            	this.mcsl_key = line.split("=")[1].trim();
            
            if(line.startsWith("max-players="))
            	this.max_players = Integer.parseInt(line.split("=")[1].trim());
          }
        }
        catch (Exception e) { System.out.println(e.getMessage()); }
        finally{
          scanner.close();
        }
    }

   

    public void onEnable() {
        // TODO: Place any custom enable code here including the registration of any events

        // Register our events
    	if(this.mcsl_key == null)
    	{
    		System.out.println("mcsl-key is not set in server.properties, MCSL is disabled.");
    		this.setEnabled(false);
    		return;
    	}
    	
    	if(this.max_players == null)
    	{
    		System.out.println("max-players is not set in server.properties, MCSL is disabled.");
    		this.setEnabled(false);
    		return;
    	}
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
    }
    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        System.out.println("[mcsl] Unloaded.");
    }
    public boolean isDebugging(final Player player) {
        if (debugees.containsKey(player)) {
            return debugees.get(player);
        } else {
            return false;
        }
    }

    public void setDebugging(final Player player, final boolean value) {
        debugees.put(player, value);
    }
}


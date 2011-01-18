package com.bukkit.wedtm.mcsl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;





/**
 * Handle events for all Player related events
 * @author WedTM
 */
public class MCSLPlayerListener extends PlayerListener {
    private final MCSL plugin;
    private ArrayList<Player> Players;
    private Timer timer = new Timer(true);
    private String previous = null;

    public MCSLPlayerListener(MCSL instance) {
        plugin = instance;
        Players = new ArrayList<Player>();
        int delay = 10;   // delay for 5 sec.
        int period = 60;  // repeat every sec.
        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    Update();
                }
            }, delay * 1000, period * 1000);
    }

    public void onPlayerLogin(PlayerLoginEvent event) {
    	String pName = event.getPlayer().getName();
    	System.out.println("[mcsl] " + pName + " logged in.");
    	Players.add(event.getPlayer());
    	Update();
    }
    
    public void onPlayerQuit(PlayerEvent event) {
    	String pName = event.getPlayer().getName();
    	System.out.println("[mcsl] " + pName + " logged out.");
    	Players.remove(event.getPlayer());
    	
    }
    
   
    public void Update() {
    	try {
    		String players_data = "";
    		if (Players.size() == 0)
    			players_data = "  ";
    		else
    		{
    			for (Player p : Players) {
    				players_data += p.getDisplayName() + ", ";
    			}
    		}
    		
    		players_data = players_data.substring(0, players_data.length() - 2);
        // Construct data
        String data = URLEncoder.encode("key", "UTF-8") + "=" + URLEncoder.encode(plugin.mcsl_key, "UTF-8");
        data += "&" + URLEncoder.encode("player_count", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(Players.size()), "UTF-8");
        data += "&" + URLEncoder.encode("max_players", "UTF-8") + "=" + URLEncoder.encode(plugin.max_players.toString(), "UTF-8");
        data += "&" + URLEncoder.encode("player_list", "UTF-8") + "=" + URLEncoder.encode(players_data, "UTF-8");

        // Send data
        URL url = new URL("http://mcserverlist.net/api/update");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(data);
        wr.flush();

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) { }
        wr.close();
        rd.close();
    	}
    	catch (Exception e) {	}
    	System.out.println("[MCSL] Updated Server Listing.");
    }

    

}


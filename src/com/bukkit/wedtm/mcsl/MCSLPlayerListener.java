package com.bukkit.wedtm.mcsl;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.logging.*;
import net.minecraft.server.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.player.*;


public class McslPlayerListener extends PlayerListener
{
	private final static Logger logger = Logger.getLogger("Minecraft");
	private boolean running = true;
	private final Server server;
	private int maxPlayers = 0;
	private String mcslKey = "";
	private Thread thread;

	@SuppressWarnings("CallToThreadStartDuringObjectConstruction")
	public McslPlayerListener(Mcsl parent)
	{
		this.server = parent.getServer();

		// Get the data from the server.properties file as the server sees it, rather than reading it manually
		try
		{
			// Use reflection to get a private MinecraftServer instance
			ServerConfigurationManager mgr = ((CraftServer)server).getHandle();
			Field c = ServerConfigurationManager.class.getDeclaredField("c");
			c.setAccessible(true); // Our workaround to turn private into public
			MinecraftServer mc = (MinecraftServer)c.get(mgr);

			// Read values from configuration
			this.maxPlayers = mc.d.a("max-players", 20);
			this.mcslKey = mc.d.a("mcsl-key", "");
		}
		catch (Exception ex)
		{
			// Disable the plugin
			logger.log(Level.WARNING, "Error encountered while initializing MCServerlist plugin.", ex);
			parent.getPluginLoader().disablePlugin(parent);
			return;
		}

		// Check for invalid MCSL key
		if (this.mcslKey == null || "".equals(this.mcslKey))
		{
			logger.warning("Invalid or nonexistent MCServerlist key in server properties.");
			parent.getPluginLoader().disablePlugin(parent);
			return;
		}

		// Run Update on a set interval of 1 minute with an initial delay of 10 seconds
		thread = new Thread(new UpdateRunnable());
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	@SuppressWarnings("FinalizeDeclaration")
	protected void finalize() throws Throwable
	{
		// Stop the timer
		if (thread != null && thread.isAlive())
		{
			running = false;
			thread.join();
		}

		super.finalize();
	}

	@Override
	public void onPlayerJoin(PlayerEvent event)
	{
		forceUpdate();
	}

	@Override
	public void onPlayerQuit(PlayerEvent event)
	{
		forceUpdate();
	}

	public void forceUpdate()
	{
		if (thread != null && thread.isAlive()) thread.interrupt();
	}


	private class UpdateRunnable implements Runnable
	{
		@SuppressWarnings("SleepWhileHoldingLock")
		public void run()
		{
			do
			{
				update();
				try
				{
					Thread.sleep(60000);
				}
				catch (InterruptedException ex)
				{
					logger.info("Forcing MCServerlist update.");
				}
			}
			while (running);
		}

		@SuppressWarnings("CallToThreadDumpStack")
		public void update()
		{
			// Check that we aren't receiving an event inappropriately
			if (mcslKey == null || mcslKey.equals("")) return;
			// Compile a comma-space-delimted list of players
			Player[] players = server.getOnlinePlayers();
			StringBuilder list = new StringBuilder();
			if (players.length > 0)
			{
				for (int i = 0; i < players.length; i++)
				{
					if (i > 0) list.append(", ");
					list.append(players[i].getName());
				}
			}

			try
			{
				// Compile POST data
				StringBuilder data = new StringBuilder();
				data.append("key=");
				data.append(URLEncoder.encode(mcslKey, "UTF-8"));
				data.append("&player_count=");
				data.append(Integer.toString(players.length));
				data.append("&max_players=");
				data.append(Integer.toString(maxPlayers));
				data.append("&player_list=");
				data.append(URLEncoder.encode(list.toString(), "UTF-8"));

				OutputStreamWriter tx = null;
				BufferedReader rx = null;
				try
				{
					// Send POST request
					URL url = new URL("http://mcserverlist.net/api/update");
					// Swap line for testing purposes
					//URL url = new URL("http://localhost/mcsl.php");
					HttpURLConnection http = (HttpURLConnection)url.openConnection();
					http.setRequestMethod("POST");
					http.setUseCaches(false);
					http.setConnectTimeout(1000);
					http.setAllowUserInteraction(false);
					http.setInstanceFollowRedirects(true);
					http.setRequestProperty("User-Agent", "Java;Mcsl");
					http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
					http.setRequestProperty("X-Mcsl-Key", mcslKey.replaceAll("[^a-zA-Z0-9]", ""));
					http.setRequestProperty("X-Minecraft-Name", URLEncoder.encode(server.getName(), "UTF-8"));
					http.setRequestProperty("X-Minecraft-Version", server.getVersion());
					http.setDoInput(true);
					http.setDoOutput(true);
					tx = new OutputStreamWriter(http.getOutputStream());
					tx.write(data.toString());
					tx.flush();

					// Get the HTTP response
					rx = new BufferedReader(new InputStreamReader(http.getInputStream()));
					for (String l = ""; rx.ready(); l = rx.readLine())
					{
						if ("".equals(l)) continue;
						else if (l.startsWith("i:")) logger.info(l.substring(2));
						else if (l.startsWith("w:")) logger.warning(l.substring(2));
						else System.out.println(l);
					}
				}
				finally
				{
					if (tx != null) tx.close();
					if (rx != null) rx.close();
				}
			}
			catch (Exception ex)
			{
				logger.log(Level.WARNING, "Error communication with MCServerlist.", ex);
				ex.printStackTrace();
			}
		}
	}
}

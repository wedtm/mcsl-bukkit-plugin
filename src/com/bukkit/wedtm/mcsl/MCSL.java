package com.bukkit.wedtm.mcsl;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;


public class Mcsl extends JavaPlugin
{
	private static final Logger logger = Logger.getLogger("Minecraft");
	private final McslPlayerListener playerListener = new McslPlayerListener(this);
	public final String author;

	public Mcsl(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) throws IOException
	{
		super(pluginLoader, instance, desc, folder, plugin, cLoader);

		// Compile author list
		List<String> authors = desc.getAuthors();
		int alen = authors.size();
		if (alen == 1)
		{
			author = " by " + authors.get(0);
		}
		else if (alen > 1)
		{
			int i = 0;
			StringBuilder bldr = new StringBuilder();
			for (String a : desc.getAuthors())
			{
				if (i + 1 == alen)
				{
					if (alen > 2) bldr.append(",");
					bldr.append(" and ");
				}
				else if (i++ > 0)
				{
					bldr.append(", ");
				}
				bldr.append(a);
			}
			bldr.insert(0, " by ");
			author = bldr.toString();
		}
		else
		{
			author = "";
		}
	}

	@SuppressWarnings("LoggerStringConcat")
	public void onEnable()
	{
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);

		logger.info(getDescription().getName() + " version " + getDescription().getVersion() + author + " enabled.");
	}

	@SuppressWarnings("LoggerStringConcat")
	public void onDisable()
	{
		logger.info(getDescription().getName() + " version " + getDescription().getVersion() + " disabled.");
	}
}

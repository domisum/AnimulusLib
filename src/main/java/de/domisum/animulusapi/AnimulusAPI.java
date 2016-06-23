package de.domisum.animulusapi;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import de.domisum.animulusapi.npc.NPCManager;

public class AnimulusAPI
{

	// REFERENCES
	private static AnimulusAPI instance;
	private JavaPlugin plugin;

	private static NPCManager npcManager;


	// -------
	// CONSTRUCTOR
	// -------
	public AnimulusAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public void onEnable()
	{
		npcManager = new NPCManager();

		getLogger().info(this.getClass().getSimpleName() + " has been enabled\n");
	}

	public void onDisable()
	{
		if(npcManager != null)
			npcManager.terminate();

		getLogger().info(this.getClass().getSimpleName() + " has been disabled\n");
	}


	// -------
	// GETTERS
	// -------
	public static AnimulusAPI getInstance()
	{
		return instance;
	}

	public JavaPlugin getPlugin()
	{
		return this.plugin;
	}

	public Logger getLogger()
	{
		return getInstance().plugin.getLogger();
	}


	public static NPCManager getNPCManager()
	{
		return npcManager;
	}

}

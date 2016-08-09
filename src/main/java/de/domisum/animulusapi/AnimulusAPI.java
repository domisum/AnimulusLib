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
	protected AnimulusAPI(JavaPlugin plugin)
	{
		instance = this;
		this.plugin = plugin;

		onEnable();
	}

	public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		new AnimulusAPI(plugin);
	}

	public static void disable()
	{
		if(instance == null)
			return;

		getInstance().onDisable();
		instance = null;
	}

	protected void onEnable()
	{
		npcManager = new NPCManager();

		getLogger().info(this.getClass().getSimpleName() + " has been enabled");
	}

	protected void onDisable()
	{
		if(npcManager != null)
			npcManager.terminate();

		getLogger().info(this.getClass().getSimpleName() + " has been disabled");
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

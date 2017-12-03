package de.domisum.lib.animulus;

import de.domisum.lib.animulus.npc.NPCManager;
import de.domisum.lib.auxilium.util.java.annotations.API;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AnimulusLib
{

	// REFERENCES
	private static AnimulusLib instance;
	private JavaPlugin plugin;

	private static NPCManager npcManager;


	// -------
	// CONSTRUCTOR
	// -------
	private AnimulusLib(JavaPlugin plugin)
	{
		this.plugin = plugin;
	}

	@API public static void enable(JavaPlugin plugin)
	{
		if(instance != null)
			return;

		instance = new AnimulusLib(plugin);
		instance.onEnable();
	}

	@API public static void disable()
	{
		if(instance == null)
			return;

		getInstance().onDisable();
		instance = null;
	}

	private void onEnable()
	{
		npcManager = new NPCManager();

		getLogger().info(this.getClass().getSimpleName()+" has been enabled");
	}

	private void onDisable()
	{
		if(npcManager != null)
			npcManager.terminate();

		getLogger().info(this.getClass().getSimpleName()+" has been disabled");
	}


	// -------
	// GETTERS
	// -------
	public static AnimulusLib getInstance()
	{
		if(instance == null)
			throw new IllegalArgumentException(AnimulusLib.class.getSimpleName()+" has to be initialized before usage");

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

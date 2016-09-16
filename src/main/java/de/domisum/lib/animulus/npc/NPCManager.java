package de.domisum.lib.animulus.npc;

import de.domisum.lib.animulus.AnimulusLib;
import de.domisum.lib.animulus.listener.NPCInteractPacketListener;
import de.domisum.lib.auxilium.util.bukkit.LocationUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NPCManager implements Listener
{

	// CONSTANTS
	private static final int MS_PER_TICK = 50;

	private static final int CHECK_PLAYER_DISTANCE_TICK_INTERVAL = 20;

	// REFERENCES
	private NPCInteractPacketListener npcInteractPacketListener;

	// STATUS
	private ScheduledFuture<?> updatingTask;
	private int updateCount;

	private Map<Integer, StateNPC> npcs = new ConcurrentHashMap<>(); // <entityId, npc>
	private List<StateNPC> npcsToRemove = new CopyOnWriteArrayList<>();

	/*private List<Long> lastNPCUpdateDurations = new ArrayList<>();*/


	// -------
	// CONSTRUCTOR
	// -------
	public NPCManager()
	{
		registerListener();

		startUpdatingTask();
	}

	private void registerListener()
	{
		this.npcInteractPacketListener = new NPCInteractPacketListener();

		JavaPlugin instance = AnimulusLib.getInstance().getPlugin();
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}

	public void terminate()
	{
		if(this.npcInteractPacketListener != null)
		{
			this.npcInteractPacketListener.terminate();
			this.npcInteractPacketListener = null;
		}

		stopUpdatingTask();

		terminateNPCs();
	}

	private void terminateNPCs()
	{
		for(StateNPC npc : this.npcs.values())
			npc.terminate();

		this.npcs.clear();
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public int getUpdateCount()
	{
		return this.updateCount;
	}

	public StateNPC getNPC(int entityId)
	{
		return this.npcs.get(entityId);
	}

	@APIUsage
	public StateNPC getNPC(String id)
	{
		for(StateNPC npc : this.npcs.values())
			if(npc.getId().equals(id))
				return npc;

		return null;
	}


	// -------
	// CHANGERS
	// -------
	@APIUsage
	public void addNPC(StateNPC npc)
	{
		// this sets the entity id, so do this first
		npc.initialize();

		this.npcs.put(npc.getEntityId(), npc);
	}

	@APIUsage
	public void removeNPC(StateNPC npc)
	{
		this.npcsToRemove.add(npc);
	}


	// -------
	// TICKING
	// -------
	private void startUpdatingTask()
	{
		Runnable run = ()->
		{
			try
			{
				update();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		};

		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		this.updatingTask = executor.scheduleWithFixedDelay(run, 0, MS_PER_TICK, TimeUnit.MILLISECONDS);
	}

	private void stopUpdatingTask()
	{
		this.updatingTask.cancel(true);
		this.updatingTask = null;
	}


	private void update()
	{
		/*long startNano = System.nanoTime();*/

		for(StateNPC toRemove : this.npcsToRemove)
			this.npcs.values().remove(toRemove);
		this.npcsToRemove.clear();

		for(StateNPC npc : this.npcs.values())
		{
			if((this.updateCount%CHECK_PLAYER_DISTANCE_TICK_INTERVAL) == 0)
				npc.updateVisibleForPlayers();

			if(npc.isVisibleToSomebody())
				if(LocationUtil.isChunkLoaded(npc.getLocation()))
					npc.update();
		}

		this.updateCount++;

		// benchmarking
		/*long endNano = System.nanoTime();
		long durationNano = endNano-startNano;
		this.lastNPCUpdateDurations.add(durationNano);
		if(this.lastNPCUpdateDurations.size() > 20*60)
			this.lastNPCUpdateDurations.remove(0);*/
	}


	// -------
	// EVENTS
	// -------
	@EventHandler
	public void playerJoin(PlayerQuitEvent event)
	{
		for(StateNPC npc : this.npcs.values())
			npc.updateVisibilityForPlayer(event.getPlayer());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();

		for(StateNPC npc : this.npcs.values())
			npc.becomeInvisibleFor(player, false);
	}


	@EventHandler
	public void playerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		// this is needed since the world is sent anew when the player respawns
		// delay because this event is called before the respawn and the location is not right

		Runnable run = ()->
		{
			for(StateNPC npc : this.npcs.values())
			{
				npc.becomeInvisibleFor(player, true);
				npc.updateVisibilityForPlayer((player));
			}
		};

		Bukkit.getScheduler().runTaskLater(AnimulusLib.getInstance().getPlugin(), run, 1);
	}

}

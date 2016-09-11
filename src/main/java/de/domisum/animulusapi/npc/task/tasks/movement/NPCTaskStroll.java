package de.domisum.animulusapi.npc.task.tasks.movement;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.math.RandomUtil;
import org.bukkit.Location;

@APIUsage
public class NPCTaskStroll extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.MOVEMENT, NPCTaskSlot.HEAD_ROTATION};

	// PROPERTIES
	private int durationTicks;

	// REFERENCES
	private Location startLocation;
	private NPCTaskWalkTo walkTask;

	// STATUS
	private int ticksToWait = 0;
	private int ticksStrolled = 0;
	private Location currentTarget;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NPCTaskStroll(int durationTicks)
	{
		super();

		this.durationTicks = durationTicks;
	}


	// -------
	// GETTERS
	// -------
	@Override
	public NPCTaskSlot[] USED_TASK_SLOTS()
	{
		return USED_TASK_SLOTS;
	}


	// -------
	// EXECUTION
	// -------
	@Override
	protected void onStart()
	{
		startLocation = npc.getLocation();
	}

	@Override
	protected boolean onUpdate()
	{
		if(durationTicks > 0) // if duration <= 0, then do this forever
			if(ticksStrolled >= durationTicks)
			{
				if(walkTask != null)
					walkTask.onCancel();

				return true;
			}
		ticksStrolled++;

		if(ticksToWait > 0)
		{
			ticksToWait--;
			return false;
		}

		if(currentTarget == null)
		{
			currentTarget = findStrollLocation(startLocation, 7);
			if(currentTarget == null)
				return false;

			walkTask = new NPCTaskWalkTo(currentTarget, 0.9);
			walkTask.initialize(npc);
			walkTask.onStart();

			return false;
		}

		boolean arrived = walkTask.onUpdate();
		if(arrived)
		{
			walkTask = null;
			currentTarget = null;

			ticksToWait = RandomUtil.distribute(4*20, 2*20);
		}

		return false;
	}

	@Override
	protected void onCancel()
	{

	}


	// -------
	// UTIL
	// -------
	private Location findStrollLocation(Location base, double radius)
	{
		// 5 tries to find a location
		for(int i = 0; i < 5; i++)
		{
			double randomAngle = RandomUtil.nextDouble()*2*Math.PI;
			double randomDistance = RandomUtil.nextDouble()*radius;
			double dX = Math.sin(randomAngle)*randomDistance;
			double dZ = Math.cos(randomAngle)*randomDistance;

			for(int dY : new int[] {0, 1, -1, 2, -2})
			{
				Location location = base.clone().add(Math.round(dX), Math.round(dY), dZ);

				if(canStandAt(location))
					return location;
			}
		}

		return null;
	}

	private boolean canStandAt(Location location)
	{
		if(location.getBlock().getType().isSolid())
			return false;

		if(location.clone().add(0, 1, 0).getBlock().getType().isSolid())
			return false;

		if(!location.clone().add(0, -1, 0).getBlock().getType().isSolid())
			return false;

		return true;
	}

}

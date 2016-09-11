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
		this.startLocation = this.npc.getLocation();
	}

	@Override
	protected boolean onUpdate()
	{
		if(this.durationTicks > 0) // if duration <= 0, then do this forever
			if(this.ticksStrolled >= this.durationTicks)
			{
				if(this.walkTask != null)
					this.walkTask.onCancel();

				return true;
			}
		this.ticksStrolled++;

		if(this.ticksToWait > 0)
		{
			this.ticksToWait--;
			return false;
		}

		if(this.currentTarget == null)
		{
			this.currentTarget = findStrollLocation(this.startLocation, 7);
			if(this.currentTarget == null)
				return false;

			this.walkTask = new NPCTaskWalkTo(this.currentTarget, 0.9);
			this.walkTask.initialize(this.npc);
			this.walkTask.onStart();

			return false;
		}

		boolean arrived = this.walkTask.onUpdate();
		if(arrived)
		{
			this.walkTask = null;
			this.currentTarget = null;

			this.ticksToWait = RandomUtil.distribute(4*20, 3*20);
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

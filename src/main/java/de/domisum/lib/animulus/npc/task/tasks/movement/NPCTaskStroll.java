package de.domisum.lib.animulus.npc.task.tasks.movement;

import de.domisum.lib.animulus.npc.task.NPCTask;
import de.domisum.lib.animulus.npc.task.NPCTaskSlot;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.math.RandomUtil;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalBlockPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import de.domisum.lib.compitum.transitionalpath.pathfinders.TransitionalPathSmoother;
import de.domisum.lib.compitum.transitionalpath.special.TransitionalStrollAStar;
import org.bukkit.Location;

@APIUsage
public class NPCTaskStroll extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.MOVEMENT, NPCTaskSlot.HEAD_ROTATION};
	private static final double MAX_STROLL_DISTANCE = 7;

	// PROPERTIES
	private int durationTicks;

	// REFERENCES
	private Location startLocation;
	private NPCTaskWalkTo walkTask;

	// STATUS
	private int ticksToWait = 0;
	private int ticksStrolled = 0;
	private TransitionalPath currentPath;


	// CONSTRUCTOR
	@APIUsage public NPCTaskStroll(int durationTicks)
	{
		super();

		this.durationTicks = durationTicks;
	}


	// GETTERS
	@Override public NPCTaskSlot[] USED_TASK_SLOTS()
	{
		return USED_TASK_SLOTS;
	}


	// EXECUTION
	@Override protected void onStart()
	{
		this.startLocation = this.npc.getLocation();
	}

	@Override protected boolean onUpdate()
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

		if(this.currentPath == null)
		{
			findStrollPath();
			if(this.currentPath == null)
			{
				this.ticksToWait = 10;
				return false;
			}

			this.walkTask = new NPCTaskWalkTo(this.currentPath, 0.9);
			this.walkTask.initialize(this.npc);
			this.walkTask.onStart();

			return false;
		}

		boolean arrived = this.walkTask.onUpdate();
		if(arrived)
		{
			this.walkTask = null;
			this.currentPath = null;

			this.ticksToWait = RandomUtil.distribute(4*20, 3*20);
		}

		return false;
	}

	@Override protected void onCancel()
	{
		// nothing that needs to be canceled
	}

	private void findStrollPath()
	{
		TransitionalStrollAStar strollAStar = new TransitionalStrollAStar(this.npc.getLocation(), this.startLocation,
				MAX_STROLL_DISTANCE);
		strollAStar.findPath();

		if(!strollAStar.pathFound())
			return;
		TransitionalBlockPath blockPath = strollAStar.getPath();

		TransitionalPathSmoother smoother = new TransitionalPathSmoother(blockPath);
		smoother.convert();
		this.currentPath = smoother.getSmoothPath();
	}

}

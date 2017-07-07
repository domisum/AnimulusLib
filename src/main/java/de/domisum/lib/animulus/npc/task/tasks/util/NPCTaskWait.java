package de.domisum.lib.animulus.npc.task.tasks.util;

import de.domisum.lib.animulus.npc.task.NPCTask;
import de.domisum.lib.animulus.npc.task.NPCTaskSlot;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;

@APIUsage
public class NPCTaskWait extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {};

	// PROPERTIES
	private int durationTicks;

	// STATUS
	private int ticksWaited = 0;


	// INIT
	@APIUsage public NPCTaskWait(int durationTicks)
	{
		super();
		this.durationTicks = durationTicks;
	}


	// GETTERS
	@Override public NPCTaskSlot[] getUsedTaskSlots()
	{
		return USED_TASK_SLOTS;
	}

	@Override protected boolean isRunSeparately()
	{
		return true;
	}


	// EXECUTION
	@Override protected void onStart()
	{
		// nothing to start
	}

	@Override protected boolean onUpdate()
	{
		if(this.ticksWaited >= this.durationTicks)
			return true;

		this.ticksWaited++;
		return false;
	}


	@Override protected void onCancel()
	{
		// nothing to cancel
	}

}

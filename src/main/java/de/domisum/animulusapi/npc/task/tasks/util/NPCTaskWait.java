package de.domisum.animulusapi.npc.task.tasks.util;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;

@APIUsage
public class NPCTaskWait extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {};

	// PROPERTIES
	private int durationTicks;

	// STATUS
	private int ticksWaited = 0;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NPCTaskWait(int durationTicks)
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

	@Override
	protected boolean isRunSeparately()
	{
		return true;
	}


	// -------
	// EXECUTION
	// -------
	@Override
	protected void onStart()
	{

	}

	@Override
	protected boolean onUpdate()
	{
		if(this.ticksWaited >= this.durationTicks)
			return false;

		this.ticksWaited++;
		return true;
	}


	@Override
	protected void onCancel()
	{

	}

}

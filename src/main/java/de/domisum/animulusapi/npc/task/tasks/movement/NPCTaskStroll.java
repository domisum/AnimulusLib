package de.domisum.animulusapi.npc.task.tasks.movement;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;

@APIUsage
public class NPCTaskStroll extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.MOVEMENT, NPCTaskSlot.HEAD_ROTATION};

	// PROPERTIES
	private int durationTicks;
	private double speedMultiplier;

	// STATUS
	private int ticksStrolled = 0;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NPCTaskStroll(int durationTicks)
	{
		this(durationTicks, 1);
	}

	@APIUsage
	public NPCTaskStroll(int durationTicks, double speedMultiplier)
	{
		super();

		this.speedMultiplier = speedMultiplier;
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

	}

	@Override
	protected boolean onUpdate()
	{
		// TODO implement this
		if(Math.random() < 0.05)
			return false;

		return true;
	}

	@Override
	protected void onCancel()
	{

	}

}

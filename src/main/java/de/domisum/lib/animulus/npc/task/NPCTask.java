package de.domisum.lib.animulus.npc.task;

import de.domisum.lib.auxilium.util.java.annotations.API;
import de.domisum.lib.auxilium.util.java.annotations.DeserializationNoArgsConstructor;

public abstract class NPCTask
{

	// PROPERTIES
	private boolean runSeparately = false;

	// REFERENCES
	protected TaskNPC npc;

	// STATUS
	private boolean canceled;


	// INIT
	@DeserializationNoArgsConstructor public NPCTask()
	{

	}

	public void initialize(TaskNPC npc)
	{
		this.npc = npc;
	}


	// GETTERS
	public abstract NPCTaskSlot[] getUsedTaskSlots();

	protected boolean isRunSeparately()
	{
		return this.runSeparately;
	}

	protected boolean isCanceled()
	{
		return this.canceled;
	}


	// SETTERS
	@API public NPCTask setRunSeparately(boolean runSeparately)
	{
		this.runSeparately = runSeparately;
		return this;
	}


	// EXECUTION
	protected abstract void onStart();

	/**
	 * @return true if the task is finished, false if not
	 */
	protected abstract boolean onUpdate();

	protected abstract void onCancel();

	@API public void cancel()
	{
		onCancel();
		this.canceled = true;
	}

}

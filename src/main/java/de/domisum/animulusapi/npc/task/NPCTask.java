package de.domisum.animulusapi.npc.task;

import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;

public abstract class NPCTask
{

	// PROPERTIES
	private boolean runSeparately = false;

	// REFERENCES
	protected TaskNPC npc;

	// STATUS
	private boolean canceled;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public NPCTask()
	{

	}

	void initialize(TaskNPC npc)
	{
		this.npc = npc;
	}


	// -------
	// GETTERS
	// -------
	public abstract NPCTaskSlot[] USED_TASK_SLOTS();

	boolean isRunSeparately()
	{
		return this.runSeparately;
	}

	boolean isCanceled()
	{
		return this.canceled;
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public void setRunSeparately(boolean runSeparately)
	{
		this.runSeparately = runSeparately;
	}


	// -------
	// EXECUTION
	// -------
	protected abstract void onStart();

	/**
	 * @return true if the task is finished, false if not
	 */
	protected abstract boolean onUpdate();

	protected abstract void onCancel();

	@APIUsage
	public void cancel()
	{
		onCancel();
		this.canceled = true;
	}

}

package de.domisum.lib.animulus.npc.ai;

import de.domisum.lib.animulus.npc.task.TaskNPC;

public interface NPCBrain
{

	// -------
	// CONSTRUCTOR
	// -------
	void initialize(TaskNPC npc);


	// -------
	// UPDATING
	// -------
	void update();

}

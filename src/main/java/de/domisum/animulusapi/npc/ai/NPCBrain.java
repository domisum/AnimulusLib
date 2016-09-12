package de.domisum.animulusapi.npc.ai;

import de.domisum.animulusapi.npc.task.TaskNPC;

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

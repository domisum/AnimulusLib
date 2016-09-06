package de.domisum.animulusapi.npc.task.tasks.item;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import org.bukkit.inventory.ItemStack;

@APIUsage
public class NPCTaskEatItem extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.MOUTH, NPCTaskSlot.ITEM_MAINHAND};

	// PROPERTIES
	private ItemStack itemStack;
	private int durationTicks;

	// STATUS
	private int elapsedTicks = 0;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NPCTaskEatItem(ItemStack itemStack)
	{
		this(itemStack, 30);
	}

	@APIUsage
	public NPCTaskEatItem(ItemStack itemStack, int durationTicks)
	{
		super();

		this.itemStack = itemStack;
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
		this.npc.setItemInHand(this.itemStack);
		this.npc.setEating(true);
	}

	@Override
	protected boolean onUpdate()
	{
		if(this.elapsedTicks >= this.durationTicks)
		{
			end();
			return false;
		}

		this.elapsedTicks++;
		return true;
	}

	@Override
	protected void onCancel()
	{
		end();
	}


	private void end()
	{
		this.npc.setEating(false);
		this.npc.setItemInHand(null);
	}

}

package de.domisum.lib.animulus.npc.task.tasks.item;

import de.domisum.lib.animulus.npc.task.NPCTask;
import de.domisum.lib.animulus.npc.task.NPCTaskSlot;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import org.bukkit.Location;
import org.bukkit.Sound;
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
	@APIUsage public NPCTaskEatItem(ItemStack itemStack)
	{
		this(itemStack, 32);
	}

	@APIUsage public NPCTaskEatItem(ItemStack itemStack, int durationTicks)
	{
		super();

		this.itemStack = itemStack;
		this.durationTicks = durationTicks;
	}


	// -------
	// GETTERS
	// -------
	@Override public NPCTaskSlot[] getUsedTaskSlots()
	{
		return USED_TASK_SLOTS;
	}


	// -------
	// EXECUTION
	// -------
	@Override protected void onStart()
	{
		this.npc.setItemInHand(this.itemStack);
		this.npc.setEating(true);
	}

	@Override protected boolean onUpdate()
	{
		if(this.elapsedTicks >= this.durationTicks)
		{
			end();
			playSound(Sound.ENTITY_PLAYER_BURP);
			return true;
		}

		if(this.elapsedTicks%4 == 0)
			playSound(Sound.ENTITY_GENERIC_EAT);

		this.elapsedTicks++;
		return false;
	}

	private void playSound(Sound sound)
	{
		Location soundLocation = this.npc.getEyeLocation();
		soundLocation.getWorld().playSound(soundLocation, sound, 2, 1);
	}

	@Override protected void onCancel()
	{
		end();
	}


	private void end()
	{
		this.npc.setEating(false);
		this.npc.setItemInHand(null);
	}

}

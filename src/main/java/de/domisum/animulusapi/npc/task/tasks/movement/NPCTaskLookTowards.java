package de.domisum.animulusapi.npc.task.tasks.movement;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import org.bukkit.Location;

@APIUsage
public class NPCTaskLookTowards extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.HEAD_ROTATION};

	private static final float TOLERANCE = 1;
	private static final float BASE_SPEED = 10; // degrees per tick

	// PROPERTIES
	private float targetYaw;
	private float targetPitch;
	private double speedMultiplier;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NPCTaskLookTowards(float targetYaw, float targetPitch)
	{
		this(targetYaw, targetPitch, 1);
	}

	@APIUsage
	public NPCTaskLookTowards(float targetYaw, float targetPitch, double speedMultiplier)
	{
		super();

		this.targetYaw = (targetYaw%360+360)%360;
		this.targetPitch = targetPitch;
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
		Location currentLocation = this.npc.getLocation();

		float dYaw = (this.targetYaw-currentLocation.getYaw())%360;
		if(dYaw < 0)
			dYaw += 360;
		if(dYaw > 180)
			dYaw -= 360;

		float dPitch = this.targetPitch-currentLocation.getPitch();

		if(Math.abs(dYaw)+Math.abs(dPitch) < TOLERANCE)
			return false;

		float stepDYaw = getClampedDelta(dYaw);
		float stepDPitch = getClampedDelta(dPitch);

		this.npc.setYawPitch(currentLocation.getYaw()+stepDYaw, currentLocation.getPitch()+stepDPitch);
		return true;
	}

	private float getClampedDelta(float value)
	{
		return (float) ((value < 0 ? -1 : 1)*Math.min(Math.abs(value), BASE_SPEED*this.speedMultiplier));
	}


	@Override
	protected void onCancel()
	{

	}

}

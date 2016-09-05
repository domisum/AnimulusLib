package de.domisum.animulusapi.npc.task.tasks;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.math.MathUtil;
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

	// STATUS
	private int yawRotationDirection = 0;


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
		float dYaw = (this.targetYaw-this.npc.getLocation().getYaw()+360f)%360f;

		if(dYaw < 180)
			this.yawRotationDirection = 1;
		else
			this.yawRotationDirection = -1;

		/*if(dYaw < 0)
			throw new IllegalStateException(
					"dYaw has illegal value: "+dYaw+" locationYaw: "+this.npc.getLocation().getYaw()+" targetYaw: "
							+this.targetYaw);*/
	}

	@Override
	protected boolean onUpdate()
	{
		Location currentLocation = this.npc.getLocation();

		float dYaw = ((this.targetYaw-currentLocation.getYaw())%360f+360f)%360f;
		if(dYaw > 180)
			dYaw -= 360;
		float dPitch = this.targetPitch-currentLocation.getPitch();

		if(Math.abs(dYaw)+Math.abs(dPitch) < TOLERANCE)
			return false;

		float stepDYaw = (dPitch < 0 ? -1 : 1)*(float) MathUtil.minAbs(BASE_SPEED*this.speedMultiplier, dYaw);
		float stepDPitch = (dPitch < 0 ? -1 : 1)*(float) Math.min(BASE_SPEED*this.speedMultiplier, Math.abs(dPitch));

		this.npc.setYawPitch(currentLocation.getYaw()+stepDYaw, currentLocation.getPitch()+stepDPitch);
		return true;
	}

	@Override
	protected void onCancel()
	{

	}

}

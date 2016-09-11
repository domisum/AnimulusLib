package de.domisum.animulusapi.npc.task.tasks.movement;

import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.data.container.Duo;
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
		Duo<Float, Float> stepYawAndPitch = getStepYawAndPitch(currentLocation, this.targetYaw, this.targetPitch,
				BASE_SPEED*this.speedMultiplier);


		if(Math.abs(stepYawAndPitch.a)+Math.abs(stepYawAndPitch.b) < TOLERANCE)
			return false;


		this.npc.setYawPitch(currentLocation.getYaw()+stepYawAndPitch.a, currentLocation.getPitch()+stepYawAndPitch.b);
		return true;
	}

	@Override
	protected void onCancel()
	{

	}


	// -------
	// UTIL
	// -------
	@APIUsage
	public static Duo<Float, Float> getStepYawAndPitch(Location currentLocation, float targetYaw, float targetPitch, double speed)
	{
		float dYaw = (targetYaw-currentLocation.getYaw())%360;
		if(dYaw < 0)
			dYaw += 360;
		if(dYaw > 180)
			dYaw -= 360;

		float dPitch = targetPitch-currentLocation.getPitch();

		float stepDYaw = (float) MathUtil.clampAbs(dYaw, speed);
		float stepDPitch = (float) MathUtil.clampAbs(dPitch, speed);
		return new Duo<>(stepDYaw, stepDPitch);
	}

}

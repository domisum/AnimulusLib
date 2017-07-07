package de.domisum.lib.animulus.npc.task.tasks.movement;

import de.domisum.lib.animulus.npc.task.NPCTask;
import de.domisum.lib.animulus.npc.task.NPCTaskSlot;
import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.math.MathUtil;
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


	// INIT
	@APIUsage public NPCTaskLookTowards(float targetYaw, float targetPitch)
	{
		this(targetYaw, targetPitch, 1);
	}

	@APIUsage public NPCTaskLookTowards(float targetYaw, float targetPitch, double speedMultiplier)
	{
		super();

		this.targetYaw = (targetYaw%360+360)%360;
		this.targetPitch = targetPitch;
		this.speedMultiplier = speedMultiplier;
	}


	// GETTERS
	@Override public NPCTaskSlot[] getUsedTaskSlots()
	{
		return USED_TASK_SLOTS;
	}


	// EXECUTION
	@Override protected void onStart()
	{
		// nothing needs to be started
	}

	@Override protected boolean onUpdate()
	{
		Location currentLocation = this.npc.getLocation();
		Duo<Float, Float> stepYawAndPitch = getStepYawAndPitch(currentLocation, this.targetYaw, this.targetPitch,
				BASE_SPEED*this.speedMultiplier);


		if(Math.abs(stepYawAndPitch.a)+Math.abs(stepYawAndPitch.b) < TOLERANCE)
			return true;


		this.npc.setYawPitch(currentLocation.getYaw()+stepYawAndPitch.a, currentLocation.getPitch()+stepYawAndPitch.b);
		return false;
	}

	@Override protected void onCancel()
	{
		// nothing needs to be canceled
	}


	// UTIL
	@APIUsage public static Duo<Float, Float> getStepYawAndPitch(Location currentLocation, float targetYaw, float targetPitch,
			double speed)
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

	@APIUsage
	public static Duo<Float, Float> getStepYawAndPitchSmooth(Location currentLocation, float targetYaw, float targetPitch,
			double speedMultiplier)
	{
		float dYaw = (targetYaw-currentLocation.getYaw())%360;
		if(dYaw < 0)
			dYaw += 360;
		if(dYaw > 180)
			dYaw -= 360;

		float dPitch = targetPitch-currentLocation.getPitch();

		float stepDYaw = (float) (smoothFormula(dYaw)*speedMultiplier);
		float stepDPitch = (float) (smoothFormula(dPitch)*speedMultiplier);
		return new Duo<>(stepDYaw, stepDPitch);
	}

	private static float smoothFormula(float degrees)
	{
		float unsigned = (float) Math.pow(Math.abs(degrees)/180, 0.6)*15;
		float value = (degrees < 0 ? -1 : 1)*unsigned;

		return (float) MathUtil.clampAbs(value, Math.abs(degrees));
	}

}

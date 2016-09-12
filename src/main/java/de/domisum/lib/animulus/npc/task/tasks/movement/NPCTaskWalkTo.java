package de.domisum.lib.animulus.npc.task.tasks.movement;

import de.domisum.lib.animulus.AnimulusLib;
import de.domisum.lib.animulus.npc.task.NPCTask;
import de.domisum.lib.animulus.npc.task.NPCTaskSlot;
import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.data.container.math.Vector2D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.TextUtil;
import de.domisum.lib.auxilium.util.bukkit.LocationUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.math.MathUtil;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import de.domisum.lib.compitum.universal.UniversalPathfinder;
import org.bukkit.Location;

@APIUsage
public class NPCTaskWalkTo extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.MOVEMENT, NPCTaskSlot.HEAD_ROTATION};

	// PROPERTIES
	private Location target;
	private double speedMultiplier;

	// STATUS
	private TransitionalPath path;
	private int currentWaypointIndex = 0;


	// -------
	// CONSTRUCTOR
	// -------
	@APIUsage
	public NPCTaskWalkTo(Location target)
	{
		this(target, 1);
	}

	@APIUsage
	public NPCTaskWalkTo(Location target, double speedMultiplier)
	{
		super();

		this.target = target;
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
		Location start = this.npc.getLocation();

		UniversalPathfinder pathfinder = new UniversalPathfinder(start, target);
		pathfinder.findPath();
		path = pathfinder.getPath();

		if(this.path == null)
		{
			this.npc.onWalkingFail();
			AnimulusLib.getInstance().getLogger().warning(
					npc.getId()+": No path was found from "+TextUtil.getLocationAsString(start)+" to "+TextUtil
							.getLocationAsString(this.target));
			AnimulusLib.getInstance().getLogger().warning("Pathfinder Data: "+pathfinder.getDiagnose());
			if(pathfinder.getError() != null)
				AnimulusLib.getInstance().getLogger().severe("Error: '"+pathfinder.getError()+"'");

			this.cancel();
		}
	}

	@Override
	protected boolean onUpdate()
	{
		if(path == null)
			return true;

		if(this.currentWaypointIndex >= this.path.getNumberOfWaypoints())
			return true;

		Location loc = this.npc.getLocation();
		Duo<Vector3D, Integer> currentWaypoint = this.path.getWaypoint(this.currentWaypointIndex);

		double dX = currentWaypoint.a.x-loc.getX();
		double dY = currentWaypoint.a.y-loc.getY();
		double dZ = currentWaypoint.a.z-loc.getZ();

		if(dX*dX+dZ*dZ < 0.1)
		{
			this.currentWaypointIndex++;
			return false;
		}

		if(dY > 0 && currentWaypoint.b == TransitionType.JUMP)
			this.npc.jump();

		double speed = this.npc.getWalkSpeed()*this.speedMultiplier;
		double stepX = (dX < 0 ? -1 : 1)*Math.min(Math.abs(dX), speed);
		double stepZ = (dZ < 0 ? -1 : 1)*Math.min(Math.abs(dZ), speed);

		Vector2D mov = new Vector2D(stepX, stepZ);
		double movLength = mov.length();
		if(movLength > speed)
			mov = mov.multiply(speed/movLength);

		// inair acceleration is not that good
		if(!this.npc.isOnGround())
			mov = mov.multiply(0.3);

		this.npc.setVelocity(new Vector3D(mov.x, this.npc.getVelocity().y, mov.y));


		// HEAD ROTATION
		Location waypointLocation = new Location(loc.getWorld(), currentWaypoint.a.x, currentWaypoint.a.y, currentWaypoint.a.z);
		Location directionLoc = LocationUtil.lookAt(loc, waypointLocation);

		float targetYaw = directionLoc.getYaw();
		float targetPitch = directionLoc.getPitch();
		targetPitch = (float) MathUtil.clampAbs(targetPitch, 25);

		Duo<Float, Float> stepYawAndPitch = NPCTaskLookTowards.getStepYawAndPitch(loc, targetYaw, targetPitch, 10);
		this.npc.setYawPitch(loc.getYaw()+stepYawAndPitch.a, loc.getPitch()+stepYawAndPitch.b);


		return false;
	}

	@Override
	protected void onCancel()
	{

	}

}

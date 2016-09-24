package de.domisum.lib.animulus.npc.task.tasks.movement;

import de.domisum.lib.animulus.AnimulusLib;
import de.domisum.lib.animulus.npc.PhysicsNPC;
import de.domisum.lib.animulus.npc.task.NPCTask;
import de.domisum.lib.animulus.npc.task.NPCTaskSlot;
import de.domisum.lib.auxilium.data.container.Duo;
import de.domisum.lib.auxilium.data.container.math.Vector2D;
import de.domisum.lib.auxilium.data.container.math.Vector3D;
import de.domisum.lib.auxilium.util.TextUtil;
import de.domisum.lib.auxilium.util.bukkit.LocationUtil;
import de.domisum.lib.auxilium.util.java.annotations.APIUsage;
import de.domisum.lib.auxilium.util.java.debug.DebugUtil;
import de.domisum.lib.auxilium.util.math.MathUtil;
import de.domisum.lib.compitum.transitionalpath.node.TransitionType;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalPath;
import de.domisum.lib.compitum.transitionalpath.path.TransitionalWaypoint;
import de.domisum.lib.compitum.universal.UniversalPathfinder;
import org.bukkit.Location;

@APIUsage
public class NPCTaskWalkTo extends NPCTask
{

	// CONSTANTS
	private static NPCTaskSlot[] USED_TASK_SLOTS = new NPCTaskSlot[] {NPCTaskSlot.MOVEMENT, NPCTaskSlot.HEAD_ROTATION};
	private static final double NO_MOVEMENT_THRESHOLD = 0.01;
	private static final int NO_MOVEMENT_STUCK_REPETITIONS = 20;

	// PROPERTIES
	private Location target;
	private double speedMultiplier;

	// STATUS
	private TransitionalPath path;
	private int currentWaypointIndex = 0;
	private TransitionalWaypoint currentWaypoint;

	private Vector3D lastPosition;
	private int unchangedPositionsInRow = 0;

	// walking
	private int reuseLastDirectionTicks = 0;
	private Vector2D lastDirection;


	// TODO stepping sounds


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

		UniversalPathfinder pathfinder = new UniversalPathfinder(start, this.target);
		pathfinder.findPath();
		this.path = pathfinder.getPath();

		if(this.path == null)
		{
			this.npc.onWalkingFail();
			AnimulusLib.getInstance().getLogger().warning(
					this.npc.getId()+": No path was found from "+TextUtil.getLocationAsString(start)+" to "+TextUtil
							.getLocationAsString(this.target));
			AnimulusLib.getInstance().getLogger().warning("Pathfinder Data: "+pathfinder.getDiagnose());
			if(pathfinder.getFailure() != null)
				AnimulusLib.getInstance().getLogger().severe("Error: '"+pathfinder.getFailure()+"'");

			this.cancel();
		}
	}

	@Override
	protected boolean onUpdate()
	{
		if(this.path == null)
			return true;

		if(this.currentWaypointIndex >= this.path.getNumberOfWaypoints())
			return true;

		if(this.lastPosition != null)
			if(this.npc.getPosition().subtract(this.lastPosition).lengthSquared() < NO_MOVEMENT_THRESHOLD)
			{
				this.unchangedPositionsInRow++;
				if(this.unchangedPositionsInRow >= NO_MOVEMENT_STUCK_REPETITIONS)
				{
					this.npc.onWalkingFail();
					return true;
				}
			}


		this.currentWaypoint = this.path.getWaypoint(this.currentWaypointIndex);
		int transitionType = this.currentWaypoint.getTransitionType();
		if(transitionType == TransitionType.WALK || transitionType == TransitionType.JUMP)
			walk();
		else if(transitionType == TransitionType.CLIMB)
			climb();
		else
			throw new UnsupportedOperationException("The TransitionType '"+transitionType+"' is not supported");


		this.lastPosition = this.npc.getPosition();
		return false;
	}

	@Override
	protected void onCancel()
	{

	}


	// -------
	// MOVEMENT TYPES
	// -------
	private void walk()
	{
		Location loc = this.npc.getLocation();

		double dX = this.currentWaypoint.getPosition().x-loc.getX();
		double dY = this.currentWaypoint.getPosition().y-loc.getY();
		double dZ = this.currentWaypoint.getPosition().z-loc.getZ();

		double distanceXZSquared = dX*dX+dZ*dZ;

		if(distanceXZSquared < 0.01)
		{
			this.currentWaypointIndex++;
			this.reuseLastDirectionTicks = 2;
			return;
		}

		if(this.currentWaypointIndex+1 == this.path.getNumberOfWaypoints())
			if(distanceXZSquared < 0.2)
			{
				this.currentWaypointIndex++;
				return;
			}

		if(dY > 0 && this.currentWaypoint.getTransitionType() == TransitionType.JUMP)
			this.npc.jump();

		double speed = this.npc.getWalkSpeed()*this.speedMultiplier;

		Vector2D mov = new Vector2D(dX, dZ);
		double movLength = mov.length();
		if(this.reuseLastDirectionTicks > 0)
		{
			mov = this.lastDirection.multiply(movLength);
			this.reuseLastDirectionTicks--;
		}
		Vector2D direction = mov.divide(movLength);
		if(movLength > speed)
			mov = mov.multiply(speed/movLength);

		// inair acceleration is not that good
		if(!this.npc.isOnGround())
			mov = mov.multiply(0.3);

		this.npc.setVelocity(new Vector3D(mov.x, this.npc.getVelocity().y, mov.y));

		this.lastDirection = direction;


		// HEAD ROTATION
		Location waypointLocation = new Location(loc.getWorld(), this.currentWaypoint.getPosition().x,
				this.currentWaypoint.getPosition().y, this.currentWaypoint.getPosition().z);
		Location directionLoc = LocationUtil.lookAt(loc, waypointLocation);

		float targetYaw = directionLoc.getYaw();
		float targetPitch = directionLoc.getPitch();
		targetPitch = (float) MathUtil.clampAbs(targetPitch, 25);

		Duo<Float, Float> stepYawAndPitch = NPCTaskLookTowards.getStepYawAndPitch(loc, targetYaw, targetPitch, 10);
		this.npc.setYawPitch(loc.getYaw()+stepYawAndPitch.a, loc.getPitch()+stepYawAndPitch.b);
	}

	private void climb()
	{
		Location location = this.npc.getLocation();

		double dY = this.currentWaypoint.getPosition().y-location.getY();

		double stepY = MathUtil.clampAbs(dY, PhysicsNPC.CLIMBING_BLOCKS_PER_SECOND/20d);
		this.npc.setVelocity(new Vector3D(0, stepY, 0));
		DebugUtil.say("stepY: "+stepY);

		if(Math.abs(dY) < 0.1)
		{
			this.currentWaypoint = this.path.getWaypoint(this.currentWaypointIndex+1);
			DebugUtil.say("walk");
			walk();
		}
	}

}

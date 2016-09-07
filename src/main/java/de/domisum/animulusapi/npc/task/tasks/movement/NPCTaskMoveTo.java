package de.domisum.animulusapi.npc.task.tasks.movement;

import de.domisum.animulusapi.AnimulusAPI;
import de.domisum.animulusapi.npc.task.NPCTask;
import de.domisum.animulusapi.npc.task.NPCTaskSlot;
import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.compitumapi.CompitumAPI;
import de.domisum.compitumapi.transitionalpath.path.TransitionalPath;
import org.bukkit.Location;

@APIUsage
public class NPCTaskMoveTo extends NPCTask
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
	public NPCTaskMoveTo(Location target)
	{
		this(target, 1);
	}

	@APIUsage
	public NPCTaskMoveTo(Location target, double speedMultiplier)
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
		this.path = CompitumAPI.findPlayerPath(start, this.target);
		if(this.path == null)
		{
			AnimulusAPI.getInstance().getLogger().severe("Failed pathfinding from '"+start+"' to '"+this.target+"'");
			this.cancel();
		}
	}

	@Override
	protected boolean onUpdate()
	{
		if(this.currentWaypointIndex >= this.path.getNumberOfWaypoints())
			return false;

		Location loc = this.npc.getLocation();
		Duo<Vector3D, Integer> currentWaypoint = this.path.getWaypoint(this.currentWaypointIndex);

		double dX = currentWaypoint.a.x-loc.getX();
		double dY = currentWaypoint.a.y-loc.getY();
		double dZ = currentWaypoint.a.z-loc.getZ();

		if(dX*dX+dZ*dZ < 1)
		{
			this.currentWaypointIndex++;
			return true;
		}

		if(dY > 0.6 && this.npc.isOnGround())
			this.npc.jump();

		double stepX = (dX < 0 ? -1 : 1)*Math.min(Math.abs(dX), this.npc.getWalkSpeed()*this.speedMultiplier);
		double stepZ = (dZ < 0 ? -1 : 1)*Math.min(Math.abs(dZ), this.npc.getWalkSpeed()*this.speedMultiplier);

		Location targetLocation = loc.clone().add(stepX, 0, stepZ);
		this.npc.moveToNearby(targetLocation);
		return true;
	}

	@Override
	protected void onCancel()
	{

	}

}

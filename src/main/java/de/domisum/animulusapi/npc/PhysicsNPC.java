package de.domisum.animulusapi.npc;

import com.mojang.authlib.GameProfile;
import de.domisum.auxiliumapi.data.container.math.Vector3D;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;

import java.util.List;

@APIUsage
public class PhysicsNPC extends StateNPC
{

	// CONSTANTS
	private static final double ACCELERATION = 0.981;
	private static final double AABB_XZ_LENGTH = 0.6;
	private static final double AABB_Y_LENGTH = 1.8;

	// PROPERTIES
	private Vector3D movement = new Vector3D();
	private boolean onGround = false;

	// REFERENCES
	private AxisAlignedBB baseAABB = new AxisAlignedBB(-AABB_XZ_LENGTH/2d, 0, -AABB_XZ_LENGTH/2d, AABB_XZ_LENGTH/2d,
			AABB_Y_LENGTH, AABB_XZ_LENGTH/2d);


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public PhysicsNPC()
	{
		super();
	}

	@APIUsage
	public PhysicsNPC(GameProfile gameProfile, Location location)
	{
		super(gameProfile, location);
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public boolean isOnGround()
	{
		return this.onGround;
	}


	// -------
	// UPDATING
	// -------
	@Override
	public void update()
	{
		super.update();

		applyPhysics();
	}

	private void applyPhysics()
	{
		this.movement = this.movement.add(0, -ACCELERATION, 0);

		// method c() means offset
		// method a() means addCoord
		AxisAlignedBB aabb = this.baseAABB.c(this.location.getX(), this.location.getY(), this.location.getZ());
		AxisAlignedBB addedAABB = aabb.a(this.movement.x, this.movement.y, this.movement.z);

		WorldServer nmsWorld = ((CraftWorld) this.location.getWorld()).getHandle();
		double mY = this.movement.y;
		List list = nmsWorld.getCubes(null, addedAABB);
		int i = 0;
		for(int j = list.size(); i < j; i++)
			mY = ((AxisAlignedBB) list.get(i)).b(aabb, mY);

		this.onGround = mY >= this.movement.y;
		this.movement = new Vector3D(this.movement.x, mY, this.movement.z);

		moveToNearby(this.location.clone().add(this.movement.x, this.movement.y, this.movement.z));

		//DebugUtil.say("location: "+this.location+"movement: "+this.movement+" onGround: "+this.onGround+" mY: "+mY);
	}

}

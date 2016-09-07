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
	private static final double ACCELERATION = 0.08;
	private static final double AABB_XZ_LENGTH = 0.6;
	private static final double AABB_Y_LENGTH = 1.8;

	private static final double HOVER_HEIGHT = 0.01;

	// PROPERTIES
	protected Vector3D velocity = new Vector3D();
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

	@APIUsage
	public double getWalkSpeed()
	{
		if(isSprinting())
			return 5.6/20d;

		if(isCrouched())
			return 1.3/20d;

		return 4.3/20d;
	}

	@APIUsage
	public Vector3D getVelocity()
	{
		return this.velocity;
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public void setVelocity(Vector3D velocity)
	{
		this.velocity = velocity;
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
		applyGravity();
		doBlockCollision();

		moveToNearby(this.location.clone().add(this.velocity.x, this.velocity.y, this.velocity.z), true);
		applyDrag();
	}

	private void applyGravity()
	{
		this.velocity = this.velocity.add(0, -ACCELERATION, 0);
	}

	private void doBlockCollision()
	{
		// method c() means offset
		// method a() means addCoord
		AxisAlignedBB aabb = this.baseAABB.c(this.location.getX(), this.location.getY(), this.location.getZ());
		AxisAlignedBB addedAABB = aabb.a(this.velocity.x, this.velocity.y, this.velocity.z);

		WorldServer nmsWorld = ((CraftWorld) this.location.getWorld()).getHandle();
		double mY = this.velocity.y;
		List list = nmsWorld.getCubes(null, addedAABB);
		int i = 0;
		for(int j = list.size(); i < j; i++)
			mY = ((AxisAlignedBB) list.get(i)).b(aabb, mY);

		this.onGround = mY >= this.velocity.y && this.velocity.y <= 0;

		if(mY < 0)
			mY += HOVER_HEIGHT;
		this.velocity = new Vector3D(this.velocity.x, mY, this.velocity.z);
	}

	private void applyDrag()
	{
		double newVX = this.velocity.x*0.6;
		double newVY = this.velocity.y*0.98;
		double newVZ = this.velocity.z*0.6;

		this.velocity = new Vector3D(newVX, newVY, newVZ);
	}


	// -------
	// PHYSICS INTERACTION
	// -------
	public void jump()
	{
		this.velocity = new Vector3D(this.velocity.x, 0.6, this.velocity.z);
	}

}

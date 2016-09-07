package de.domisum.animulusapi.npc;

import com.mojang.authlib.GameProfile;
import de.domisum.animulusapi.AnimulusAPI;
import de.domisum.animulusapi.block.BlockButton;
import de.domisum.auxiliumapi.data.container.Duo;
import de.domisum.auxiliumapi.util.bukkit.LocationUtil;
import de.domisum.auxiliumapi.util.java.ReflectionUtil;
import de.domisum.auxiliumapi.util.java.ThreadUtil;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;
import de.domisum.auxiliumapi.util.math.RandomUtil;
import de.domisum.compitumapi.path.pathfinders.AStar;
import de.domisum.compitumapi.path.pathprocessors.WalkablePath;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.PacketPlayOutBed;
import net.minecraft.server.v1_9_R1.PacketPlayOutBlockAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@APIUsage
public class ActionNPC extends StateNPC
{

	// CONSTANTS
	private static final double ACTION_RADIUS = 4;
	private static final double ACTION_RADIUS_SQUARED = ACTION_RADIUS*ACTION_RADIUS;

	// STATUS
	private transient Location bedLocation = null;
	private transient Chest openedChest = null;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public ActionNPC()
	{
		super();
	}

	@APIUsage
	public ActionNPC(GameProfile gameProfile, Location location)
	{
		super(gameProfile, location);
	}

	@Override
	protected void terminate()
	{
		super.terminate();

		if(this.openedChest != null)
			sendChestStatus(this.openedChest.getLocation(), false, getPlayersVisibleToArray());
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public double getWalkSpeed()
	{
		if(isSprinting())
			return 5.6/20d;

		if(isCrouched())
			return 1.3/20d;

		return 4.3/20d;
	}


	// -------
	// ACTION
	// -------
	@APIUsage
	public void putItemStackIntoChest(Location chestLocation, ItemStack itemStack) throws InterruptedException
	{
		// check if chest is too far away
		Duo<Boolean, Double> checkDistanceResult = checkDistance(chestLocation);
		if(!checkDistanceResult.a)
		{
			AnimulusAPI.getInstance().getLogger().warning(
					"The npc '"+this.id+"' could not put the item into the chest. The chest ("+chestLocation
							+") is too far away from the npc ("+getEyeLocation()+"). Distance: "+checkDistanceResult.b+" / "
							+ACTION_RADIUS);
			return;
		}

		// check if the block above the chest doesn't block it
		if(chestLocation.clone().add(0, 1, 0).getBlock().getType().isSolid())
		{
			AnimulusAPI.getInstance().getLogger().warning(
					"The npc '"+this.id+"' could not put the item into the chest. The chest ("+chestLocation
							+") is blocked by a solid block above.");
		}

		// turn towards chest
		turnHeadTowards(LocationUtil.getCenter(chestLocation), 5);

		// open chest
		Thread.sleep(RandomUtil.distribute(200, 70));
		if(!openChest(chestLocation))
			return;
		swingArm();

		// add item
		Thread.sleep(RandomUtil.distribute(1000, 300));
		this.openedChest.getInventory().addItem(itemStack);

		// close chest
		Thread.sleep(RandomUtil.distribute(1000, 300));
		sendChestStatus(chestLocation, false, getPlayersVisibleToArray());
		this.openedChest = null;

		// look up again
		Thread.sleep(RandomUtil.distribute(200, 50));
		turnHeadTowards(LocationUtil.getCenter(chestLocation).add(0, getEyeHeight()-.6, 0), 3);
	}

	private boolean openChest(Location chestLocation)
	{
		// TODO npc api | prevent chest closing animation when real player closes chest inventory and npc is still looking at it,
		// this
		// happens since bukkit thinks nobody is looking into the chest


		BlockState state = chestLocation.getBlock().getState();// SynchronizationUtil.getBlockAt(chestLocation).getState();
		if(!(state instanceof Chest))
		{
			AnimulusAPI.getInstance().getLogger()
					.warning("The NPC '"+this.id+"' failed to open the chest at '"+chestLocation+"', since it isn't a chest");

			return false;
		}

		this.openedChest = (Chest) state;

		sendChestStatus(chestLocation, true, getPlayersVisibleToArray());
		// TODO npc api | double chests

		return true;
	}

	@APIUsage
	public void pressButton(Location buttonLocation) throws InterruptedException
	{
		// check if there is a button at the specified location
		if(!buttonLocation.getBlock().getType().name().contains("BUTTON"))
		{
			AnimulusAPI.getInstance().getLogger()
					.warning("The npc '"+this.id+"' could not find a button at '"+buttonLocation+"'.");
			return;
		}

		// check if button is too far away
		Duo<Boolean, Double> checkDistanceResult = checkDistance(buttonLocation);
		if(!checkDistanceResult.a)
		{
			AnimulusAPI.getInstance().getLogger().warning(
					"The npc '"+this.id+"' could not press the button. The button ("+buttonLocation
							+") is too far away from the npc ("+getEyeLocation()+"). Distance: "+checkDistanceResult.b+" / "
							+ACTION_RADIUS);
			return;
		}

		// turn towards button
		turnHeadTowards(LocationUtil.getCenter(buttonLocation), 5);

		// press button
		new BlockButton(buttonLocation).press();
		swingArm();
	}


	@SuppressWarnings("deprecation")
	@APIUsage
	public void placeBlock(Location blockLocation, Material material, byte data) throws InterruptedException
	{
		// check if block can be set at location
		Block block = blockLocation.getBlock();
		if(block.getType().isSolid())
			return;

		// check if block location is too far away
		Duo<Boolean, Double> checkDistanceResult = checkDistance(blockLocation);
		if(!checkDistanceResult.a)
		{
			AnimulusAPI.getInstance().getLogger().warning(
					"The npc '"+this.id+"' could not place a block. The location ("+blockLocation
							+") is too far away from the npc ("+getEyeLocation()+"). Distance: "+checkDistanceResult.b+" / "
							+ACTION_RADIUS);
			return;
		}

		// turn towards location
		turnHeadTowards(LocationUtil.getCenter(blockLocation), 5);

		// place the block
		swingArm();
		block.setType(material);
		block.setData(data);
	}

	@SuppressWarnings("deprecation")
	@APIUsage
	public void tillDirt(Location blockLocation) throws InterruptedException
	{
		Block block = blockLocation.getBlock();
		if((block.getType() != Material.DIRT) && (block.getType() != Material.GRASS))
			return;

		// check if block location is too far away
		Duo<Boolean, Double> checkDistanceResult = checkDistance(blockLocation);
		if(!checkDistanceResult.a)
		{
			AnimulusAPI.getInstance().getLogger().warning(
					"The npc '"+this.id+"' could not till a block. The location ("+blockLocation
							+") is too far away from the npc ("+getEyeLocation()+"). Distance: "+checkDistanceResult.b+" / "
							+ACTION_RADIUS);
			return;
		}

		// turn towards block
		turnHeadTowards(LocationUtil.getCenter(blockLocation).add(0, 0.5, 0), 5);

		// till the block
		swingArm();
		block.setType(Material.SOIL);
		block.setData((byte) 0);
	}


	private Duo<Boolean, Double> checkDistance(Location location)
	{
		double distanceSquared = getEyeLocation().distanceSquared(location);
		if(distanceSquared < ACTION_RADIUS_SQUARED)
			return new Duo<>(true, 0d);

		return new Duo<>(false, Math.sqrt(distanceSquared));
	}


	// -------
	// MOVEMENT
	// -------
	@APIUsage
	public void walkTo(Location target) throws InterruptedException
	{
		walkTo(target, 1);
	}

	@APIUsage
	public void walkTo(Location target, double speedMultiplier) throws InterruptedException
	{
		// find path
		AStar aStar = new AStar(this.location, target);
		try
		{
			aStar.findPath();
		}
		catch(IllegalArgumentException e)
		{
			onWalkingFail(e);
			return;
		}
		if(!aStar.isPathFound())
			return;

		WalkablePath walkablePath = new WalkablePath(aStar.getPath());

		// walk path
		double speed = getWalkSpeed()*speedMultiplier;
		float accuracy = 1;
		double tolerance = 0.3f;

		while(walkablePath.hasNext())
		{
			Location nextLocation = walkablePath.getNext();

			double dX = nextLocation.getX()-this.location.getX();
			double dY = nextLocation.getY()-this.location.getY();
			double dZ = nextLocation.getZ()-this.location.getZ();

			while(this.location.distanceSquared(nextLocation) > (tolerance*tolerance))
			{
				double stepX = (dX < 0 ? -1 : 1)*Math.min(speed/accuracy, Math.abs(dX));
				double stepY = (dY < 0 ? -1 : 1)*Math.min(speed/accuracy, Math.abs(dY));
				double stepZ = (dZ < 0 ? -1 : 1)*Math.min(speed/accuracy, Math.abs(dZ));

				float stepYaw = (nextLocation.getYaw()-this.location.getYaw())/5f/accuracy;
				float stepPitch = (nextLocation.getPitch()-this.location.getPitch())/5f/accuracy;

				dX -= stepX;
				dY -= stepY;
				dZ -= stepZ;

				Location newLocation = this.location.clone();
				newLocation.add(stepX, stepY, stepZ);
				newLocation.setYaw(this.location.getYaw()+stepYaw);
				newLocation.setPitch(this.location.getPitch()+stepPitch);

				moveToNearby(newLocation);

				ThreadUtil.sleep(Math.round(50/accuracy));
				if(Thread.currentThread().isInterrupted())
					return;
			}
		}

		Location rotatedTarget = target.clone();
		rotatedTarget.setYaw(this.location.getYaw());
		rotatedTarget.setPitch(this.location.getPitch());
		moveToNearby(LocationUtil.getFloorCenter(rotatedTarget));
	}

	@APIUsage
	protected void onWalkingFail(Exception e)
	{
		e.printStackTrace();
	}


	@APIUsage
	public void stroll(double radius, long durationMs) throws InterruptedException
	{
		Location startLocation = getLocation();
		long startTime = System.currentTimeMillis();

		while(System.currentTimeMillis() < (startTime+durationMs))
		{
			Location strollLocation = findStrollLocation(startLocation, radius);
			if(strollLocation == null)
				return;

			walkTo(strollLocation, 0.7);

			Thread.sleep(RandomUtil.distribute(4000, 2500));
		}

		walkTo(startLocation, 0.7);
	}

	private Location findStrollLocation(Location base, double radius)
	{
		// 5 tries to find a location
		for(int i = 0; i < 5; i++)
		{
			double randomAngle = RandomUtil.nextDouble()*2*Math.PI;
			double randomDistance = RandomUtil.nextDouble()*radius;
			double dX = Math.sin(randomAngle)*randomDistance;
			double dZ = Math.cos(randomAngle)*randomDistance;

			for(int dY : new int[] {0, 1, -1, 2, -2})
			{
				Location location = base.clone().add(Math.round(dX), Math.round(dY), dZ);

				if(canStandAt(location))
					return location;
			}
		}

		return null;
	}


	@APIUsage
	public void turnHeadTowards(Location lookAt, float speed) throws InterruptedException
	{
		Location directionLocation = LocationUtil.lookAt(getEyeLocation(), lookAt);
		turnHeadTowards(directionLocation.getYaw(), directionLocation.getPitch(), speed);
	}

	@APIUsage
	public void turnHeadTowards(float yaw, float pitch, float speed) throws InterruptedException
	{
		double accuracy = 2;

		float dYaw = yaw-this.location.getYaw();
		float dPitch = pitch-this.location.getPitch();

		while((Math.abs(dYaw) > 0.1) || (Math.abs(dPitch) > 0.1))
		{
			float stepYaw = (float) Math.min(speed*accuracy, Math.abs(dYaw))*(dYaw < 0 ? -1 : 1);
			float stepPitch = (float) Math.min(speed*accuracy, Math.abs(dPitch))*(dPitch < 0 ? -1 : 1);

			this.location.setYaw(this.location.getYaw()+stepYaw);
			this.location.setPitch(this.location.getPitch()+stepPitch);

			dYaw -= stepYaw;
			dPitch -= stepPitch;

			sendLookHeadRotation(getPlayersVisibleToArray());

			Thread.sleep(Math.round(50/accuracy));
		}
	}


	@APIUsage
	public void enterBed(Location bedLocation) throws InterruptedException
	{
		if(bedLocation.getBlock().getType() != Material.BED_BLOCK)
			return;

		// check if bed location is too far away
		Duo<Boolean, Double> checkDistanceResult = checkDistance(bedLocation);
		if(!checkDistanceResult.a)
		{
			AnimulusAPI.getInstance().getLogger().warning(
					"The npc '"+this.id+"' could not enter the bed. The location ("+bedLocation+") is too far away from the npc ("
							+getEyeLocation()+"). Distance: "+checkDistanceResult.b+" / "+ACTION_RADIUS);
			return;
		}

		// turn head towards
		turnHeadTowards(LocationUtil.getCenter(bedLocation), 5);
		Thread.sleep(RandomUtil.distribute(200, 50));

		// enter bed
		swingArm();
		this.bedLocation = bedLocation;
		sendEnterBed(bedLocation, getPlayersVisibleToArray());
	}

	@APIUsage
	public void leaveBed()
	{
		if(this.bedLocation == null)
			return;

		Location leaveLocation = findBedLeaveLocation();
		if(leaveLocation == null)
			leaveLocation = this.bedLocation;

		this.bedLocation = null;
		sendAnimation(2, getPlayersVisibleToArray());
		teleport(leaveLocation);
	}


	private Location findBedLeaveLocation()
	{
		int searchRadius = 1;

		for(int dY : new int[] {0, -1, 1})
			for(int dX = -searchRadius; dX <= searchRadius; dX++)
				for(int dZ = -searchRadius; dZ <= searchRadius; dZ++)
				{
					Location newLocation = this.bedLocation.clone().add(dX, dY, dZ);
					if(canStandAt(newLocation))
						return LocationUtil.getCenter(newLocation).add(0, -.5, 0);
				}

		return null;
	}


	// -------
	// PACKETS
	// -------
	@Override
	protected void sendToPlayer(Player... players)
	{
		super.sendToPlayer(players);

		if(this.openedChest != null)
			sendChestStatus(this.openedChest.getLocation(), true, players);

		if(this.bedLocation != null)
			sendEnterBed(this.bedLocation, players);
	}


	// ACTION
	private void sendChestStatus(Location location, boolean open, Player... players)
	{
		PacketPlayOutBlockAction packet = new PacketPlayOutBlockAction(
				new BlockPosition(location.getX(), location.getY(), location.getZ()),
				net.minecraft.server.v1_9_R1.Block.getByName("chest"), 1, open ? 1 : 0);

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}


	// MOVEMENT
	private void sendEnterBed(Location location, Player... players)
	{
		PacketPlayOutBed packet = new PacketPlayOutBed();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", this.entityId);
		ReflectionUtil.setDeclaredFieldValue(packet, "b", new BlockPosition(location.getX(), location.getY(), location.getZ()));

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

}

package de.domisum.animulusapi.npc;

import com.mojang.authlib.GameProfile;
import de.domisum.animulusapi.AnimulusAPI;
import de.domisum.auxiliumapi.util.bukkit.LocationUtil;
import de.domisum.auxiliumapi.util.bukkit.PacketUtil;
import de.domisum.auxiliumapi.util.java.ReflectionUtil;
import de.domisum.auxiliumapi.util.java.ThreadUtil;
import de.domisum.auxiliumapi.util.java.annotations.APIUsage;
import de.domisum.auxiliumapi.util.java.annotations.DeserializationNoArgsConstructor;
import net.minecraft.server.v1_9_R1.*;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_9_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_9_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_9_R1.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.minecraft.server.v1_9_R1.WorldSettings.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StateNPC
{

	// CONSTANTS
	protected static final double VISIBILITY_RANGE = 45d;
	protected static final long TABLIST_REMOVE_DELAY_MS = 5000;
	protected static final long RELATIVE_MOVE_TELEPORT_INTERVAL = 40;

	// STATUS
	protected transient String id;
	protected transient int entityId;
	protected transient GameProfile gameProfile;

	protected transient Location location;
	protected transient ItemStack itemInHand = null;
	protected transient ItemStack itemInOffHand = null;
	protected transient ItemStack[] armor = new ItemStack[4]; // 0: boots, 1: leggings, 2: chestplate, 3: helmet

	protected transient boolean onFire = false;
	protected transient boolean crouched = false;
	protected transient boolean sprinting = false;
	protected transient boolean isEating = false; // TODO implement these
	protected transient boolean isDrinking = false;
	protected transient boolean isBlocking = false;
	protected transient boolean isInvisible = false; // TODO does this work?
	protected transient boolean isGlowing = false;
	protected transient boolean isFlyingWithElytra = false;

	private transient byte numberOfArrowsInBody = 0;

	// PLAYERS
	private transient Set<Player> visibleTo;

	// TEMP
	private transient int moveTeleportCounter = 0;


	// -------
	// CONSTRUCTOR
	// -------
	@DeserializationNoArgsConstructor
	public StateNPC()
	{

	}

	protected StateNPC(GameProfile gameProfile, Location location)
	{
		this.gameProfile = gameProfile;
		this.location = location;

		initialize();
	}

	protected void initialize()
	{
		this.entityId = getUnusedEntityId();
		this.visibleTo = new HashSet<>();
	}

	protected void terminate()
	{
		sendRemoveToPlayer(getPlayersVisibleToArray());
		this.visibleTo.clear();
	}

	@APIUsage
	public void despawn()
	{
		AnimulusAPI.getNPCManager().removeNPC(this);
		terminate();
	}


	// -------
	// GETTERS
	// -------
	@APIUsage
	public String getId()
	{
		return this.id;
	}

	@APIUsage
	public int getEntityId()
	{
		return this.entityId;
	}

	@APIUsage
	public Location getLocation()
	{
		return this.location.clone();
	}

	@APIUsage
	public double getEyeHeight()
	{
		if(this.crouched)
			return 1.54;

		return 1.62;
	}

	@APIUsage
	public Location getEyeLocation()
	{
		return this.location.clone().add(0, getEyeHeight(), 0);
	}

	@APIUsage
	public ItemStack getItemInHand()
	{
		return this.itemInHand;
	}


	@APIUsage
	public boolean isOnFire()
	{
		return this.onFire;
	}

	@APIUsage
	public boolean isCrouched()
	{
		return this.crouched;
	}

	@APIUsage
	public boolean isSprinting()
	{
		return this.sprinting;
	}

	@APIUsage
	public boolean isGlowing()
	{
		return this.isGlowing;
	}


	private DataWatcher getMetadata()
	{
		DataWatcher metadata = new DataWatcher(null);
		// http://wiki.vg/Entities#Entity_Metadata_Format

		// base information
		byte metadataBaseInfo = 0;
		if(this.onFire)
			metadataBaseInfo |= 0x01;
		if(this.crouched)
			metadataBaseInfo |= 0x02;
		if(this.sprinting)
			metadataBaseInfo |= 0x08;
		if(this.isEating || this.isDrinking || this.isBlocking)
			metadataBaseInfo |= 0x10;
		if(this.isInvisible)
			metadataBaseInfo |= 0x20;
		if(this.isGlowing)
			metadataBaseInfo |= 0x40;
		if(this.isFlyingWithElytra)
			metadataBaseInfo |= 0x80;
		metadata.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), metadataBaseInfo);

		// arrows in body
		metadata.register(new DataWatcherObject<>(9, DataWatcherRegistry.a), this.numberOfArrowsInBody);
		// !!! seems to have changed to 10 in v1.9/v1.10

		// skin parts
		byte skinParts = 0b01111111; // all parts displayed (first bit is unused)
		metadata.register(new DataWatcherObject<>(13, DataWatcherRegistry.a), skinParts);

		return metadata;
	}


	@APIUsage
	public Set<Player> getPlayersVisibleTo()
	{
		return new HashSet<>(this.visibleTo);
	}

	@APIUsage
	public boolean isVisibleTo(Player player)
	{
		return this.visibleTo.contains(player);
	}

	@APIUsage
	public boolean isVisibleToSomebody()
	{
		return !this.visibleTo.isEmpty();
	}

	@APIUsage
	public Player[] getPlayersVisibleToArray()
	{
		return this.visibleTo.toArray(new Player[this.visibleTo.size()]);
	}


	// -------
	// SETTERS
	// -------
	@APIUsage
	public void setId(String id)
	{
		this.id = id;
	}

	@APIUsage
	public void setItemInHand(ItemStack itemStack)
	{
		this.itemInHand = itemStack;

		sendEntityEquipmentChange(EnumItemSlot.MAINHAND, itemStack, getPlayersVisibleToArray());
	}

	@APIUsage
	public void setItemInOffHand(ItemStack itemStack)
	{
		this.itemInOffHand = itemStack;
		sendEntityEquipmentChange(EnumItemSlot.OFFHAND, itemStack, getPlayersVisibleToArray());
	}

	@APIUsage
	public void setArmor(int slot, ItemStack itemStack)
	{
		this.armor[slot] = itemStack;

		sendEntityEquipmentChange(getArmorItemSlot(slot), itemStack, getPlayersVisibleToArray());
	}


	@APIUsage
	public void setOnFire(boolean onFire)
	{
		if(this.onFire == onFire)
			return;

		this.onFire = onFire;
		sendEntityMetadata(getPlayersVisibleToArray());
	}

	@APIUsage
	public void setCrouched(boolean crouched)
	{
		if(this.crouched == crouched)
			return;

		if(isSprinting())
			throw new IllegalStateException("Can't crouch while sprinting");

		this.crouched = crouched;
		sendEntityMetadata(getPlayersVisibleToArray());
	}

	@APIUsage
	public void setSprinting(boolean sprinting)
	{
		if(this.sprinting == sprinting)
			return;

		if(isCrouched())
			throw new IllegalStateException("Can't sprint while crouching");

		this.sprinting = sprinting;
		sendEntityMetadata(getPlayersVisibleToArray());
	}

	@APIUsage
	public void setGlowing(boolean glowing)
	{
		if(this.isGlowing == glowing)
			return;

		this.isGlowing = glowing;
		sendEntityMetadata(getPlayersVisibleToArray());
	}


	@APIUsage
	public void setNumberOfArrowsInBody(byte numberOfArrowsInBody)
	{
		if(this.numberOfArrowsInBody == numberOfArrowsInBody)
			return;

		this.numberOfArrowsInBody = numberOfArrowsInBody;
		sendEntityMetadata(getPlayersVisibleToArray());
	}


	// -------
	// PLAYERS
	// -------
	@APIUsage
	protected void updateVisibleForPlayers()
	{
		for(Player p : Bukkit.getOnlinePlayers())
			updateVisibilityForPlayer(p);
	}

	@APIUsage
	protected void updateVisibilityForPlayer(Player player)
	{
		if(player.getLocation().distanceSquared(getLocation()) < (VISIBILITY_RANGE*VISIBILITY_RANGE))
		{
			if(!isVisibleTo(player))
				becomeVisibleFor(player);
		}
		else
		{
			if(isVisibleTo(player))
				becomeInvisibleFor(player, true);
		}
	}

	@APIUsage
	public void becomeVisibleFor(Player player)
	{
		this.visibleTo.add(player);
		sendToPlayer(player);
	}

	@APIUsage
	public void becomeInvisibleFor(Player player, boolean sendPackets)
	{
		this.visibleTo.remove(player);

		if(sendPackets)
			sendRemoveToPlayer(player);
	}


	// -------
	// INTERACTION
	// -------
	@APIUsage
	public void playerLeftClick(Player player)
	{

	}

	@APIUsage
	public void playerRightClick(Player player)
	{

	}


	// -------
	// ACTION
	// -------
	@APIUsage
	public void swingArm()
	{
		sendAnimation(0, getPlayersVisibleToArray());
	}

	@APIUsage
	public void swingOffhandArm()
	{
		sendAnimation(3, getPlayersVisibleToArray());
	}


	// -------
	// MOVEMENT
	// -------
	@APIUsage
	public void moveToNearby(Location target)
	{
		if((this.moveTeleportCounter++%RELATIVE_MOVE_TELEPORT_INTERVAL) == 0)
		{
			teleport(target);
			return;
		}

		sendRelativeMoveLook(target, getPlayersVisibleToArray());
		this.location = target;
		sendHeadRotation(getPlayersVisibleToArray());
	}

	@APIUsage
	public void teleport(Location target)
	{
		this.location = target;

		// this sends despawn packets if the npc is too far away now
		updateVisibleForPlayers();

		sendTeleport(getPlayersVisibleToArray());
		sendLookHeadRotation(getPlayersVisibleToArray());
	}


	public void setYawPitch(float yaw, float pitch)
	{
		this.location.setYaw(yaw);
		this.location.setPitch(pitch);

		sendLookHeadRotation(getPlayersVisibleToArray());
	}

	@APIUsage
	public void lookAt(Location lookAt)
	{
		this.location = LocationUtil.lookAt(this.location, lookAt);
		sendLookHeadRotation(getPlayersVisibleToArray());
	}


	@APIUsage
	public boolean canStandAt(Location location)
	{
		if(location.getBlock().getType().isSolid())
			return false;

		if(location.clone().add(0, 1, 0).getBlock().getType().isSolid())
			return false;

		if(!location.clone().add(0, -1, 0).getBlock().getType().isSolid())
			return false;

		return true;
	}


	// -------
	// UPDATING
	// -------
	@APIUsage
	protected void update()
	{

	}


	// -------
	// PACKETS
	// -------
	protected void sendToPlayer(Player... players)
	{
		sendPlayerInfo(players);
		sendEntitySpawn(players);
		sendHeadRotation(players);

		sendEntityMetadata(players);
		sendEntityEquipment(players);

		sendPlayerInfoRemove(players);
	}

	protected void sendEntityEquipment(Player... players)
	{
		for(int slot = 0; slot < 4; slot++)
			sendEntityEquipmentChange(getArmorItemSlot(slot), this.armor[slot], players);

		sendEntityEquipmentChange(EnumItemSlot.MAINHAND, this.itemInHand, players);
		sendEntityEquipmentChange(EnumItemSlot.OFFHAND, this.itemInOffHand, players);
	}


	protected void sendRemoveToPlayer(Player... players)
	{
		sendEntityDespawn(players);
	}


	// PLAYER INFO
	protected PlayerInfoData getPlayerInfoData(PacketPlayOutPlayerInfo packetPlayOutPlayerInfo)
	{
		return packetPlayOutPlayerInfo.new PlayerInfoData(this.gameProfile, 0, EnumGamemode.NOT_SET,
				CraftChatMessage.fromString("")[0]);
	}

	protected void sendPlayerInfo(Player... players)
	{
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", EnumPlayerInfoAction.ADD_PLAYER);
		@SuppressWarnings("unchecked")
		List<PlayerInfoData> b = (List<PlayerInfoData>) ReflectionUtil.getDeclaredFieldValue(packet, "b");
		b.add(getPlayerInfoData(packet));

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	protected void sendPlayerInfoRemove(Player... players)
	{
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", EnumPlayerInfoAction.REMOVE_PLAYER);
		@SuppressWarnings("unchecked")
		List<PlayerInfoData> b = (List<PlayerInfoData>) ReflectionUtil.getDeclaredFieldValue(packet, "b");
		b.add(getPlayerInfoData(packet));

		ThreadUtil.runDelayed(()->
		{
			for(Player p : players)
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}, TABLIST_REMOVE_DELAY_MS);
	}


	// ENTITY SPAWN
	protected void sendEntitySpawn(Player... players)
	{
		PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", this.entityId);
		ReflectionUtil.setDeclaredFieldValue(packet, "b", this.gameProfile.getId());
		ReflectionUtil.setDeclaredFieldValue(packet, "c", this.location.getX());
		ReflectionUtil.setDeclaredFieldValue(packet, "d", this.location.getY());
		ReflectionUtil.setDeclaredFieldValue(packet, "e", this.location.getZ());
		ReflectionUtil.setDeclaredFieldValue(packet, "f", PacketUtil.toPacketAngle(this.location.getYaw()));
		ReflectionUtil.setDeclaredFieldValue(packet, "g", PacketUtil.toPacketAngle(this.location.getPitch()));
		ReflectionUtil.setDeclaredFieldValue(packet, "h", getMetadata());

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}


	// ENTITY DESPAWN
	protected void sendEntityDespawn(Player... players)
	{
		PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", new int[] {this.entityId});

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}


	// ENTITY CHANGE
	protected void sendEntityEquipmentChange(EnumItemSlot slot, ItemStack itemStack, Player... players)
	{
		PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(this.entityId, slot,
				CraftItemStack.asNMSCopy(itemStack));

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	protected void sendEntityMetadata(Player... players)
	{
		// the boolean determines the metadata sent to the player;
		// true = all metadata
		// false = the dirty metadata
		PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(this.entityId, getMetadata(), true);

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}


	// MOVEMENT
	protected void sendRelativeMoveLook(Location target, Player... players)
	{
		double dX = target.getX()-this.location.getX();
		double dY = target.getY()-this.location.getY();
		double dZ = target.getZ()-this.location.getZ();

		// @formatter:off
        PacketPlayOutRelEntityMoveLook packet = new PacketPlayOutRelEntityMoveLook(
                this.entityId,
                PacketUtil.toPacketDistance(dX),
                PacketUtil.toPacketDistance(dY),
                PacketUtil.toPacketDistance(dZ),
                PacketUtil.toPacketAngle(target.getYaw()),
                PacketUtil.toPacketAngle(target.getPitch()),
                true);
        // @formatter:on

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	protected void sendTeleport(Player... players)
	{
		PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", this.entityId);
		ReflectionUtil.setDeclaredFieldValue(packet, "b", this.location.getX());
		ReflectionUtil.setDeclaredFieldValue(packet, "c", this.location.getY());
		ReflectionUtil.setDeclaredFieldValue(packet, "d", this.location.getZ());
		ReflectionUtil.setDeclaredFieldValue(packet, "e", PacketUtil.toPacketAngle(this.location.getYaw()));
		ReflectionUtil.setDeclaredFieldValue(packet, "f", PacketUtil.toPacketAngle(this.location.getPitch()));
		ReflectionUtil.setDeclaredFieldValue(packet, "g", true);

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	void sendLookHeadRotation(Player... players)
	{
		sendLook(players);
		sendHeadRotation(players);
	}

	private void sendLook(Player... players)
	{
		PacketPlayOutEntityLook packet = new PacketPlayOutEntityLook(this.entityId,
				PacketUtil.toPacketAngle(this.location.getYaw()), PacketUtil.toPacketAngle(this.location.getPitch()), true);

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	private void sendHeadRotation(Player... players)
	{
		PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", this.entityId);
		ReflectionUtil.setDeclaredFieldValue(packet, "b", PacketUtil.toPacketAngle(this.location.getYaw()));

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}


	// ACTION
	void sendAnimation(int animationId, Player... players)
	{
		PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
		ReflectionUtil.setDeclaredFieldValue(packet, "a", this.entityId);
		ReflectionUtil.setDeclaredFieldValue(packet, "b", animationId);

		for(Player p : players)
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}


	// -------
	// UTIL
	// -------
	private static int getUnusedEntityId()
	{
		// the entityId of a new (net.minecraft.server.)Entity is set like this:
		// this.id = (entityCount++);
		// since the ++ is after the variable, the value of id is set to the current value of entityCount and
		// entity count is increased by one for the next entity
		// this means returning the entityCount as a new entityId is safe, if we increase the variable before returning

		int entityCount = (int) ReflectionUtil.getDeclaredFieldValue(Entity.class, null, "entityCount");
		ReflectionUtil.setDeclaredFieldValue(Entity.class, null, "entityCount", entityCount+1);

		return entityCount;
	}

	private static EnumItemSlot getArmorItemSlot(int slot)
	{
		if(slot == 0)
			return EnumItemSlot.FEET;
		else if(slot == 1)
			return EnumItemSlot.LEGS;
		else if(slot == 2)
			return EnumItemSlot.CHEST;
		else if(slot == 3)
			return EnumItemSlot.HEAD;

		return null;
	}

}

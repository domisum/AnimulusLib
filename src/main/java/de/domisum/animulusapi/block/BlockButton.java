package de.domisum.animulusapi.block;

import de.domisum.animulusapi.AnimulusAPI;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BlockButton
{

	// CONSTANTS
	private static final BlockStateDirection FACING = BlockStateDirection.of("facing");
	private static final BlockStateBoolean POWERED = BlockStateBoolean.of("powered");

	// REFERENCES
	private Location location;


	// -------
	// CONSTRUCTOR
	// -------
	public BlockButton(Location location)
	{
		this.location = location;
	}


	// -------
	// GETTERS
	// -------
	@SuppressWarnings("unused")
	public boolean isPressed()
	{
		WorldServer world = ((CraftWorld) this.location.getWorld()).getHandle();
		BlockPosition blockPosition = new BlockPosition(this.location.getBlockX(), this.location.getBlockY(),
				this.location.getBlockZ());
		IBlockData blockData = world.getType(blockPosition);

		return blockData.get(POWERED);
	}

	private int getPressDuration()
	{
		if(this.location.getBlock().getType() == Material.STONE_BUTTON)
			return 20;
		else if(this.location.getBlock().getType() == Material.WOOD_BUTTON)
			return 30;

		return 0;
	}

	private SoundEffect getSoundEffects()
	{
		if(this.location.getBlock().getType() == Material.STONE_BUTTON)
			return SoundEffects.gc;
		else if(this.location.getBlock().getType() == Material.WOOD_BUTTON)
			return SoundEffects.ha;

		return null;
	}


	// -------
	// SETTERS
	// -------
	public boolean press() throws InterruptedException
	{
		Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(AnimulusAPI.getInstance().getPlugin(), this::pressRaw);

		try
		{
			return future.get();
		}
		catch(ExecutionException exception)
		{
			exception.printStackTrace();
		}

		return false;
	}

	private boolean pressRaw()
	{
		// taken from net.minecraft.server.v1_9_R1.BlockButtonAbstract

		World world = ((CraftWorld) this.location.getWorld()).getHandle();
		BlockPosition blockposition = new BlockPosition(this.location.getX(), this.location.getY(), this.location.getZ());
		IBlockData iblockdata = world.getType(blockposition);
		Block block = iblockdata.getBlock();

		if(iblockdata.get(POWERED))
			return true;

		boolean powered = iblockdata.get(POWERED);
		org.bukkit.block.Block bukkitBlock = world.getWorld()
				.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
		int old = powered ? 15 : 0;
		int current = !powered ? 15 : 0;

		BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, old, current);
		world.getServer().getPluginManager().callEvent(eventRedstone);

		if((eventRedstone.getNewCurrent() > 0 ? 1 : 0) != (powered ? 0 : 1))
			return true;

		world.setTypeAndData(blockposition, iblockdata.set(POWERED, true), 3);
		world.b(blockposition, blockposition);
		world.a(null, blockposition, getSoundEffects(), SoundCategory.BLOCKS, 0.3f, 0.3f); // null is for entity human, maybe
		// cause problems
		c(world, blockposition, iblockdata.get(FACING), block);
		world.a(blockposition, block, getPressDuration());
		return true;
	}

	private void c(World world, BlockPosition blockposition, EnumDirection enumdirection, Block block)
	{
		world.applyPhysics(blockposition, block);
		world.applyPhysics(blockposition.shift(enumdirection.opposite()), block);
	}


}

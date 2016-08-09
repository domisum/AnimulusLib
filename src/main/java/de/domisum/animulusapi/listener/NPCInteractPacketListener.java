package de.domisum.animulusapi.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import de.domisum.animulusapi.AnimulusAPI;
import de.domisum.animulusapi.npc.StateNPC;
import org.bukkit.entity.Player;

public class NPCInteractPacketListener
{

	// -------
	// CONSTRUCTOR
	// -------
	public NPCInteractPacketListener()
	{
		listenForPackets();
	}

	private void listenForPackets()
	{
		PacketAdapter packetAdapter = new PacketAdapter(AnimulusAPI.getInstance().getPlugin(), ListenerPriority.NORMAL,
				PacketType.Play.Client.USE_ENTITY)
		{
			@Override
			public void onPacketReceiving(PacketEvent packetEvent)
			{
				// taken from PacketWrapper
				EntityUseAction action = packetEvent.getPacket().getEntityUseActions().read(0);
				if(action == EntityUseAction.INTERACT_AT)
					return;

				Player player = packetEvent.getPlayer();
				int entityId = packetEvent.getPacket().getIntegers().read(0);
				StateNPC npc = AnimulusAPI.getNPCManager().getNPC(entityId);

				// clicked actual player
				if(npc == null)
					return;

				if(action == EntityUseAction.INTERACT)
					npc.playerRightClick(player);
				else
					npc.playerLeftClick(player);
			}
		};

		ProtocolLibrary.getProtocolManager().addPacketListener(packetAdapter);
	}

}

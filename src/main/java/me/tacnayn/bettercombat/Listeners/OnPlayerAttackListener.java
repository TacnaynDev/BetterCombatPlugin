package me.tacnayn.bettercombat.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import me.tacnayn.bettercombat.BetterCombat;

public class OnPlayerAttackListener {

    BetterCombat plugin;

    public OnPlayerAttackListener(BetterCombat plugin) {
        this.plugin = plugin;
    }

    public void register(){
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                // If packet was an attack
                if(event.getPacket().getEnumEntityUseActions().read(0).getAction() == EnumWrappers.EntityUseAction.ATTACK){
                    event.setCancelled(true);
                }
            }
        });
    }
}
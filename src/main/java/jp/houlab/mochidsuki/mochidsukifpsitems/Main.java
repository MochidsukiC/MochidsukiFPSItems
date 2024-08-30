package jp.houlab.mochidsuki.mochidsukifpsitems;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin {
    public static Plugin plugin;

    private ProtocolManager protocolManager;
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;



        //ProtocolLib
        protocolManager = ProtocolLibrary.getProtocolManager();

        //望遠鏡をのぞくのをやめたことをキャッチ
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SPYGLASS){
                    V.useSniper.remove(event.getPlayer());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> V.useSniper.remove(event.getPlayer()),5L);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

class V{
    static List<Player> useSniper = new ArrayList<Player>();
}
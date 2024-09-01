package jp.houlab.mochidsuki.mochidsukifpsitems;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class Main extends JavaPlugin {
    public static Plugin plugin;
    public static FileConfiguration config;

    private ProtocolManager protocolManager;
    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        getServer().getPluginManager().registerEvents(new Listener(),this);

        saveDefaultConfig();
        config = getConfig();

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

        new EveryTicks().runTaskTimer(this, 0L, 1L);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

class V{
    static public List<Player> useSniper = new ArrayList<Player>();//スナイパーを使用中か否か

    static public HashMap<Projectile, PotionType> SnowBallEffect = new HashMap<>();

    static public HashMap<Entity, Player> Owner = new HashMap<>();
}
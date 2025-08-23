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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

import java.util.*;

/**
 * メインクラス
 */
public final class Main extends JavaPlugin {
    public static Plugin plugin;
    public static FileConfiguration config;

    private ProtocolManager protocolManager;

    /**
     * 起動時の初期化処理
     */
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

    /**
     * 終了
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

class V{
    static public HashSet<Player> useSniper = new HashSet<>();//スナイパーを使用中か否か

    static public HashMap<Projectile, PotionType> SnowBallEffect = new HashMap<>();

    static private Map<Entity, Player> Owner = new LinkedHashMap<>();
    static private Map<Player, List<Entity>> Entities = new HashMap<>();

    public static void addOwner(Player player,Entity entity){
        Owner.put(entity, player);

        List<Entity> entities = getOwnerEntities(player);
        if(entities == null){
            entities =  new ArrayList<>();
            Entities.put(player, entities);
        }
        entities.add(entity);
    }

    public static void removeEntity(Entity entity){
        entity.remove();
        getOwnerEntities(Owner.get(entity)).remove(entity);
        Owner.remove(entity);
    }

    public static Player getOwner(Entity entity){
        return Owner.get(entity);
    }

    public static Set<Entity> getEntities(){
        return Owner.keySet();
    }

    public static List<Entity> getOwnerEntities(Player player){
        return Entities.get(player);
    }
}
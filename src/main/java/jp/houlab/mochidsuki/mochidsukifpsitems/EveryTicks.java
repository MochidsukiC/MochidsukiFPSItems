package jp.houlab.mochidsuki.mochidsukifpsitems;

import org.bukkit.GameMode;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import static jp.houlab.mochidsuki.mochidsukifpsitems.Main.plugin;

/**
 * 毎ティック実行されるクラス
 * @author Mochidsuki
 */
public class EveryTicks extends BukkitRunnable {

    /**
     * 実行
     */
    @Override
    public void run() {
        a:
        for(Entity entity : V.getEntities()){
            if(entity.getType().equals(EntityType.CREEPER)){//クリーパーズトラップ
                Creeper creeper = (Creeper) entity;
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    if((player.getGameMode().equals(GameMode.ADVENTURE) || player.getGameMode().equals(GameMode.SURVIVAL)) && creeper.getLocation().distance(player.getLocation()) < 3 && (player.getScoreboard().getEntityTeam(player) == null ||  !player.getScoreboard().getEntityTeam(player).getEntries().contains(entity.getScoreboardEntryName()))) {
                        if(creeper.isDead()){
                            V.removeEntity(entity);
                            continue a;
                        }

                        V.getOwner(entity).sendMessage("トラップ発動!!");
                        creeper.removePotionEffect(PotionEffectType.SLOW);
                        creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 4, true, true));

                        V.removeEntity(entity);
                    }
                }

            }
        }
    }
}

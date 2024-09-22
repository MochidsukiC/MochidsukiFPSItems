package jp.houlab.mochidsuki.mochidsukifpsitems;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 飛来物が永久に演算され、サーバーに負荷をかけるのを抑止するクラス
 */
public class DistanceKiller extends BukkitRunnable {
    Entity entity;
    Location location;
    int time;

    /**
     * コンストラクタ
     * @param entity 飛来物
     * @param location 発射地点
     * @param time 限界時間
     */
    public DistanceKiller(Entity entity, Location location, int time){
        this.entity = entity;
        this.location = location;
        this.time = time;
    }

    /**
     * 実行
     */
    @Override
    public void run() {
        if(time > 0){
            time = time - 1;
            for (int i = 0;i < 40;i++) {
                entity.getWorld().spawnParticle(Particle.END_ROD, entity.getLocation(), 1, 0, 0, 0, 0);
            }
            if(entity.getLocation().distance(location) > 400){
                entity.remove();
                cancel();
            }
        }else {
            cancel();
        }
    }
}

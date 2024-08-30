package jp.houlab.mochidsuki.mochidsukifpsitems;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class DistanceKiller extends BukkitRunnable {
    Entity entity;
    Location location;
    int time;

    public DistanceKiller(Entity entity, Location location, int time){
        this.entity = entity;
        this.location = location;
        this.time = time;
    }

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

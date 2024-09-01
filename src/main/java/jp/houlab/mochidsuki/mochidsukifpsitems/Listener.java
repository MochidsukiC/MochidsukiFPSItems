package jp.houlab.mochidsuki.mochidsukifpsitems;

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Objects;

import static jp.houlab.mochidsuki.mochidsukifpsitems.Main.config;
import static jp.houlab.mochidsuki.mochidsukifpsitems.Main.plugin;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event){
        switch (Objects.requireNonNull(event.getMaterial())) {
            case FIRE_CHARGE: {//ファイアーボール発射
                if((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.getPlayer().getCooldown(Material.FIRE_CHARGE) <= 0) {
                    Fireball fireball = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation(), Fireball.class);
                    //fireball.setShooter(event.getPlayer());
                    fireball.setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(1.5));
                    event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);

                    event.getPlayer().setCooldown(Material.FIRE_CHARGE, 10);
                }
            }
            case SPYGLASS: {//望遠鏡をのぞいたイベント
                if(event.getPlayer().getCooldown(Material.SPLASH_POTION) <= 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> V.useSniper.add(event.getPlayer()), 5L);
                }
                break;
            }
            case CREEPER_SPAWN_EGG: {
                Player player = event.getPlayer();
                if(event.getPlayer().getCooldown(Material.CREEPER_SPAWN_EGG) <= 0) {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        Creeper creeper = player.getWorld().spawn(event.getInteractionPoint(), Creeper.class);
                        V.Owner.put(creeper, player);
                        creeper.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 9999999, 10, true, true));
                        if(player.getScoreboard().getPlayerTeam(player) != null) {
                            creeper.customName(Component.text(player.getScoreboard().getPlayerTeam(player).getColor().name()));
                        }
                        player.getScoreboard().getEntityTeam(player).addEntity(creeper);
                        event.getItem().setAmount(event.getItem().getAmount() - 1);

                        player.setCooldown(Material.CREEPER_SPAWN_EGG, 40);
                    }
                }
            }
        }
    }

    @EventHandler
    public void ProjectileLaunchEvent(ProjectileLaunchEvent event){
        switch (Objects.requireNonNull(event.getEntity().getType())) {
            case SNOWBALL: {
                if (event.getEntity().getShooter() instanceof Player) {
                    Player player = (Player) event.getEntity().getShooter();
                    if (player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")) != null && player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getType() == Material.SPLASH_POTION) {
                        PotionMeta meta = (PotionMeta) player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getItemMeta();
                        V.SnowBallEffect.put(event.getEntity(), meta.getBasePotionType());
                        player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).setAmount(player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getAmount() - 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void PlayerToggleSneakEvent(PlayerToggleSneakEvent event){
        //スナイパートグル
        if(event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SPYGLASS) && event.isSneaking() && ((Entity)event.getPlayer()).isOnGround()){
            if(V.useSniper.contains(event.getPlayer())){
                if(event.getPlayer().getCooldown(Material.SPYGLASS) <= 0) {
                    if(event.getPlayer().getInventory().contains(Material.ARROW)) {
                        event.getPlayer().getInventory().removeItem(new ItemStack(Material.ARROW, 1));


                        Arrow ammo = event.getPlayer().getWorld().spawnArrow(event.getPlayer().getLocation().add(0, 1.65, 0), event.getPlayer().getLocation().getDirection(), 50, 1);
                        ammo.setShooter(event.getPlayer());
                        ammo.setCritical(true);
                        ammo.setColor(Color.GRAY);
                        ammo.setPierceLevel(3);
                        ammo.setDamage(0.15);
                        ammo.setShooter(event.getPlayer());
                        new DistanceKiller(ammo, event.getPlayer().getLocation(), 40).runTaskTimer(plugin, 0L, 1L);
                        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5, 0);
                        event.getPlayer().setCooldown(Material.SPYGLASS, 50);
                    }else {
                        event.getPlayer().sendTitle("" ,"弾切れ!",10,10,20);
                    }
                }
            }
        }
    }

    @EventHandler
    public void PlayerItemConsumeEvent(PlayerItemConsumeEvent event){
        //金リンゴ弱体化
        if(event.getItem().getType().equals(Material.ENCHANTED_GOLDEN_APPLE)){
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,1200,0,false,true,true));
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                event.getPlayer().removePotionEffect(PotionEffectType.REGENERATION);
                event.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
                event.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                event.getPlayer().removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,1200,1));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,1200,3));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,1200,0));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,1200,0));
            });
        }
        if(event.getItem().getType().equals(Material.MILK_BUCKET)&&event.getPlayer().hasPotionEffect(PotionEffectType.LUCK)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerItemHeldEvent(PlayerItemHeldEvent event){
        if(event.getPlayer().getInventory().getItem(event.getNewSlot()) != null &&event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.SPYGLASS)){
            event.getPlayer().setCooldown(Material.SPYGLASS, 50);
        }
        V.useSniper.remove(event.getPlayer());
    }

    @EventHandler
    public void ProjectileHitEvent(ProjectileHitEvent event){
        if(event.getEntity().getType() == EntityType.SNOWBALL){
            if(V.SnowBallEffect.containsKey(event.getEntity())){
                AreaEffectCloud effectCloud = event.getEntity().getLocation().getWorld().spawn(event.getEntity().getLocation(), AreaEffectCloud.class);
                effectCloud.setBasePotionType(V.SnowBallEffect.get(event.getEntity()));
            }
        }
    }

    @EventHandler
    public void EntityExplodeEvent(EntityExplodeEvent event){
        if(event.getEntity().getType() == EntityType.CREEPER){
            event.setCancelled(true);
            Creeper creeper = (Creeper)event.getEntity();
            creeper.removePotionEffect(PotionEffectType.SPEED);
            creeper.removePotionEffect(PotionEffectType.SLOW);
            event.getLocation().getWorld().createExplosion(event.getLocation(),2F,false);
            event.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE,event.getLocation(),10);
            event.getEntity().remove();
        }
    }
}

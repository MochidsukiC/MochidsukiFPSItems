package jp.houlab.mochidsuki.mochidsukifpsitems;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

import static jp.houlab.mochidsuki.mochidsukifpsitems.Main.plugin;

public class Listener implements org.bukkit.event.Listener {
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event){
        switch (Objects.requireNonNull(event.getMaterial())) {
            case FIRE_CHARGE: {//ファイアーボール発射
                Fireball fireball = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation(), Fireball.class);
                //fireball.setShooter(event.getPlayer());
                fireball.setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(1.5));
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            }
            case SPYGLASS://望遠鏡をのぞいたイベント
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> V.useSniper.add(event.getPlayer()),5L);
                break;

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
}

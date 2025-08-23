package jp.houlab.mochidsuki.mochidsukifpsitems;

import com.destroystokyo.paper.event.entity.CreeperIgniteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.N;

import java.time.Duration;
import java.util.Objects;

import static jp.houlab.mochidsuki.mochidsukifpsitems.Main.config;
import static jp.houlab.mochidsuki.mochidsukifpsitems.Main.plugin;

/**
 * リスナークラス
 * @author Mochidski
 */
public class Listener implements org.bukkit.event.Listener {
    /**
     * アイテム使用を検知
     * @param event イベント
     */
    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        switch (Objects.requireNonNull(event.getMaterial())) {
            case FIRE_CHARGE: {//ファイアーボール発射
                if((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.getPlayer().getCooldown(Material.FIRE_CHARGE) <= 0) {
                    Fireball fireball = event.getPlayer().getWorld().spawn(event.getPlayer().getEyeLocation(), Fireball.class);
                    fireball.setShooter(event.getPlayer());
                    fireball.setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(1.5));
                    event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);

                    event.getPlayer().setCooldown(Material.FIRE_CHARGE, 10);
                }
                break;
            }
            case SPYGLASS: {//望遠鏡をのぞいたイベント
                if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) && event.getPlayer().getCooldown(Material.SPYGLASS) <= 0 && event.getPlayer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> V.useSniper.add(event.getPlayer()), 5L);
                }
                break;
            }
            case CREEPER_SPAWN_EGG: {//クリーパーズトラップ
                if(event.getPlayer().getCooldown(Material.CREEPER_SPAWN_EGG) <= 0) {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        event.setCancelled(true);
                        Creeper creeper = player.getWorld().spawn(event.getInteractionPoint(), Creeper.class);
                        V.addOwner(player,creeper);
                        creeper.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 9999999, 10, true, true));
                        if(player.getScoreboard().getPlayerTeam(player) != null) {
                            creeper.customName(Component.text(player.getScoreboard().getPlayerTeam(player).getColor().name()));
                            player.getScoreboard().getEntityTeam(player).addEntity(creeper);
                        }
                        event.getItem().setAmount(event.getItem().getAmount() - 1);

                        if(V.getOwnerEntities(player).size() >= 7) {
                            V.removeEntity(V.getOwnerEntities(player).get(0));
                        }

                        player.setCooldown(Material.CREEPER_SPAWN_EGG, 40);
                    }
                }
                break;
            }

            case END_PORTAL_FRAME:{
                if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
                    if(!player.isSneaking()){
                        player.openAnvil(null,true).setTitle("モバイルアンビル");

                        player.getWorld().playSound(player.getLocation(),Sound.BLOCK_ANVIL_PLACE,0.4f,0.5f);
                        player.getWorld().playSound(player.getLocation(),Sound.BLOCK_FIRE_AMBIENT,1,2);

                    }else {
                        InventoryView smithingInventory = player.openSmithingTable(null,true);
                        smithingInventory.setTitle("モバイルスミッシングテーブル");
                        ItemStack itemStack = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
                        smithingInventory.getTopInventory().setItem(0,itemStack);

                        player.getWorld().playSound(player.getLocation(),Sound.BLOCK_IRON_TRAPDOOR_OPEN,1,0);
                        player.getWorld().playSound(player.getLocation(),Sound.ITEM_FLINTANDSTEEL_USE,1,0.6f);
                    }
                }
                    break;
            }
            case SNOWBALL:{
                if(event.getAction().isRightClick()){
                    if(player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")) == null || !(player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getType().equals(Material.SPLASH_POTION) || player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getType().equals(Material.SPECTRAL_ARROW) || player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getType().equals(Material.CREEPER_SPAWN_EGG)) ){
                        Title title = Title.title(Component.text(""), Component.text("スノーボールスロットがに対応アイテムが入っていません").color(NamedTextColor.RED), Title.Times.times(Duration.ZERO,Duration.ofSeconds(2),Duration.ofMillis(100)));
                        player.showTitle(title);
                        event.setCancelled(true);
                        break;
                    }
                    if(player.getCooldown(Material.SNOWBALL) > 0) {
                        event.setCancelled(true);
                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * 雪玉発射時にポーション効果をバックアップする
     * @param event イベント
     */
    @EventHandler
    public void ProjectileLaunchEvent(ProjectileLaunchEvent event){
        switch (Objects.requireNonNull(event.getEntity().getType())) {
            case SNOWBALL: {
                if (event.getEntity().getShooter() instanceof Player) {
                    Player player = (Player) event.getEntity().getShooter();
                    if (player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")) != null) {

                        switch (player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getType()) {
                            case SPLASH_POTION: {
                                PotionMeta meta = (PotionMeta) player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getItemMeta();
                                V.SnowBallEffect.put(event.getEntity(), meta.getBasePotionType());
                                player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).setAmount(player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getAmount() - 1);
                                break;
                            }
                            case SPECTRAL_ARROW:{
                                event.getEntity().addScoreboardTag("ScanBall");
                                player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).setAmount(player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getAmount() - 1);
                                break;
                            }
                            case CREEPER_SPAWN_EGG:{
                                player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).setAmount(player.getInventory().getItem(config.getInt("SNOWBALL.SLOT")).getAmount() - 1);
                                player.setCooldown(Material.SNOWBALL,40);

                                Creeper creeper = event.getLocation().getWorld().spawn(event.getLocation(), Creeper.class);
                                creeper.addScoreboardTag("canDestroy");
                                if(player.getScoreboard().getPlayerTeam(player) != null) {
                                    creeper.customName(Component.text(player.getScoreboard().getPlayerTeam(player).getColor().name()));
                                    player.getScoreboard().getEntityTeam(player).addEntity(creeper);
                                }
                                Snowball snowball = (Snowball) event.getEntity();
                                new BukkitRunnable(){
                                    int t = 0;
                                    Location location = snowball.getLocation().clone();

                                    @Override
                                    public void run() {
                                        RayTraceResult result = null;

                                        try {
                                            if(t>0) {
                                                result = snowball.getWorld().rayTraceBlocks(snowball.getLocation(), location.toVector().subtract(snowball.getLocation().toVector()), 2, FluidCollisionMode.NEVER, true);
                                            }
                                        }catch (Exception ignored){}

                                        if(snowball.getLocation().getBlock().getType().equals(Material.AIR) &&( result == null || result.getHitBlock() == null)) {
                                            location = snowball.getLocation().clone();
                                        }
                                        if(snowball.isValid()) {
                                            creeper.teleport(location);
                                        }


                                        if(t>70){
                                            creeper.ignite();
                                        }

                                        t++;

                                        if(creeper.isDead()){
                                            cancel();
                                        }
                                    }
                                }.runTaskTimer(plugin,0L,1L);
                            }
                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * スナイパーを発射する
     * @param event イベント
     */
    @EventHandler
    public void PlayerToggleSneakEvent(PlayerToggleSneakEvent event){
        //スナイパートグル
        if(event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.SPYGLASS) && event.isSneaking() && ((Entity)event.getPlayer()).isOnGround()){
            if(V.useSniper.contains(event.getPlayer())){
                if(event.getPlayer().getCooldown(Material.SPYGLASS) <= 0) {
                    if(event.getPlayer().getInventory().contains(Material.ARROW)) {
                        event.getPlayer().getInventory().removeItem(new ItemStack(Material.ARROW, 1));


                        Arrow ammo = event.getPlayer().getWorld().spawnArrow(event.getPlayer().getLocation().add(0, 1.65, 0), event.getPlayer().getLocation().getDirection(), 50, 0);
                        ammo.setShooter(event.getPlayer());
                        ammo.setCritical(true);
                        ammo.setColor(Color.GRAY);
                        ammo.setPierceLevel(3);
                        ammo.setDamage(0.15);
                        ammo.addScoreboardTag("fromSniper");
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

    /**
     *上位金リンゴの効果時間を短縮する
     * @param event イベント
     */
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
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,600,1));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,600,3));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,600,0));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,600,0));
            });
        }
        if(event.getItem().getType().equals(Material.MILK_BUCKET)&&event.getPlayer().hasPotionEffect(PotionEffectType.LUCK)){
            event.setCancelled(true);
        }
    }

    /**
     * スナイパーを持った処理
     * @param event
     */
    @EventHandler
    public void PlayerItemHeldEvent(PlayerItemHeldEvent event){
        if(!event.isCancelled()) {
            if (event.getPlayer().getInventory().getItem(event.getNewSlot()) != null && event.getPlayer().getInventory().getItem(event.getNewSlot()).getType().equals(Material.SPYGLASS)) {
                event.getPlayer().setCooldown(Material.SPYGLASS, 50);
            }
            V.useSniper.remove(event.getPlayer());
        }
    }

    /**
     * 雪玉の着弾処理
     * @param event
     */
    @EventHandler
    public void ProjectileHitEvent(ProjectileHitEvent event){
        if(event.getEntity().getType() == EntityType.SNOWBALL){
            Snowball snowball = (Snowball) event.getEntity();
            if(V.SnowBallEffect.containsKey(event.getEntity())){
                AreaEffectCloud effectCloud = event.getEntity().getLocation().getWorld().spawn(event.getEntity().getLocation(), AreaEffectCloud.class);
                effectCloud.setBasePotionType(V.SnowBallEffect.get(event.getEntity()));
            }else if (event.getEntity().getScoreboardTags().contains("ScanBall")){
                if(event.getEntity().getShooter() instanceof Player) {
                    Player thrower = (Player) event.getEntity().getShooter();
                    int numberOfPeople = 0;
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (!player.getName().equals(thrower.getName()) && ( thrower.getScoreboard().getEntityTeam(thrower) == null || (thrower.getScoreboard().getEntityTeam(thrower) != null && !thrower.getScoreboard().getEntityTeam(thrower).getEntries().contains(player.getScoreboardEntryName())) ) && snowball.getLocation().distance(player.getLocation()) < config.getInt("SpectralArrowScan.Distance") && (player.getGameMode().equals(GameMode.SURVIVAL)||player.getGameMode().equals(GameMode.ADVENTURE))){
                            numberOfPeople++;
                            player.sendMessage("検知された!");
                            player.playSound(player,Sound.BLOCK_SCULK_SENSOR_CLICKING,1,1);
                            if(config.getBoolean("SpectralArrowScan.Glow")){
                                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,1,0,false,false,true));
                            }
                        }
                    }
                    thrower.sendMessage("敵を"+numberOfPeople+"人検知");
                    thrower.playSound(thrower,Sound.BLOCK_NOTE_BLOCK_BIT,0.7f,0.5946f+(float) numberOfPeople/20f);
                    thrower.playSound(thrower,Sound.BLOCK_NOTE_BLOCK_BIT,0.7f,0.1f+(float)numberOfPeople/20f);
                }

            }
        }
    }

    /**
     * クリーパーズトラップ爆発処理
     * @param event
     */
    @EventHandler
    public void EntityExplodeEvent(EntityExplodeEvent event){
        switch (event.getEntityType()){
            case CREEPER:{
                event.setCancelled(true);
                Creeper creeper = (Creeper)event.getEntity();
                creeper.removePotionEffect(PotionEffectType.SPEED);
                creeper.removePotionEffect(PotionEffectType.SLOW);
                boolean b = creeper.getScoreboardTags().contains("canDestroy");
                event.getLocation().getWorld().createExplosion(event.getLocation(),2F,false,(config.getBoolean("CreepersGrenadeFire") && creeper.getScoreboardTags().contains("canDestroy")));
                event.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_HUGE,event.getLocation(),10);
                event.getEntity().remove();
                break;
            }
            case FIREBALL:{
                event.setCancelled(true);
                event.getLocation().getWorld().createExplosion(event.getLocation(),0F,false,false);
                event.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL,event.getLocation(),10);
                event.getEntity().remove();
                break;
            }
        }
    }

    @EventHandler
    public void InventoryCloseEvent(InventoryCloseEvent event){

        if(event.getView().getTitle().equals("モバイルスミッシングテーブル")) {
            if (event.getInventory().getItem(0) != null) {
                event.getInventory().getItem(0).setAmount(event.getInventory().getItem(0).getAmount() - 1);
            }
        }
    }

}

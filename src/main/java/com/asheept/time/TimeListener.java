package com.asheept.time;


import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TimeListener implements Listener
{
    private final TimePlugin plugin;


    TimeListener(TimePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        try {
            TimeConfig.saveConfig(player);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();
        int movX = event.getFrom().getBlockX() - event.getTo().getBlockX();
        int movZ = event.getFrom().getBlockZ() - event.getTo().getBlockZ();

        if(plugin.eliminatePlayer.contains(player.getUniqueId()))
            return;


        File file = new File(TimePlugin.getInstance().getDataFolder() + File.separator + player.getName() + ".yml");
        YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        String timeString = fileConfig.get(player.getName() + ".time").toString();
        int time = Integer.parseInt(timeString);


        if (Math.abs(movX) > 0 || Math.abs(movZ) > 0)
        {
            plugin.stopPlayer.remove(player.getUniqueId());
        }

        if (Math.abs(movX) == 0 || Math.abs(movZ) == 0)
        {
            if(!plugin.stopPlayer.contains(player.getUniqueId()))
            {
                plugin.stopPlayer.add(player.getUniqueId());
                plugin.countPlayer.put(player.getUniqueId(), time);
            }
        }
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) throws IOException
    {
        Entity entity = event.getEntity();

        if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
        {
            if(entity instanceof  Player)
            {
                Player player = (Player) entity;
                double damage = event.getFinalDamage();

                if(player != null)
                {
                    if(damage >= 2)
                    {
                        plugin.decreaseTime(player, 20);
                        player.sendMessage("피해를 입어 §b1§r초를 잃었습니다!");
                        player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_HURT, 0.5F, 0.5F);
                    }
                }
            }
        }
        if(entity instanceof Player)
        {
            Player player = (Player) entity;
            if(player != null)
            {
                double health = player.getHealth();
                double damage = event.getFinalDamage();

                if(event instanceof EntityDamageByEntityEvent)
                {
                    Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

                    if(damager instanceof  Player)
                    {
                        Player damagerPlayer = (Player) damager;

                        if(damagerPlayer != null)
                        {
                            if(!player.isBlocking())
                            {
                                if(health <= damage)
                                {
                                    event.setCancelled(true);
                                    plugin.eliminate(player);

                                    plugin.increaseTime(damagerPlayer, 200);
                                    damagerPlayer.sendTitle(" ", "상대방을 죽여 10초를 획득했습니다!", 5, 30, 5);
                                }

                                if(damage >= 2)
                                {
                                    plugin.decreaseTime(player, 20);
                                    player.sendMessage("피해를 입어 §b1§r초를 잃었습니다!");
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 0.5F);
                                }
                            }
                        }
                    }
                    else if(damager instanceof Monster)
                    {
                        if(!player.isBlocking())
                        {
                            plugin.decreaseTime(player, 10);
                            player.sendMessage("피해를 입어 §b0.5§r초를 잃었습니다!");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.2F, 0.5F);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = (Player) event.getEntity();

        plugin.eliminate(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws IOException
    {
        Player player =event.getPlayer();
        Action action = event.getAction();

        if (event.getHand() == EquipmentSlot.HAND)
        {
            if (action.equals(Action.RIGHT_CLICK_BLOCK) || (action.equals(Action.RIGHT_CLICK_AIR)))
            {
                if (player.getInventory().getItemInMainHand().getType() == Material.CLOCK)
                {
                    if(!plugin.makePlayer.contains(player.getUniqueId()))
                    {
                        plugin.makePlayer.add(player.getUniqueId());
                        player.sendTitle(" ", ChatColor.of(new Color(0xE2F1FF)) + "시간이 증가했습니다!");
                        plugin.increaseTime(player, 100);
                        player.getInventory().getItemInMainHand().setAmount(0);
                    }
                    else
                    {
                        player.sendTitle(" ", ChatColor.of(new Color(0xFF2222)) + "더 이상 사용할 수 없습니다!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) throws IOException
    {
        if (event.getDamager() instanceof Arrow)
        {
            Arrow arrow = (Arrow) event.getDamager();
            if (arrow.getShooter() instanceof Player)
            {
                Player shooter = (Player) arrow.getShooter();
                if (event.getEntity() instanceof Player)
                {
                    Player victim = (Player) event.getEntity();
                    if (victim instanceof Player)
                    {
                        if (victim.isBlocking())
                        {
                            arrow.remove();
                        }

                        shooter.sendMessage(" " + event.getFinalDamage());

                        if(event.getFinalDamage() >= 3)
                        {
                            plugin.decreaseTime(victim, 20);
                            victim.sendMessage("피해를 입어 §b1§r초를 잃었습니다!");
                            victim.playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 0.5F);
                        }
                    }
                }
            }
        }
    }
}


package com.asheept.time;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class TimePlugin extends JavaPlugin implements CommandExecutor {
    public static TimePlugin instance;

    public static TimePlugin getInstance() {
        return instance;
    }

    public HashSet<UUID> stopPlayer = new HashSet<>();
    public HashSet<UUID> eliminatePlayer = new HashSet<>();
    public HashMap<UUID, Integer> countPlayer = new HashMap<>();
    public HashSet<UUID> makePlayer = new HashSet<>();

    public static final net.md_5.bungee.api.ChatColor SUHYEN_COLOR = ChatColor.of(new Color(0x863CE8));

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new TimeListener(this), this);
        saveConfig();

        new BukkitRunnable() {

            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (countPlayer.containsKey(player.getUniqueId())) {
                        int time = countPlayer.get(player.getUniqueId());
                        --time;

                        int seconds = time / 20;

                        countPlayer.put(player.getUniqueId(), time);

                        player.sendActionBar("시간 : " + ChatColor.of(new Color(0xD1DBFF)) + seconds);

                        if (time == 0) {
                            eliminate(player);
                        }
                    }
                });
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (label.equalsIgnoreCase("time")) {
            Bukkit.getOnlinePlayers().forEach(players -> {
                File file = new File(TimePlugin.getInstance().getDataFolder() + File.separator + players.getName() + ".yml");
                YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
                String timeString = fileConfig.get(players.getName() + ".time").toString();
                try {
                    fileConfig.set(players.getName() + ".time", TimeConfig.time);
                    fileConfig.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (label.equalsIgnoreCase("alive")) {
            eliminatePlayer.remove(player.getUniqueId());
            player.setGameMode(GameMode.SURVIVAL);
        }

        return true;
    }

    public void eliminate(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        eliminatePlayer.add(player.getUniqueId());
        countPlayer.remove(player.getUniqueId());
        stopPlayer.remove(player.getUniqueId());


        Bukkit.getOnlinePlayers().forEach(players -> {
            players.sendTitle(" ", replaceName(player) + " §c탈락", 5, 30, 5);
        });
    }

    public void increaseTime(Player player, int value) throws IOException {
        File file = new File(TimePlugin.getInstance().getDataFolder() + File.separator + player.getName() + ".yml");
        YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        String timeString = fileConfig.get(player.getName() + ".time").toString();
        int time = Integer.parseInt(timeString);

        fileConfig.set(player.getName() + ".time", time + value);
        fileConfig.save(file);
    }

    public void decreaseTime(Player player, int value) throws IOException {
        File file = new File(TimePlugin.getInstance().getDataFolder() + File.separator + player.getName() + ".yml");
        YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        String timeString = fileConfig.get(player.getName() + ".time").toString();
        int time = Integer.parseInt(timeString);

        fileConfig.set(player.getName() + ".time", time - value);
        fileConfig.save(file);
    }

    public static String replaceName(Player player) {
        String name = player.getName();
        if (name.equalsIgnoreCase("ehdgh141")) {
            name = name.replace(player.getName(), "§a공룡§r");
        } else if (name.equalsIgnoreCase("Sleepground")) {
            name = name.replace(player.getName(), "§b잠뜰§r");
        } else if (name.equalsIgnoreCase("DUCKGAE")) {
            name = name.replace(player.getName(), "§6덕개§r");
        } else if (name.equalsIgnoreCase("SUHYEN")) {
            name = name.replace(player.getName(), SUHYEN_COLOR + "수현§r");
        } else if (name.equalsIgnoreCase("DDony")) {
            name = name.replace(player.getName(), "§d또니§r");
        } else if (name.equalsIgnoreCase("lLeeShin")) {
            name = name.replace(player.getName(), "§d또니§r");
        }
        return name;
    }


}

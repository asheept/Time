package com.asheept.time;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class TimeConfig
{
    public static int time = 620;

    public static void saveConfig(Player player) throws IOException
    {
        File file = new File(TimePlugin.getInstance().getDataFolder() + File.separator + player.getName() + ".yml");
        if (!file.exists()) {
            YamlConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
            fileConfig.set(player.getName() + ".uuid", player.getUniqueId().toString());
            fileConfig.set(player.getName() + ".time", Integer.valueOf(time));
            fileConfig.save(file);
            Logger logger = Bukkit.getLogger();
            logger.info(ChatColor.RED + "[Config] Create configuration  " + player.getName());
        }
    }
}
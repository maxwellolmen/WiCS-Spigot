package com.maxwellolmen.wics.spigot;

import com.maxwellolmen.wics.spigot.mail.MailListener;
import com.maxwellolmen.wics.spigot.mail.MailManager;
import com.maxwellolmen.wics.spigot.sql.SQLManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class WiCSPlugin extends JavaPlugin {

    public static SQLManager sqlManager;
    public static MailManager mailManager;

    private List<Manager> managers;

    @Override
    public void onEnable() {
        sqlManager = new SQLManager(this);
        mailManager = new MailManager(this);

        managers = new ArrayList<>();
        managers.add(sqlManager);
        managers.add(mailManager);

        // Run after server has properly initialize
        // This avoids issues like querying Bukkit.getWorld() before world is loading
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Manager manager : managers) {
                    manager.init();
                }
            }
        }.runTaskLater(this, 0);

        // Interval for managers every 5 minutes
        // Useful for auto-save on managers with live data
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Manager manager : managers) {
                    manager.interval();
                }
            }
        }.runTaskTimer(this, 6000, 6000);
    }

    @Override
    public void onDisable() {
        for (Manager manager : managers) {
            manager.disable();
        }
    }
}
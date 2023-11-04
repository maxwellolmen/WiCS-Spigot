package com.maxwellolmen.wics.spigot.mail;

import com.maxwellolmen.wics.spigot.Manager;
import com.maxwellolmen.wics.spigot.WiCSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.*;

public class MailManager implements Manager {

    private WiCSPlugin plugin;
    private MailListener listener;

    private Set<UUID> openMailboxes;
    private Map<UUID, Mailbox> mailboxes;
    private Map<Location, Mailbox> locations;

    public MailManager(WiCSPlugin plugin) {
        this.plugin = plugin;
        this.listener = new MailListener(this);
        this.openMailboxes = new TreeSet<>();
        this.mailboxes = new HashMap<>();
        this.locations = new HashMap<>();
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        Objects.requireNonNull(plugin.getCommand("mail")).setExecutor(new MailCommand(this));

        try {
            mailboxes = WiCSPlugin.sqlManager.loadMailboxes();
            locations = WiCSPlugin.sqlManager.loadLocations(mailboxes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interval() {
        try {
            WiCSPlugin.sqlManager.saveMailboxes(mailboxes, locations);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disable() {
        try {
            WiCSPlugin.sqlManager.saveMailboxes(mailboxes, locations);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void markOpen(UUID uuid) {
        openMailboxes.add(uuid);
    }

    public void processClose(UUID uuid, Inventory inventory) {
        if (openMailboxes.contains(uuid)) {
            mailboxes.get(uuid).close(inventory);
            openMailboxes.remove(uuid);
        }
    }

    public void createMailbox(UUID uuid, Location location) {
        Mailbox mailbox = new Mailbox(uuid);

        mailboxes.put(uuid, mailbox);
        locations.put(location, mailbox);
    }

    public void openMailbox(Player player) {
        if (mailboxes.containsKey(player.getUniqueId())) {
            mailboxes.get(player.getUniqueId()).open(player);
            markOpen(player.getUniqueId());
        }
    }

    public void destroyMailbox(UUID uuid) {
        if (mailboxes.get(uuid).getItems().size() == 0) {
            locations.remove(getLocation(mailboxes.get(uuid)));
            mailboxes.remove(uuid);
        }
    }

    public Mailbox getMailbox(UUID uuid) {
        return mailboxes.getOrDefault(uuid, null);
    }

    public void refresh(Mailbox mailbox) {
        for (UUID uuid : openMailboxes) {
            if (mailboxes.get(uuid) == mailbox) {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    player.closeInventory();
                    mailbox.open(player);
                }
            }
        }
    }

    public void processChestOpen(Player player, Location location, PlayerInteractEvent event) {
        if (locations.containsKey(location)) {
            locations.get(location).open(player);
            event.setCancelled(true);
        }
    }

    public Mailbox getMailbox(Location location) {
        return locations.getOrDefault(location, null);
    }

    public Location getLocation(Mailbox mailbox) {
        for (Map.Entry<Location, Mailbox> entry : locations.entrySet()) {
            if (entry.getValue() == mailbox) {
                return entry.getKey();
            }
        }

        return null;
    }

    public void setMailboxLocation(Location location, Mailbox mailbox) {
        locations.remove(getLocation(mailbox));
        locations.put(location, mailbox);
    }
}
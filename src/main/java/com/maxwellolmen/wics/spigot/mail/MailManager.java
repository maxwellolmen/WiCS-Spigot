package com.maxwellolmen.wics.spigot.mail;

import com.maxwellolmen.wics.spigot.Manager;
import com.maxwellolmen.wics.spigot.WiCSPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.SQLException;
import java.util.*;

public class MailManager implements Manager {

    private WiCSPlugin plugin;
    private MailListener listener;

    private Set<UUID> openMailboxes;
    private Map<UUID, Mailbox> mailboxes;

    public MailManager(WiCSPlugin plugin) {
        this.plugin = plugin;
        this.listener = new MailListener(this);
        this.openMailboxes = new TreeSet<>();
        this.mailboxes = new HashMap<>();
    }

    public void init() {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        // mailboxes = WiCSPlugin.sqlManager.loadMailboxes();
    }

    @Override
    public void interval() {
        try {
            WiCSPlugin.sqlManager.saveMailboxes(mailboxes);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disable() {
        try {
            WiCSPlugin.sqlManager.saveMailboxes(mailboxes);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    public void createMailbox(UUID uuid) {
        mailboxes.put(uuid, new Mailbox());
    }

    public void openMailbox(Player player) {
        if (mailboxes.containsKey(player.getUniqueId())) {
            mailboxes.get(player.getUniqueId()).open(player);
            markOpen(player.getUniqueId());
        }
    }

    public void destroyMailbox(UUID uuid) {
        if (mailboxes.get(uuid).getItems().size() == 0) {
            mailboxes.remove(uuid);
        }
    }
}
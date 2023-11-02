package com.maxwellolmen.wics.spigot.mail;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class MailListener implements Listener {

    private MailManager manager;

    public MailListener(MailManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        manager.processClose(event.getPlayer().getUniqueId(), event.getInventory());
    }
}
package com.maxwellolmen.wics.spigot.mail;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class MailListener implements Listener {

    private MailManager manager;

    public MailListener(MailManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        manager.processClose(event.getPlayer().getUniqueId(), event.getInventory());
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null || block.getType() != Material.CHEST) {
            return;
        }

        event.setCancelled(true);
        manager.processChestOpen(event.getPlayer(), block.getLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Mailbox mailbox = manager.getMailbox(event.getBlock().getLocation());

        if (mailbox == null) {
            return;
        }

        if (mailbox.getOwner() != event.getPlayer().getUniqueId()) {
            return;
        }

        if (mailbox.getItems().size() != 0) {
            event.getPlayer().sendMessage(ChatColor.RED + "You still have items in your mailbox! You can't destroy it yet.");
            return;
        }

        manager.destroyMailbox(event.getPlayer().getUniqueId());
        event.getPlayer().sendMessage(ChatColor.GREEN + "Mailbox destroyed.");
    }
}
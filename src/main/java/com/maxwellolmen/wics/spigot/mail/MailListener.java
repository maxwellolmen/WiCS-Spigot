package com.maxwellolmen.wics.spigot.mail;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MailListener implements Listener {

    private MailManager manager;

    public MailListener(MailManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Mailbox mailbox = manager.getMailbox(event.getPlayer().getUniqueId());

        if (mailbox != null && mailbox.getItems().size() > 0) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "Heads up, you have items in your mailbox!");
        }

        List<String> pendingNotifs = manager.getPendingNotifs(event.getPlayer().getUniqueId());

        if (pendingNotifs != null) {
            event.getPlayer().sendMessage(ChatColor.GOLD + "While you were gone, you were sent " + pendingNotifs.size() + " item" + (pendingNotifs.size() > 1 ? "s" : "") + ".");

            Map<String, Integer> senderAmounts = new TreeMap<>();

            for (String pendingNotif : pendingNotifs) {
                senderAmounts.put(pendingNotif, senderAmounts.getOrDefault(pendingNotif, 0) + 1);
            }

            for (String sender : senderAmounts.keySet()) {
                event.getPlayer().sendMessage(ChatColor.GOLD + "  - from " + sender + ": " + senderAmounts.get(sender) + " item" + (senderAmounts.get(sender) > 1 ? "s" : ""));
            }
        }
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

        if (event.getPlayer().isSneaking()) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null || block.getType() != Material.CHEST) {
            return;
        }

        manager.processChestOpen(event.getPlayer(), block.getLocation(), event);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Mailbox mailbox = manager.getMailbox(event.getBlock().getLocation());

        if (mailbox == null) {
            return;
        }

        if (mailbox.getOwner().equals(event.getPlayer().getUniqueId())) {
            if (mailbox.getItems().size() != 0) {
                event.getPlayer().sendMessage(ChatColor.RED + "You still have items in your mailbox! You can't destroy it yet.");
                event.setCancelled(true);
                return;
            }

            manager.destroyMailbox(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Mailbox destroyed.");
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Mailbox mailbox = manager.getMailbox(event.getBlock().getLocation());

        if (mailbox != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPiston(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();

        for (Block block : blocks) {
            Mailbox mailbox = manager.getMailbox(block.getLocation());

            if (mailbox != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPiston(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();

        for (Block block : blocks) {
            Mailbox mailbox = manager.getMailbox(block.getLocation());

            if (mailbox != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Mailbox mailbox = manager.getMailbox(event.getBlock().getLocation());

        if (mailbox != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        Mailbox mailbox = manager.getMailbox(event.getBlock().getLocation());

        if (mailbox != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Mailbox mailbox = manager.getMailbox(event.getBlock().getLocation());

        if (mailbox != null) {
            event.setCancelled(true);
        }
    }
}
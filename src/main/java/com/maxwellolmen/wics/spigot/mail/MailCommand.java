package com.maxwellolmen.wics.spigot.mail;

import com.maxwellolmen.wics.spigot.WiCSPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MailCommand implements CommandExecutor {

    private MailManager manager;

    public MailCommand(MailManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("/" + label + " cannot be run from the console.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED +
                    "Usage:\n" +
                    "  /" + label + " send <player> - Sends the item in your hand to the given player's mailbox.\n" +
                    "  /" + label + " set - Sets your mailbox to the current chest.");
            return true;
        }

        if (args[0].equalsIgnoreCase("send")) {
            if (args.length == 1) {
                player.sendMessage(ChatColor.RED + "Usage: /" + label + " send <player>");
                return true;
            }

            OfflinePlayer target =  Bukkit.getOfflinePlayer(args[1]);

            if (!target.hasPlayedBefore() && Bukkit.getPlayer(args[1]) == null) {
                player.sendMessage(ChatColor.RED + "That player has never been online.");
                return true;
            }

            Mailbox mailbox = WiCSPlugin.mailManager.getMailbox(target.getUniqueId());

            if (mailbox == null) {
                player.sendMessage(ChatColor.RED + "That player doesn't have a mailbox.");
                return true;
            }

            if (manager.isOpen(target.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "That player is currently in their mailbox! Try again later...");
                return true;
            }

            if (mailbox.getItems().size() >= 54) {
                player.sendMessage(ChatColor.RED + "Sorry! That player's mailbox is full.");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Your hand is empty!");
                return true;
            }

            player.getInventory().remove(item);
            mailbox.addItem(item);

            WiCSPlugin.mailManager.refresh(mailbox);

            player.sendMessage(ChatColor.GREEN + "Item sent!");

            if (target.isOnline()) {
                ((Player) target).sendMessage(ChatColor.GOLD + "Hey! " + ChatColor.BOLD + player.getDisplayName() + ChatColor.GOLD + " just mailed you an item!");
            } else {
                manager.scheduleNotif(target.getUniqueId(), player.getDisplayName());
            }
        } else if (args[0].equalsIgnoreCase("set")) {
            Block block = player.getTargetBlockExact(10);

            if (block == null || block.getType() != Material.CHEST) {
                player.sendMessage(ChatColor.RED + "You must be looking at a regular chest!");
                return true;
            }

            Chest chest = (Chest) block.getState();

            if (!isInventoryEmpty(chest.getInventory())) {
                player.sendMessage(ChatColor.RED + "Chest has items! You can't set it as your mailbox.");
                return true;
            }

            Location location = block.getLocation();

            if (manager.getMailbox(location) != null) {
                player.sendMessage(ChatColor.RED + "This is already a mailbox!");
                return true;
            }

            if (manager.getMailbox(player.getUniqueId()) != null) {
                manager.setMailboxLocation(location, manager.getMailbox(player.getUniqueId()));
            } else {
                manager.createMailbox(player.getUniqueId(), location);
                player.sendMessage(ChatColor.GREEN + "New mailbox created! Right-click to open.");
            }

            player.sendMessage(ChatColor.GREEN + "Mailbox location set!");
        }

        return true;
    }

    public boolean isInventoryEmpty(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }
}
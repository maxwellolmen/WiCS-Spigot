package com.maxwellolmen.wics.spigot.mail;

import com.maxwellolmen.wics.spigot.WiCSPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MailCommand implements CommandExecutor {

    private MailManager manager;

    public MailCommand(MailManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("/mail cannot be run from the console.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED +
                    "Usage:\n" +
                    "  /mail send <player> - Sends the item in your hand to the given player's mailbox.\n" +
                    "  /mail set - Sets your mailbox to the current chest.");
            return true;
        }

        if (args[0].equalsIgnoreCase("send")) {
            if (args.length == 1) {
                player.sendMessage(ChatColor.RED + "Usage: /mail send <player>");
                return true;
            }

            OfflinePlayer target =  Bukkit.getOfflinePlayer(args[1]);

            if (!target.hasPlayedBefore()) {
                player.sendMessage(ChatColor.RED + "That player has never been online.");
                return true;
            }

            Mailbox mailbox = WiCSPlugin.mailManager.getMailbox(target.getUniqueId());

            if (mailbox == null) {
                player.sendMessage(ChatColor.RED + "That player doesn't have a mailbox.");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            player.getInventory().getItemInMainHand().setAmount(0);

            mailbox.addItem(item);
            WiCSPlugin.mailManager.refresh(mailbox);

            player.sendMessage(ChatColor.GREEN + "Item sent!");
        } else if (args[0].equalsIgnoreCase("set")) {
            Block block = player.getTargetBlockExact(10);

            if (block == null || block.getType() != Material.CHEST) {
                player.sendMessage(ChatColor.RED + "You must be looking at a regular chest!");
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
}
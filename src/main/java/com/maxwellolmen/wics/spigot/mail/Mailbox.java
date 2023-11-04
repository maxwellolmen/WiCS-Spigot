package com.maxwellolmen.wics.spigot.mail;

import com.maxwellolmen.wics.spigot.WiCSPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Mailbox {

    private List<ItemStack> items;
    private UUID owner;

    public Mailbox(UUID owner) {
        this.items = new ArrayList<>();
        this.owner = owner;
    }

    public void updateItems(List<ItemStack> items) {
        this.items = items;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, items.size() > 27 ? 54 : 27, player.getDisplayName() + "'s Mailbox");

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) != null && items.get(i).getType() != Material.AIR) {
                inventory.setItem(i, items.get(i));
            }
        }

        player.openInventory(inventory);
    }

    public void close(Inventory inventory) {
        items.clear();
        items.addAll(Arrays.asList(inventory.getContents()));
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void addItem(ItemStack item) {
        items.add(item);
        WiCSPlugin.mailManager.refresh(this);
    }

    public UUID getOwner() {
        return owner;
    }
}
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

public class Mailbox {

    private List<ItemStack> items;

    public Mailbox() {
        this(new ArrayList<>());
    }

    public Mailbox(List<ItemStack> items) {
        this.items = items;
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
}
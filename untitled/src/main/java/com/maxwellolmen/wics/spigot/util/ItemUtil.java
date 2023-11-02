package com.maxwellolmen.wics.spigot.util;

import com.google.gson.Gson;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static Gson gson = new Gson();

    public static String serialize(ItemStack item) {
        return gson.toJson(item);
    }

    public static ItemStack deserialize(String json) {
        return gson.fromJson(json, ItemStack.class);
    }
}
package com.maxwellolmen.wics.spigot.sql;

import com.maxwellolmen.wics.spigot.Manager;
import com.maxwellolmen.wics.spigot.WiCSPlugin;
import com.maxwellolmen.wics.spigot.mail.Mailbox;
import com.maxwellolmen.wics.spigot.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLManager implements Manager {

    private WiCSPlugin plugin;
    private Connection conn = null;

    public SQLManager(WiCSPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/wics.db");
    }

    public void verifyOpen() throws SQLException {
        if (conn == null || conn.isClosed()) {
            connect();
        }
    }

    public void init() {
        try {
            connect();
            initTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void interval() {}

    @Override
    public void disable() {

    }

    public void initTables() throws SQLException {
        verifyOpen();

        Statement st = conn.createStatement();
        st.execute("CREATE TABLE IF NOT EXISTS mailitems (uuid VARCHAR(64), item VARCHAR(2048));");
        st.execute("CREATE TABLE IF NOT EXISTS mailboxes (world VARCHAR(16), x INT(8), y INT(8), z INT(8), owner VARCHAR(64));");
        st.close();
    }

    public void saveMailboxes(Map<UUID, Mailbox> mailboxes, Map<Location, Mailbox> locations) throws SQLException {
        verifyOpen();

        Statement st = conn.createStatement();

        for (Map.Entry<UUID, Mailbox> entry : mailboxes.entrySet()) {
            UUID uuid = entry.getKey();
            Mailbox mailbox = entry.getValue();

            st.execute("DELETE FROM mailitems WHERE uuid=" + uuid.toString() + ";");

            for (ItemStack item : mailbox.getItems()) {
                st.execute("INSERT INTO mailitems (uuid, item) VALUES (" + uuid.toString() + ", " + ItemUtil.serialize(item) + ");");
            }
        }

        for (Map.Entry<Location, Mailbox> entry : locations.entrySet()) {
            Location location = entry.getKey();
            Mailbox mailbox = entry.getValue();

            st.execute("DELETE FROM mailboxes WHERE owner=" + mailbox.getOwner().toString() + ";");
            st.execute("INSERT INTO mailboxes (world, x, y, z, owner) VALUES (" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ", " + mailbox.getOwner().toString() + ");");
        }

        st.close();
    }

    public Map<Location, Mailbox> loadLocations(Map<UUID, Mailbox> mailboxes) throws SQLException {
        verifyOpen();

        Map<Location, Mailbox> locations = new HashMap<>();
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SELECT (world, x, y, z, owner) FROM mailboxes;");

        while (rs.next()) {
            World world = Bukkit.getWorld(rs.getString("world"));
            int x = rs.getInt("x");
            int y = rs.getInt("y");
            int z = rs.getInt("z");
            UUID owner = UUID.fromString(rs.getString("owner"));

            locations.put(new Location(world, x, y, z), mailboxes.get(owner));
        }

        rs.close();
        st.close();

        return locations;
    }

    public Map<UUID, Mailbox> loadMailboxes() throws SQLException {
        verifyOpen();

        Map<UUID, Mailbox> mailboxes = new HashMap<>();
        Statement st = conn.createStatement();

        ResultSet rs = st.executeQuery("SELECT (uuid, item) FROM mailitems;");

        while (rs.next()) {
            UUID uuid = UUID.fromString(rs.getString("uuid"));
            ItemStack item = ItemUtil.deserialize(rs.getString("item"));

            if (mailboxes.containsKey(uuid)) {
                mailboxes.get(uuid).addItem(item);
            } else {
                Mailbox mailbox = new Mailbox(uuid);
                mailbox.addItem(item);

                mailboxes.put(uuid, mailbox);
            }
        }

        rs.close();
        st.close();

        return mailboxes;
    }
}
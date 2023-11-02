package com.maxwellolmen.wics.spigot.sql;

import com.maxwellolmen.wics.spigot.Manager;
import com.maxwellolmen.wics.spigot.WiCSPlugin;
import com.maxwellolmen.wics.spigot.mail.Mailbox;
import com.maxwellolmen.wics.spigot.util.ItemUtil;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

public class SQLManager implements Manager {

    private WiCSPlugin plugin;
    private Connection conn = null;

    public SQLManager(WiCSPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "wics.db");
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
        Statement st = conn.createStatement();
        st.execute("CREATE TABLE IF NOT EXISTS mailboxes (uuid VARCHAR(64), item VARCHAR(2048));");
        st.close();
    }

    public void saveMailboxes(Map<UUID, Mailbox> mailboxes) throws SQLException {
        Statement st = conn.createStatement();

        for (Map.Entry<UUID, Mailbox> entry : mailboxes.entrySet()) {
            UUID uuid = entry.getKey();
            Mailbox mailbox = entry.getValue();

            st.execute("DELETE FROM mailboxes WHERE uuid=" + uuid.toString() + ";");

            for (ItemStack item : mailbox.getItems()) {
                st.execute("INSERT INTO mailboxes (uuid, item) VALUES (" + uuid.toString() + ", " + ItemUtil.serialize(item) + ");");
            }
        }

        st.close();
    }
}
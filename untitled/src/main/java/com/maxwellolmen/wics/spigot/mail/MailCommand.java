package com.maxwellolmen.wics.spigot.mail;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MailCommand implements CommandExecutor {

    private MailManager manager;

    public MailCommand(MailManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        return true;
    }
}
package dev.qdhshamiro.qdhworldguardtitle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class QDHWorldGuardTitleCommand implements CommandExecutor, TabCompleter {

    private final QDHWorldguardTitle plugin;

    public QDHWorldGuardTitleCommand(QDHWorldguardTitle plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return plugin.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return plugin.onTabComplete(sender, command, alias, args);
    }
}


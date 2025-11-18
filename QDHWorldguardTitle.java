package dev.qdhshamiro.qdhworldguardtitle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class QDHWorldguardTitle extends JavaPlugin {

    private static QDHWorldguardTitle instance;
    private RegionConfigManager regionConfigManager;

    private final AtomicBoolean enabledOverall = new AtomicBoolean(true);
    private final AtomicBoolean titleEnabled = new AtomicBoolean(true);
    private final AtomicBoolean subtitleEnabled = new AtomicBoolean(true);
    private final AtomicBoolean actionbarEnabled = new AtomicBoolean(true);
    private final AtomicBoolean messagesEnabled = new AtomicBoolean(true);

    // Fallback default resources (used only if embedded resources are missing in the JAR)
    private static final String DEFAULT_CONFIG = "settings:\n  async-processing: true\n  debug: false\n\ndefaults:\n  title-enter: \"&bEntering %region%\"\n  subtitle-enter: \"&7Welcome!\"\n  actionbar-enter: \"&fYou entered &b%region%\"\n  messages-enter:\n    - \"&a[INFO] &7You entered &b%region%\"\n  title-leave: \"&cLeaving %region%\"\n  subtitle-leave: \"&7Goodbye!\"\n  actionbar-leave: \"&fYou left &c%region%\"\n  messages-leave:\n    - \"&c[INFO] &7You left &c%region%\"\n";

    private static final String DEFAULT_REGIONS = "spawn:\n  title-enter: \"<#00aaff>Entering Spawn\"\n  subtitle-enter: \"&7Enjoy your stay!\"\n  actionbar-enter: \"<#00aaff>Welcome to Spawn\"\n  messages-enter:\n    - \"&bYou have entered Spawn.\"\n  title-leave: \"<#ff3333>Leaving Spawn\"\n  subtitle-leave: \"&7See you soon!\"\n  actionbar-leave: \"<#ff3333>You left Spawn\"\n  messages-leave:\n    - \"&cYou have left Spawn.\"\n";

    @Override
    public void onEnable() {
        instance = this;

        // Save embedded resources only if they exist in the JAR.
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        } else {
            getLogger().warning("Embedded resource 'config.yml' not found in JAR; writing fallback default config.yml to data folder.");
            writeStringToFile(new File(getDataFolder(), "config.yml"), DEFAULT_CONFIG);
        }

        if (getResource("regions.yml") != null) {
            saveResource("regions.yml", false);
        } else {
            getLogger().warning("Embedded resource 'regions.yml' not found in JAR; writing fallback regions.yml to data folder.");
            writeStringToFile(new File(getDataFolder(), "regions.yml"), DEFAULT_REGIONS);
        }

        regionConfigManager = new RegionConfigManager(this);

        // initialize external API
        QDHWorldguardTitleAPI.init(this);

        Bukkit.getPluginManager().registerEvents(new RegionListener(this), this);

        // Register command
        PluginCommand cmd = getCommand("qdhworldguardtitle");
        if (cmd != null) {
            QDHWorldGuardTitleCommand handler = new QDHWorldGuardTitleCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        // load initial flags from config
        enabledOverall.set(getConfig().getBoolean("settings.enabled", true));
        titleEnabled.set(getConfig().getBoolean("defaults.enable-title", true));
        subtitleEnabled.set(getConfig().getBoolean("defaults.enable-subtitle", true));
        actionbarEnabled.set(getConfig().getBoolean("defaults.enable-actionbar", true));
        messagesEnabled.set(getConfig().getBoolean("defaults.enable-messages", true));

        getLogger().info("QDHWorldguardTitle enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("QDHWorldguardTitle disabled.");
    }

    private void writeStringToFile(File file, String content) {
        if (file == null || content == null) return;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException ex) {
            getLogger().severe("Failed to write fallback resource file " + file.getName() + ": " + ex.getMessage());
        }
    }

    // -----------------------------
    // Runtime API (toggles)
    // -----------------------------

    public void setOverallEnabled(boolean v) {
        enabledOverall.set(v);
    }

    public boolean isOverallEnabled() {
        return enabledOverall.get();
    }

    public void setTitleEnabled(boolean v) {
        titleEnabled.set(v);
    }

    public boolean isTitleEnabled() {
        return titleEnabled.get();
    }

    public void setSubtitleEnabled(boolean v) {
        subtitleEnabled.set(v);
    }

    public boolean isSubtitleEnabled() {
        return subtitleEnabled.get();
    }

    public void setActionbarEnabled(boolean v) {
        actionbarEnabled.set(v);
    }

    public boolean isActionbarEnabled() {
        return actionbarEnabled.get();
    }

    public void setMessagesEnabled(boolean v) {
        messagesEnabled.set(v);
    }

    public boolean isMessagesEnabled() {
        return messagesEnabled.get();
    }

    // -----------------------------
    // Command handling: reload/status/toggle
    // -----------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("QDHWorldguardTitle v" + getDescription().getVersion());
            sender.sendMessage("Usage: /qdhworldguardtitle <reload|status|toggle> [feature] [on|off]");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("qdhworldguardtitle.reload")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            reloadConfig();
            regionConfigManager = new RegionConfigManager(this);
            sender.sendMessage("QDHWorldguardTitle reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            if (!sender.hasPermission("qdhworldguardtitle.status")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            sender.sendMessage("QDHWorldguardTitle v" + getDescription().getVersion());
            sender.sendMessage("Plugin overall enabled: " + isOverallEnabled());
            sender.sendMessage("Title enabled: " + isTitleEnabled());
            sender.sendMessage("Subtitle enabled: " + isSubtitleEnabled());
            sender.sendMessage("Actionbar enabled: " + isActionbarEnabled());
            sender.sendMessage("Messages enabled: " + isMessagesEnabled());
            int regions = (regionConfigManager != null) ? regionConfigManager.getRegionCount() : 0;
            sender.sendMessage("Loaded regions: " + regions);
            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("qdhworldguardtitle.toggle")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("Usage: /qdhworldguardtitle toggle <title|subtitle|actionbar|messages|overall> [on|off]");
                return true;
            }

            String feature = args[1].toLowerCase(Locale.ROOT);
            boolean value = true;
            if (args.length >= 3) {
                value = args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("true");
            } else {
                // toggle
                switch (feature) {
                    case "title":
                        value = !isTitleEnabled();
                        break;
                    case "subtitle":
                        value = !isSubtitleEnabled();
                        break;
                    case "actionbar":
                        value = !isActionbarEnabled();
                        break;
                    case "messages":
                        value = !isMessagesEnabled();
                        break;
                    case "overall":
                        value = !isOverallEnabled();
                        break;
                    default:
                        sender.sendMessage("Unknown feature: " + feature);
                        return true;
                }
            }

            switch (feature) {
                case "title":
                    if (!sender.hasPermission("qdhworldguardtitle.toggle.title")) {
                        sender.sendMessage("Missing permission: qdhworldguardtitle.toggle.title");
                        return true;
                    }
                    setTitleEnabled(value);
                    break;
                case "subtitle":
                    if (!sender.hasPermission("qdhworldguardtitle.toggle.subtitle")) {
                        sender.sendMessage("Missing permission: qdhworldguardtitle.toggle.subtitle");
                        return true;
                    }
                    setSubtitleEnabled(value);
                    break;
                case "actionbar":
                    if (!sender.hasPermission("qdhworldguardtitle.toggle.actionbar")) {
                        sender.sendMessage("Missing permission: qdhworldguardtitle.toggle.actionbar");
                        return true;
                    }
                    setActionbarEnabled(value);
                    break;
                case "messages":
                    if (!sender.hasPermission("qdhworldguardtitle.toggle.messages")) {
                        sender.sendMessage("Missing permission: qdhworldguardtitle.toggle.messages");
                        return true;
                    }
                    setMessagesEnabled(value);
                    break;
                case "overall":
                    if (!sender.hasPermission("qdhworldguardtitle.toggle.overall")) {
                        sender.sendMessage("Missing permission: qdhworldguardtitle.toggle.overall");
                        return true;
                    }
                    setOverallEnabled(value);
                    break;
                default:
                    sender.sendMessage("Unknown feature: " + feature);
                    return true;
            }

            sender.sendMessage("Feature " + feature + " is now " + (value ? "enabled" : "disabled"));
            return true;
        }

        sender.sendMessage("Unknown subcommand. Usage: /qdhworldguardtitle <reload|status|toggle>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("qdhworldguardtitle.reload")) completions.add("reload");
            if (sender.hasPermission("qdhworldguardtitle.status")) completions.add("status");
            if (sender.hasPermission("qdhworldguardtitle.toggle")) completions.add("toggle");
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            if (!sender.hasPermission("qdhworldguardtitle.toggle")) return Collections.emptyList();
            completions.addAll(Arrays.asList("title", "subtitle", "actionbar", "messages", "overall"));
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("toggle")) {
            completions.addAll(Arrays.asList("on", "off"));
            return completions;
        }

        return completions;
    }

    public static QDHWorldguardTitle getInstance() {
        return instance;
    }

    public RegionConfigManager getRegionManager() {
        return regionConfigManager;
    }
}

package dev.qdhshamiro.qdhworldguardtitle;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;

public class RegionListener implements Listener {

    private final QDHWorldguardTitle plugin;
    private final RegionConfigManager configManager;

    private final Map<UUID, String> lastRegion = new HashMap<>();

    public RegionListener(QDHWorldguardTitle plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getRegionManager();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if (e.getTo() == null) return;
        if (e.getTo().distanceSquared(e.getFrom()) == 0) return;

        Player p = e.getPlayer();

        ApplicableRegionSet set = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(p.getWorld()))
                .getApplicableRegions(BukkitAdapter.asBlockVector(e.getTo()));

        String newRegion = null;

        for (ProtectedRegion r : set) {
            newRegion = r.getId();
            break;
        }

        String oldRegion = lastRegion.get(p.getUniqueId());

        if (Objects.equals(newRegion, oldRegion)) return;

        lastRegion.put(p.getUniqueId(), newRegion);

        final String finalOldRegion = oldRegion;
        final String finalNewRegion = newRegion;

        // ASYNC TASK - prepare heavy work off main thread then schedule UI updates on main
        if (plugin.getConfig().getBoolean("settings.async-processing", true)) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (finalOldRegion != null) {
                    // On leave: ensure immediate clear and then show leave components
                    Bukkit.getScheduler().runTask(plugin, () -> cancelAllFor(p));
                    sendLeave(p, finalOldRegion);
                }

                if (finalNewRegion != null) {
                    sendEnter(p, finalNewRegion);
                }
            });
        } else {
            // synchronous path
            if (finalOldRegion != null) {
                cancelAllFor(p);
                sendLeave(p, finalOldRegion);
            }
            if (finalNewRegion != null) {
                sendEnter(p, finalNewRegion);
            }
        }
    }

    private void sendEnter(Player p, String region) {
        // global overall toggle
        if (!plugin.isOverallEnabled()) return;

        // region enabled
        if (!configManager.getBoolean(region, "enabled", "defaults.enabled")) return;

        final boolean doTitle = configManager.getBoolean(region, "enable-title", "defaults.enable-title") && plugin.isTitleEnabled();
        final boolean doSubtitle = configManager.getBoolean(region, "enable-subtitle", "defaults.enable-subtitle") && plugin.isSubtitleEnabled();
        final boolean doActionbar = configManager.getBoolean(region, "enable-actionbar", "defaults.enable-actionbar") && plugin.isActionbarEnabled();
        final boolean doMessages = configManager.getBoolean(region, "enable-messages", "defaults.enable-messages") && plugin.isMessagesEnabled();
        final boolean doSound = configManager.getBoolean(region, "enable-sound", "defaults.enable-sound") && plugin.getConfig().getBoolean("defaults.enable-sound", true) && plugin.isActionbarEnabled();

        final boolean enterTitle = configManager.getBoolean(region, "enable-title-enter", "defaults.enable-title-enter");
        final boolean enterSubtitle = configManager.getBoolean(region, "enable-subtitle-enter", "defaults.enable-subtitle-enter");
        final boolean enterActionbar = configManager.getBoolean(region, "enable-actionbar-enter", "defaults.enable-actionbar-enter");
        final boolean enterMessages = configManager.getBoolean(region, "enable-messages-enter", "defaults.enable-messages-enter");
        final boolean enterSound = configManager.getBoolean(region, "enable-sound-enter", "defaults.enable-sound-enter");

        // combine
        final boolean finalDoTitle = doTitle && enterTitle;
        final boolean finalDoSubtitle = doSubtitle && enterSubtitle;
        final boolean finalDoActionbar = doActionbar && enterActionbar;
        final boolean finalDoMessages = doMessages && enterMessages;
        final boolean finalDoSound = doSound && enterSound;

        final String title = ColorUtil.color(configManager.get(region, "title-enter", "defaults.title-enter").replace("%region%", region));
        final String subtitle = ColorUtil.color(configManager.get(region, "subtitle-enter", "defaults.subtitle-enter").replace("%region%", region));
        final String actionbar = ColorUtil.color(configManager.get(region, "actionbar-enter", "defaults.actionbar-enter").replace("%region%", region));
        final List<String> msgList = configManager.getList(region, "messages-enter", "defaults.messages-enter");

        final String soundName = configManager.get(region, "sound-enter", "defaults.sound-enter");
        final double soundVol = configManager.getDouble(region, "sound-enter-volume", "defaults.sound-enter-volume", 1.0);
        final double soundPitch = configManager.getDouble(region, "sound-enter-pitch", "defaults.sound-enter-pitch", 1.0);

        // fixed timing now: 40 ticks stay
        final int fadeIn = 10;
        final int finalStay = 40;
        final int fadeOut = 10;

        // run UI updates on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (finalDoTitle) p.sendTitle(title, finalDoSubtitle ? subtitle : "", fadeIn, finalStay, fadeOut);
            if (finalDoActionbar) ActionBarUtil.send(p, actionbar);
            if (finalDoMessages) msgList.forEach(m -> p.sendMessage(ColorUtil.color(m.replace("%region%", region))));

            if (finalDoSound && soundName != null && !soundName.isEmpty()) {
                try {
                    Sound s = Sound.valueOf(soundName);
                    p.playSound(p.getLocation(), s, (float) soundVol, (float) soundPitch);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Unknown sound name for region '" + region + "' enter: " + soundName);
                } catch (Throwable t) {
                    plugin.getLogger().warning("Failed to play enter sound for region '" + region + "': " + t.getMessage());
                }
            }
        });
    }

    private void sendLeave(Player p, String region) {
        if (!plugin.isOverallEnabled()) return;
        if (!configManager.getBoolean(region, "enabled", "defaults.enabled")) return;

        final boolean doTitle = configManager.getBoolean(region, "enable-title", "defaults.enable-title") && plugin.isTitleEnabled();
        final boolean doSubtitle = configManager.getBoolean(region, "enable-subtitle", "defaults.enable-subtitle") && plugin.isSubtitleEnabled();
        final boolean doActionbar = configManager.getBoolean(region, "enable-actionbar", "defaults.enable-actionbar") && plugin.isActionbarEnabled();
        final boolean doMessages = configManager.getBoolean(region, "enable-messages", "defaults.enable-messages") && plugin.isMessagesEnabled();
        final boolean doSound = configManager.getBoolean(region, "enable-sound", "defaults.enable-sound") && plugin.getConfig().getBoolean("defaults.enable-sound", true) && plugin.isActionbarEnabled();

        final boolean leaveTitle = configManager.getBoolean(region, "enable-title-leave", "defaults.enable-title-leave");
        final boolean leaveSubtitle = configManager.getBoolean(region, "enable-subtitle-leave", "defaults.enable-subtitle-leave");
        final boolean leaveActionbar = configManager.getBoolean(region, "enable-actionbar-leave", "defaults.enable-actionbar-leave");
        final boolean leaveMessages = configManager.getBoolean(region, "enable-messages-leave", "defaults.enable-messages-leave");
        final boolean leaveSound = configManager.getBoolean(region, "enable-sound-leave", "defaults.enable-sound-leave");

        final boolean finalDoTitle = doTitle && leaveTitle;
        final boolean finalDoSubtitle = doSubtitle && leaveSubtitle;
        final boolean finalDoActionbar = doActionbar && leaveActionbar;
        final boolean finalDoMessages = doMessages && leaveMessages;
        final boolean finalDoSound = doSound && leaveSound;

        final String title = ColorUtil.color(configManager.get(region, "title-leave", "defaults.title-leave").replace("%region%", region));
        final String subtitle = ColorUtil.color(configManager.get(region, "subtitle-leave", "defaults.subtitle-leave").replace("%region%", region));
        final String actionbar = ColorUtil.color(configManager.get(region, "actionbar-leave", "defaults.actionbar-leave").replace("%region%", region));
        final List<String> msgList = configManager.getList(region, "messages-leave", "defaults.messages-leave");

        final String soundName = configManager.get(region, "sound-leave", "defaults.sound-leave");
        final double soundVol = configManager.getDouble(region, "sound-leave-volume", "defaults.sound-leave-volume", 1.0);
        final double soundPitch = configManager.getDouble(region, "sound-leave-pitch", "defaults.sound-leave-pitch", 1.0);

        final int fadeIn = 10;
        final int finalStay = 40;
        final int fadeOut = 10;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (finalDoTitle) p.sendTitle(title, finalDoSubtitle ? subtitle : "", fadeIn, finalStay, fadeOut);
            if (finalDoActionbar) ActionBarUtil.send(p, actionbar);
            if (finalDoMessages) msgList.forEach(m -> p.sendMessage(ColorUtil.color(m.replace("%region%", region))));

            if (finalDoSound && soundName != null && !soundName.isEmpty()) {
                try {
                    Sound s = Sound.valueOf(soundName);
                    p.playSound(p.getLocation(), s, (float) soundVol, (float) soundPitch);
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Unknown sound name for region '" + region + "' leave: " + soundName);
                } catch (Throwable t) {
                    plugin.getLogger().warning("Failed to play leave sound for region '" + region + "': " + t.getMessage());
                }
            }
        });
    }

    private void cancelAllFor(Player p) {
        try {
            ActionBarUtil.send(p, "");
        } catch (Throwable ignored) {}
        try {
            // try to clear title by sending empty title with zero timings
            p.sendTitle("", "", 0, 0, 0);
        } catch (Throwable ignored) {}
    }
}

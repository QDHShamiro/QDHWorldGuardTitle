package dev.qdhshamiro.qdhworldguardtitle;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RegionConfigManager {

    private final QDHWorldguardTitle plugin;
    private File regionsFile;
    private FileConfiguration regionsConfig;

    public RegionConfigManager(QDHWorldguardTitle plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        regionsFile = new File(plugin.getDataFolder(), "regions.yml");

        if (!regionsFile.exists()) {
            plugin.saveResource("regions.yml", false);
        }

        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    }

    public String get(String regionId, String key, String defPath) {
        String path = regionId + "." + key;

        if (regionsConfig.contains(path)) {
            return regionsConfig.getString(path);
        }

        return plugin.getConfig().getString(defPath);
    }

    public List<String> getList(String regionId, String key, String defPath) {
        String path = regionId + "." + key;

        List<String> list = regionsConfig.getStringList(path);

        if (list != null && !list.isEmpty()) {
            return list;
        }

        return plugin.getConfig().getStringList(defPath);
    }

    public boolean getBoolean(String regionId, String key, String defPath) {
        String path = regionId + "." + key;

        if (regionsConfig.contains(path)) {
            return regionsConfig.getBoolean(path);
        }

        return plugin.getConfig().getBoolean(defPath);
    }

    public double getDouble(String regionId, String key, String defPath, double fallback) {
        String path = regionId + "." + key;
        if (regionsConfig.contains(path)) {
            return regionsConfig.getDouble(path, fallback);
        }
        return plugin.getConfig().getDouble(defPath, fallback);
    }

    public int getRegionCount() {
        if (regionsConfig == null) return 0;
        return regionsConfig.getKeys(false).size();
    }

    /**
     * Returns the display time in milliseconds for the region.
     * First checks region.display-time, then falls back to plugin.display-time.
     */
    public long getDisplayTimeMillis(String regionId) {
        String path = regionId + ".display-time";
        String value = null;
        if (regionsConfig.contains(path)) value = regionsConfig.getString(path);
        if (value == null || value.isEmpty() || "default".equalsIgnoreCase(value)) {
            value = plugin.getConfig().getString("display-time");
        }
        if (value == null || value.isEmpty()) return 0L;
        return parseTimeToMillis(value);
    }

    private long parseTimeToMillis(String v) {
        v = v.trim().toLowerCase();
        try {
            if (v.endsWith("ms")) {
                long n = Long.parseLong(v.substring(0, v.length() - 2));
                return Math.max(0L, n);
            }
            if (v.endsWith("s")) {
                long n = Long.parseLong(v.substring(0, v.length() - 1));
                return Math.max(0L, TimeUnit.SECONDS.toMillis(n));
            }
            long n = Long.parseLong(v);
            return Math.max(0L, TimeUnit.SECONDS.toMillis(n));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}

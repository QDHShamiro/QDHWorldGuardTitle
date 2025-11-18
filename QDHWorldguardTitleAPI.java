package dev.qdhshamiro.qdhworldguardtitle;

/**
 * QDHWorldguardTitle API
 * <p>
 * This small wrapper provides a stable entry point for other plugins to interact with
 * the QDHWorldguardTitle plugin at runtime (toggle features programmatically, read flags, ...).
 *
 * How to add this plugin as a compile-time dependency
 * ---------------------------------------------------
 * If you want to compile against the API in your own plugin, add the following dependency
 * to your build system. Note: this plugin's artifact must be available in a Maven repository
 * or installed to your local Maven repository (mvn install) for these examples to work.
 *
 * Maven:
 *
 * <dependency>
 *   <groupId>dev.qdhshamiro</groupId>
 *   <artifactId>QDHWorldguardTitle</artifactId>
 *   <version>1.0.0</version>
 *   <scope>provided</scope>
 * </dependency>
 *
 * Gradle (Kotlin DSL):
 *
 * dependencies {
 *   compileOnly("dev.qdhshamiro:QDHWorldguardTitle:1.0.0")
 * }
 *
 * Runtime: plugin dependency
 * ---------------------------
 * To ensure the plugin is loaded before you access the API at runtime, add a `depend:`
 * (or `softdepend:`) entry in your plugin.yml referring to the plugin name `QDHWorldGuardTitle`.
 * Example plugin.yml:
 *
 * depend: [QDHWorldGuardTitle]
 *
 * Usage example (from another plugin):
 * -------------------------------------
 * // In your onEnable():
 * @Override
 * public void onEnable() {
 *     // safe usage: API may be null if the plugin is not present or not enabled yet
 *     QDHWorldguardTitleAPI api = QDHWorldguardTitleAPI.getInstance();
 *     if (api != null) {
 *         // toggle features
 *         api.setTitleEnabled(false);
 *     }
 * }
 *
 * Notes:
 * - Always check for null from getInstance() because the target plugin might not be installed or enabled.
 * - The API methods are simple forwarders; they operate on the plugin's runtime state.
 */
public class QDHWorldguardTitleAPI {

    private static QDHWorldguardTitleAPI instance;
    private final QDHWorldguardTitle plugin;

    private QDHWorldguardTitleAPI(QDHWorldguardTitle plugin) {
        this.plugin = plugin;
    }

    public static synchronized void init(QDHWorldguardTitle plugin) {
        if (instance == null) instance = new QDHWorldguardTitleAPI(plugin);
    }

    /**
     * Returns the API instance, or null if the plugin hasn't initialized it (plugin missing/disabled).
     */
    public static QDHWorldguardTitleAPI getInstance() {
        return instance;
    }

    // Forwarding methods
    public void setOverallEnabled(boolean v) { plugin.setOverallEnabled(v); }
    public boolean isOverallEnabled() { return plugin.isOverallEnabled(); }

    public void setTitleEnabled(boolean v) { plugin.setTitleEnabled(v); }
    public boolean isTitleEnabled() { return plugin.isTitleEnabled(); }

    public void setSubtitleEnabled(boolean v) { plugin.setSubtitleEnabled(v); }
    public boolean isSubtitleEnabled() { return plugin.isSubtitleEnabled(); }

    public void setActionbarEnabled(boolean v) { plugin.setActionbarEnabled(v); }
    public boolean isActionbarEnabled() { return plugin.isActionbarEnabled(); }

    public void setMessagesEnabled(boolean v) { plugin.setMessagesEnabled(v); }
    public boolean isMessagesEnabled() { return plugin.isMessagesEnabled(); }

}

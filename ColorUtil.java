package dev.qdhshamiro.qdhworldguardtitle;

import org.bukkit.ChatColor;

import java.util.regex.Matcher; import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    public static String color(String msg) {
        if (msg == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(msg);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);

            String replacement =
                    "§x§" + hex.charAt(0) +
                            "§" + hex.charAt(1) +
                            "§" + hex.charAt(2) +
                            "§" + hex.charAt(3) +
                            "§" + hex.charAt(4) +
                            "§" + hex.charAt(5);

            matcher.appendReplacement(buffer, replacement);
        }

        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}

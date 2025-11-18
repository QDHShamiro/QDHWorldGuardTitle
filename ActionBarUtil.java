package dev.qdhshamiro.qdhworldguardtitle;

import net.md_5.bungee.api.ChatMessageType; import net.md_5.bungee.api.chat.TextComponent; import org.bukkit.entity.Player;

public class ActionBarUtil {

    public static void send(Player player, String msg) {
        if (msg == null || player == null) return;

        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(msg)
        );
    }
}

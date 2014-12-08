package org.freehat.particles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Util {

	private Util() {

	}

	private final static String PREFIX = ChatColor.AQUA + "["
			+ ChatColor.YELLOW + "Particle Game" + ChatColor.AQUA + "] "
			+ ChatColor.WHITE;

	public static void send(Player player, String message) {
		if (player != null) {
			player.sendMessage(PREFIX + message);
		}
	}

	public static void broadcast(String message) {
		Bukkit.broadcastMessage(PREFIX + message);
	}

	public static void usage(Player player) {
		send(player,
				"If you can read this, go bitch Nathan out and tell him to write up the usage docs.");
	}

}

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
				"The following commands are available:\n"
						+ "/part usage - Print usage\n"
						+ "/part guess <particles> - e.g. /part guess は が. Not usually needed.\n"
						+ "/part sentence <sentence> - e.g. /part sentence 私A百円Bある. Not usually needed.\n"
						+ "/part score - List high scores.\n"
						+ "/part challenge <player> - Challenge another player.\n"
						+ "/part accept - Accept said challenge. \n");
	}

}

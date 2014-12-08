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
						+ "/part challenge <player> - Challenge another player.\n"
						+ "/part accept - Accept said challenge. \n"
						+ "/part guess <particles> - For example, /part guess は が, guesses that は and が are the two particles in the current sentence.\n"
						+ "/part sentence <sentence> - Set the sentence when it is your turn.  For example, if your particles are A: に and B: が you could say /part sentence 私A百円Bある.\n"
						+ "/part usage - Print usage\n");
	}

}

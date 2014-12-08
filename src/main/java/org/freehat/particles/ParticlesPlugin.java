package org.freehat.particles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.freehat.particles.GameSessions.GameSession;

public class ParticlesPlugin extends JavaPlugin {

	public static final UUID AI = UUID.randomUUID();

	public ParticlesPlugin() {

	}

	void handleCommand(Player player, String[] args) {
		UUID pid = player.getUniqueId();
		if (args[0].equals("ai")) {
			pid = AI;
			args = Arrays.copyOfRange(args, 1, args.length);
		}
		GameSession session = sessions.getSession(pid);
		switch (args[0]) {
		case "accept":
			if (session == null) {
				session = sessions.accept(pid);
			} else {
				Util.send(player, "I think you're already playing.");
			}
			return;
		case "challenge":
			if (args.length > 1) {
				if (session == null) {
					session = sessions.initiate(pid);
					for (int i = 1; i < args.length; i++) {
						session.invite(args[i]);
					}
				} else {
					Util.send(player, "I think you're already playing.");
				}
			} else {
				Util.send(player, "Challenge whom?");
			}
			return;
		case "pass":
			if (session != null) {
				session.pass(player);
			}
			return;
		case "sentence":
			if (session != null) {
				if (args.length > 2) {
					StringBuilder b = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						b.append(args[i]);
						b.append(" ");
					}
					String sentence = b.toString().trim();
					session.setSentence(player, sentence);
				} else {
					Util.send(player, "Must specify a sentence");
				}
			} else {
				Util.send(player, "You're not in a game.");
			}
			return;
		case "guess":
			if (session != null) {
				if (args.length > 1) {
					final List<String> particles = new ArrayList<>(
							args.length - 1);
					for (int i = 1; i < args.length; i++) {
						particles.add(args[i]);
					}
					session.guess(player, particles);
				} else {
					Util.send(player, "You should probably guess something.");
				}
			} else {
				Util.send(player, "You're not in a game.");
			}
			return;
		default:
			Util.usage(player);
		}
	}

	private volatile GameSessions sessions;

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		final Player player = (Player) sender;
		if (command.getName().equals("part")) {
			if (args.length > 0) {
				handleCommand(player, args);
			} else {
				Util.usage(player);
			}
		}
		return false;
	}

	@Override
	public void onDisable() {
		sessions = null;
		PluginDescriptionFile pdfFile = this.getDescription();
		getLogger().info(pdfFile.getName() + "Disabled !");
	}

	@Override
	public void onEnable() {
		sessions = new GameSessions(this);
		PluginDescriptionFile pdfFile = this.getDescription();
		getLogger().info(pdfFile.getName() + " Enabled !");
		getLogger().info("Current version: " + pdfFile.getVersion());
	}

}

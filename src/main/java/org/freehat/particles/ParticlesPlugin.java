package org.freehat.particles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.freehat.particles.GameSessions.GameSession;
import org.freehat.particles.game.ParticleLevel;

public class ParticlesPlugin extends JavaPlugin implements Listener {

	public static final String KEY = "part";
	public static final UUID AI = UUID.randomUUID();

	private Player aiPlayer;

	public Player getPlayer(String id) {
		UUID pid = UUID.fromString(id);
		return getPlayer(pid);
	}

	public Player getPlayer(UUID pid) {
		if (AI.equals(pid)) {
			return aiPlayer;
		} else {
			return Bukkit.getPlayer(pid);
		}
	}

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
		case "ai-reg":
			aiPlayer = player;
			return;
		case "accept":
			if (session == null) {
				session = sessions.accept(pid);
			} else {
				Util.send(player, "I think you're already playing.");
			}
			return;
		case "hard":
			if (args.length > 1) {
				if (session == null) {
					ParticleLevel level = ParticleLevel.BEGINNER;
					session = sessions.initiate(level, pid);
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
		case "challenge":
			if (args.length > 1) {
				if (session == null) {
					ParticleLevel level = ParticleLevel.BEGINNER;
					session = sessions.initiate(level, pid);
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
				session.pass(pid);
			}
			return;
		case "sentence":
			if (session != null) {
				if (args.length >= 2) {
					StringBuilder b = new StringBuilder();
					for (int i = 1; i < args.length; i++) {
						b.append(args[i]);
						b.append(" ");
					}
					String sentence = b.toString().trim();
					session.setSentence(pid, sentence);
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
					session.guess(pid, particles);
				} else {
					Util.send(player, "You should probably guess something.");
				}
			} else {
				Util.send(player, "You're not in a game.");
			}
			return;
		case "scores":
			sessions.listHighScores(pid);
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
		getServer().getPluginManager().registerEvents(this, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		getLogger().info(pdfFile.getName() + " Enabled !");
		getLogger().info("Current version: " + pdfFile.getVersion());
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player p = event.getPlayer();
		if (p.hasMetadata(KEY)) {
			UUID pid = p.getUniqueId();
			String message = event.getMessage();
			GameSession session = sessions.getSession(p.getUniqueId());
			if (session != null) {
				UUID currentSetter = session.getSentenceSetter();
				if (!AI.equals(currentSetter) && currentSetter.equals(pid)) {
					event.setCancelled(true);
					if ("pass".equals(message)) {
						session.pass(pid);
					} else {
						session.setSentence(pid, message);
					}
				} else {
					session.guess(pid,
							Arrays.asList(event.getMessage().split("\\s+")));
				}
			} else {
				p.removeMetadata(KEY, this);
			}
		}
	}
}

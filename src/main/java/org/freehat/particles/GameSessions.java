package org.freehat.particles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.freehat.particles.game.GameRound;
import org.freehat.particles.game.GameSentence;
import org.freehat.particles.game.GameSentence.SentencePart;
import org.freehat.particles.game.GuessResult;
import org.freehat.particles.game.Particle;
import org.freehat.particles.game.ParticleGame;
import org.freehat.particles.game.ParticleLevel;
import org.freehat.particles.game.SentenceResult;

public class GameSessions {

	private final ArrayList<HighScore> highScores;

	private final List<GameSession> sessions;
	private final Map<UUID, GameSession> invites;
	private final ScoreboardManager manager;
	private final ParticlesPlugin plugin;

	public GameSessions(ParticlesPlugin plugin) {
		sessions = new ArrayList<>();
		invites = new HashMap<UUID, GameSession>();
		this.plugin = plugin;
		manager = Bukkit.getScoreboardManager();
		highScores = new ArrayList<>();
	}

	public GameSession quit(UUID pid) {
		GameSession session = getSession(pid);
		if (session != null) {
			session.quit(pid);
		}
		return session;
	}

	public GameSession initiate(ParticleLevel level, UUID pid) {
		if (getSession(pid) != null) {
			return null;
		}
		GameSession session = new GameSession(level);
		sessions.add(session);
		session.join(pid);
		return session;
	}

	public GameSession accept(UUID name) {
		GameSession session = invites.remove(name);
		if (session != null) {
			session.join(name);
		}
		return session;
	}

	public GameSession getSession(UUID pid) {
		for (GameSession s : sessions) {
			if (s.hasPlayer(pid)) {
				return s;
			}
		}
		return null;
	}

	public void listHighScores(UUID pid) {
		Player p = plugin.getPlayer(pid);
		for (HighScore hs : highScores) {
			Util.send(p, hs.name + ": " + hs.score);
		}
	}

	static class HighScore implements Comparable<HighScore> {
		final String name;
		final int score;

		HighScore(String name, int score) {
			this.name = name;
			this.score = score;
		}

		@Override
		public int compareTo(HighScore o) {
			return Integer.compare(o.score, score);
		}

	}

	public class GameSession {
		private static final String SCORE = "Points";
		private final Set<UUID> players = new HashSet<>();
		private final Scoreboard board = manager.getNewScoreboard();
		private final ParticleLevel level;
		private final Objective objective = board.registerNewObjective(
				hashCode() + "_score", "dummy");
		private ParticleGame game;
		private boolean gameOn;
		private int gameLength = 20 * 60 * 5;

		GameSession(ParticleLevel level) {
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			setTimeString(gameLength / 20);
			Score s = objective.getScore(SCORE);
			s.setScore(0);
			this.level = level;
		}

		void setTimeString(int timeLeft) {
			ChatColor timeColor = timeLeft > 60 ? ChatColor.WHITE
					: timeLeft > 30 ? ChatColor.YELLOW : ChatColor.RED;
			objective.setDisplayName(ChatColor.GOLD + "Time: " + timeColor
					+ String.format("%d:%02d", timeLeft / 60, timeLeft % 60));
		}

		public void pass(UUID pid) {
			game.pass(pid.toString());
			Util.send(plugin.getPlayer(pid), "New particles: "
					+ game.getRoundInfo().getParticles());
		}

		public void setSentence(UUID pid, String sentence) {
			SentenceResult result = game.setSentence(pid.toString(), sentence);
			Player player = plugin.getPlayer(pid);
			switch (result) {
			case INVALID_SENTENCE:
				Util.send(
						player,
						"Not a valid sentence, did you remember to specify the particles using A,B,C, etc.?");
				break;
			case WRONGTIME:
				Util.send(player,
						"The sentence has already been set, if you wish to change it then use 'pass'");
				break;
			case WRONGUSER:
				Util.send(player, "It's not your turn.");
				break;
			case SENTENCE_SET:
				Util.send(player, "Sentence set");
				GameSentence gs = game.getRoundInfo().getSentence();
				StringBuilder b = new StringBuilder("Sentence to guess: \n");
				List<Particle> particles = gs.getParticles();
				ChatColor[] wheel = new ChatColor[] { ChatColor.RED,
						ChatColor.GOLD, ChatColor.BLUE, ChatColor.GREEN,
						ChatColor.YELLOW, ChatColor.DARK_AQUA,
						ChatColor.DARK_PURPLE };
				for (SentencePart sp : gs.getSentence()) {
					if (sp.isParticle()) {
						int idx = particles.indexOf(sp.particle());
						b.append(wheel[idx % wheel.length] + "XX");
					} else {
						b.append(ChatColor.WHITE + sp.text());
					}
				}
				b.append("\n" + ChatColor.WHITE + "Name the particles: ");
				for (int i = 0; i < particles.size(); i++) {
					b.append(wheel[i % wheel.length] + "XX ");
				}
				sendMessage(b.toString());
				break;
			}
		}

		public void guess(UUID pid, List<String> particles) {
			GuessResult guess = game.guess(pid.toString(), particles);
			if (guess == null) {
				Util.send(plugin.getPlayer(pid),
						"You can't guess your own sentence.");
				return;
			}
			if (guess.isSuccess()) {
				Score s = objective.getScore(SCORE);
				s.setScore(game.getScore());
				sendMessage(String.format("%s was correct.", particles));
				notifySentenceSetter();
			} else {
				Util.send(
						plugin.getPlayer(pid),
						String.format("%d correct, %d incorrect.",
								guess.getCorrect(), guess.getIncorrect()));
			}
		}

		private void notifySentenceSetter() {
			GameRound info = game.getRoundInfo();
			Player nextGuesser = plugin.getPlayer(info.getPlayer());
			Util.send(
					nextGuesser,
					"It's your turn to name a sentence, set it by typing a sentence into chat.  For example, if your particles are A: に and B: が you could type 私A百円Bある");
			StringBuilder b = new StringBuilder("Your particles: \n");
			char letter = 'A';
			for (Particle p : info.getParticles()) {
				b.append('\t');
				b.append(letter);
				b.append(": ");
				b.append(p.toString());
				b.append("\n");
				letter++;
			}
			Util.send(nextGuesser, b.toString());
		}

		@SuppressWarnings("deprecation")
		public boolean invite(String player) {
			Player invitee = Bukkit.getPlayer(player);
			if (invitee != null) {
				Util.send(
						invitee,
						"You have been invited to play particle game.  Type '/part accept' to participate.");
				invites.put(invitee.getUniqueId(), this);
				return true;
			}
			return false;
		}

		public void quit(UUID pid) {
			if (players.size() <= 2) {
				endGame();
			} else {
				players.remove(pid);
				Bukkit.getPlayer(pid)
						.setScoreboard(manager.getMainScoreboard());
			}
		}

		private void endGame() {
			gameOn = false;

			sendMessage(String.format("Game over. Final Score: %d points.",
					game.getScore()));
			sessions.remove(this);
			StringBuilder b = new StringBuilder();
			for (UUID pid : players) {
				Player p = plugin.getPlayer(pid);
				if (p != null) {
					if (b.length() > 0) {
						b.append(", ");
					}
					b.append(p.getName());
					p.setScoreboard(manager.getMainScoreboard());
					p.removeMetadata(ParticlesPlugin.KEY, plugin);
				}
			}
			highScores.add(new HighScore(b.toString(), game.getScore()));
			Collections.sort(highScores);
			if (highScores.size() == 6) {
				highScores.remove(5);
			}
			for (UUID pid : players) {
				listHighScores(pid);
			}
		}

		public UUID getSentenceSetter() {
			return UUID.fromString(game.getRoundInfo().getPlayer());
		}

		public boolean hasPlayer(UUID pid) {
			return players.contains(pid);
		}

		public void join(UUID name) {
			players.add(name);
			if (gameOn) {
				game.addPlayer(name.toString());
				Player p = plugin.getPlayer(name);
				p.setScoreboard(board);
				p.setMetadata(ParticlesPlugin.KEY, new FixedMetadataValue(
						plugin, "KEY"));

			} else {
				if (players.size() > 1) {
					sendMessage("Game starting in 5 seconds.");
					new StartGame().runTaskLater(plugin, 100);
				}
			}
		}

		private void sendMessage(String message) {
			for (UUID pid : players) {
				Util.send(plugin.getPlayer(pid), message);
			}
		}

		class StartGame extends BukkitRunnable {

			@Override
			public void run() {
				ParticleGame.Builder builder = new ParticleGame.Builder();
				builder.particleCount(1);
				for (UUID player : players) {
					Player p = plugin.getPlayer(player);
					builder.player(player.toString());
					builder.level(level);
					p.setScoreboard(board);
					p.setMetadata(ParticlesPlugin.KEY, new FixedMetadataValue(
							plugin, "foo"));
				}
				game = builder.build();
				gameOn = true;
				new GameTimer(gameLength / 20).schedule();
				notifySentenceSetter();
			}

		}

		class GameTimer extends BukkitRunnable {
			private final int PERIOD = 100;

			private int seconds;

			GameTimer(int seconds) {
				this.seconds = seconds;
			}

			@Override
			public void run() {
				seconds -= PERIOD / 20;
				if (seconds == 60 || seconds == 30) {
					sendMessage(seconds + " seconds left.");
				}
				setTimeString(seconds);
				if (seconds <= 0) {
					if (gameOn) {
						endGame();
					}
					cancel();
				}
			}

			void schedule() {
				setTimeString(seconds);
				runTaskTimer(plugin, 0, PERIOD);
			}

		}

	}

}

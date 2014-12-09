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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.freehat.particles.game.GameRound;
import org.freehat.particles.game.GuessResult;
import org.freehat.particles.game.Particle;
import org.freehat.particles.game.ParticleGame;
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

	public GameSession initiate(UUID pid) {
		if (getSession(pid) != null) {
			return null;
		}
		GameSession session = new GameSession();
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

		private final Objective objective = board.registerNewObjective(
				hashCode() + "", "dummy");
		private ParticleGame game;
		private boolean gameOn;
		private int score;
		private int gameLength = 20 * 60 * 5;

		GameSession() {
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
			objective.setDisplayName(ChatColor.GOLD + "5 Minute Round");
			Score s = objective.getScore(SCORE);
			s.setScore(score);
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
				sendMessage("Sentence to guess: "
						+ game.getRoundInfo().getText());
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
				score++;
				Score s = objective.getScore(SCORE);
				s.setScore(score);
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
					"It's your turn to name a sentence, set it by calling /part sentence <sentence>.  For example, if your particles are A: に and B: が you could type /part sentence 私A百円Bある");
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
					score));
			sessions.remove(this);
			StringBuilder b = new StringBuilder();
			for (UUID pid : players) {
				Player p = plugin.getPlayer(pid);
				if (b.length() > 0) {
					b.append(", ");
				}
				b.append(p.getName());
				p.setScoreboard(manager.getMainScoreboard());
			}
			highScores.add(new HighScore(b.toString(), score));
			Collections.sort(highScores);
			if (highScores.size() == 6) {
				highScores.remove(5);
			}
			for (UUID pid : players) {
				listHighScores(pid);
			}
		}

		public boolean hasPlayer(UUID pid) {
			return players.contains(pid);
		}

		public void join(UUID name) {
			players.add(name);
			if (gameOn) {
				game.addPlayer(name.toString());
				plugin.getPlayer(name).setScoreboard(board);
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
				new EndGame().runTaskLater(plugin, gameLength);
				new TimeWarning(60).runTaskLater(plugin, gameLength - 20 * 60);
				new TimeWarning(30).runTaskLater(plugin, gameLength - 20 * 30);
				ParticleGame.Builder builder = new ParticleGame.Builder();
				builder.particleCount(1);
				for (UUID player : players) {
					builder.player(player.toString());
					plugin.getPlayer(player).setScoreboard(board);
				}
				game = builder.build();
				gameOn = true;
				notifySentenceSetter();
			}

		}

		class TimeWarning extends BukkitRunnable {

			private final int seconds;

			TimeWarning(int seconds) {
				this.seconds = seconds;
			}

			@Override
			public void run() {
				sendMessage(seconds + " seconds left.");
			}

		}

		class EndGame extends BukkitRunnable {

			@Override
			public void run() {
				if (gameOn) {
					endGame();
				}
			}

		}

	}

}

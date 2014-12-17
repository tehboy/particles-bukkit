package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is NOT thread-safe, and must be externally synchronized if called
 * by multiple threads.
 *
 * @author nathan
 *
 */
public class ParticleGame {

	private final List<String> playerIds;
	private final ParticleLevel level;
	private int score;
	private GameRound round;

	public static class Builder {
		final List<String> players = new ArrayList<>();
		int particleCount = 1;
		ParticleLevel level = ParticleLevel.ALL;

		public Builder particleCount(int particleCount) {
			this.particleCount = particleCount;
			return this;
		}

		public Builder player(String player) {
			players.add(player);
			return this;
		}

		public Builder level(ParticleLevel level) {
			this.level = level;
			return this;
		}

		public ParticleGame build() {
			return new ParticleGame(players, level, particleCount);
		}
	}

	private ParticleGame(List<String> players, ParticleLevel level,
			int particleCount) {
		playerIds = new ArrayList<>(players);
		this.level = level;
		score = 0;
		round = new GameRound(playerIds.get(0), Particle.randomParticles(level,
				particleCount));
	}

	void setupNextRound() {
		playerIds.indexOf(round.getPlayer());
		String nextPlayer = playerIds
				.get((playerIds.indexOf(round.getPlayer()) + 1)
						% playerIds.size());
		round = new GameRound(nextPlayer, Particle.randomParticles(level,
				getScore() + 1));
	}

	public GameRound pass(String user) {
		if (!round.getPlayer().equals(user)) {
			return null;
		}
		round = new GameRound(round.getPlayer(), Particle.randomParticles(
				level, round.getParticles().size()));
		return round;
	}

	public SentenceResult setSentence(String user, String sentence) {
		if (!round.getPlayer().equals(user)) {
			return SentenceResult.WRONGUSER;
		}
		if (!(round.getState() == RoundState.INITIAL)) {
			return SentenceResult.WRONGTIME;
		}
		GameSentence gs = GameSentence.create(sentence, round.getParticles());
		round.setSentence(gs);
		if (gs == null) {
			return SentenceResult.INVALID_SENTENCE;
		}
		round.setState(RoundState.RUNNING);
		return SentenceResult.SENTENCE_SET;
	}

	public GuessResult guess(String player, List<String> particles) {
		if (player.equals(round.getPlayer())
				|| round.getState() == RoundState.INITIAL) {
			return null;
		}
		GuessResult r = round.getSentence().guess(particles);
		if (r.isSuccess()) {
			score++;
			setupNextRound();
		}
		return r;
	}

	public int getScore() {
		return score / playerIds.size();
	}

	public GameRound getRoundInfo() {
		return round;
	}

	public void addPlayer(String player) {
		playerIds.add(player);
	}

}

package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is NOT thread-safe, and must be externally synchronized if called
 * by multiple threads.
 *
 * @author nathan
 *
 */
public class ParticleGame {

	private final List<String> playerIds;
	private final Map<String, Integer> scores;
	private GameRound round;

	public static class Builder {
		final List<String> players = new ArrayList<>();
		int particleCount = 1;

		public Builder particleCount(int particleCount) {
			this.particleCount = particleCount;
			return this;
		}

		public Builder player(String player) {
			players.add(player);
			return this;
		}

		public ParticleGame build() {
			return new ParticleGame(players, particleCount);
		}
	}

	private ParticleGame(List<String> players, int particleCount) {
		playerIds = new ArrayList<>(players);
		scores = new HashMap<>();
		for (String player : playerIds) {
			scores.put(player, 0);
		}
		round = new GameRound(playerIds.get(0),
				Particle.randomParticles(particleCount));
	}

	void setupNextRound() {
		playerIds.indexOf(round.getPlayer());
		String nextPlayer = playerIds
				.get((playerIds.indexOf(round.getPlayer()) + 1)
						% playerIds.size());
		int count = round.getParticles().size() + 1;
		round = new GameRound(nextPlayer, Particle.randomParticles(count));
	}

	public GameRound pass(String user) {
		if (!round.getPlayer().equals(user)) {
			return null;
		}
		round = new GameRound(round.getPlayer(), Particle.randomParticles(round
				.getParticles().size()));
		return round;
	}

	public SentenceResult setSentence(String user, String sentence) {
		if (!round.getPlayer().equals(user)) {
			return SentenceResult.WRONGUSER;
		}
		if (!(round.getState() == RoundState.INITIAL)) {
			return SentenceResult.WRONGTIME;
		}
		List<Particle> ps = round.getParticles();
		int numParticles = ps.size();
		final TreeMap<Integer, Particle> partLoc = new TreeMap<>();
		for (int i = 0; i < numParticles; i++) {
			Character c = Character.valueOf((char) (65 + i));
			int indexOf = sentence.indexOf(c);
			if (indexOf == -1) {
				return SentenceResult.INVALID_SENTENCE;
			}
			partLoc.put(indexOf, ps.get(i));
		}
		round.setParticles(new ArrayList<>(partLoc.values()));
		StringBuilder b = new StringBuilder(sentence);
		for (int loc : partLoc.descendingKeySet()) {
			b.replace(loc, loc + 1, "XX");
		}
		round.setText(b.toString());
		round.setState(RoundState.RUNNING);
		return SentenceResult.SENTENCE_SET;
	}

	public GuessResult guess(String player, List<String> particles) {
		if (player.equals(round.getPlayer())
				|| round.getState() == RoundState.INITIAL) {
			return null;
		}
		GuessResult r = new GuessResult();
		List<Particle> roundParticles = round.getParticles();
		final int cnt = roundParticles.size();
		int correct = 0, incorrect = 0;
		for (int i = 0; i < particles.size() && i < cnt; i++) {
			Particle guess = Particle.lookup(particles.get(i));
			if (guess != null && guess.equals(roundParticles.get(i))) {
				correct++;
			} else {
				incorrect++;
			}
		}
		r.setCorrect(correct);
		r.setIncorrect(incorrect);
		final boolean success = incorrect == 0 && particles.size() == cnt;
		r.setSuccess(success);
		if (success) {
			scores.put(player, scores.get(player) + 1);
			setupNextRound();
		}
		return r;
	}

	public Map<String, Integer> getScores() {
		return Collections.unmodifiableMap(scores);
	}

	public GameRound getRoundInfo() {
		return round;
	}

	public void addPlayer(String player) {
		playerIds.add(player);
	}

}

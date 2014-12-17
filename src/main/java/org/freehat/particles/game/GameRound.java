package org.freehat.particles.game;

import java.util.List;

public class GameRound {
	private List<Particle> particles;
	private final String player;

	private GameSentence sentence;
	private RoundState state;

	GameRound(String player, List<Particle> particles) {
		this.player = player;
		this.particles = particles;
		state = RoundState.INITIAL;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	/*
	 * Used to reorder the particles to match the sentence
	 */
	void setParticles(List<Particle> particles) {
		this.particles = particles;
	}

	public GameSentence getSentence() {
		return sentence;
	}

	public void setSentence(GameSentence sentence) {
		this.sentence = sentence;
	}

	public String getPlayer() {
		return player;
	}

	public RoundState getState() {
		return state;
	}

	void setState(RoundState state) {
		this.state = state;
	}

}
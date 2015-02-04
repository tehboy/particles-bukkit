package org.freehat.particles.game;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.freehat.particles.game.GameRound;
import org.freehat.particles.game.GuessResult;
import org.freehat.particles.game.Particle;
import org.freehat.particles.game.ParticleGame;
import org.freehat.particles.game.RoundState;
import org.junit.Test;

public class ParticleGameTest {

	@Test
	public void oneRound() {
		ParticleGame game = new ParticleGame.Builder().player("foo")
				.player("bar").build();
		GameRound roundInfo = game.getRoundInfo();
		List<Particle> particles = roundInfo.getParticles();
		assertEquals("First round should have one particle", particles.size(),
				1);
		assertEquals(RoundState.INITIAL, roundInfo.getState());
		Particle p = particles.get(0);
		game.setSentence("foo", "仕方Aない");
		assertEquals(RoundState.RUNNING, roundInfo.getState());
		GuessResult guess = game.guess("bar", Arrays.asList(p.toString()));
		assertTrue(guess.isSuccess());
		assertEquals(1, game.getScore());
		roundInfo = game.getRoundInfo();
		assertEquals(RoundState.INITIAL, roundInfo.getState());
		assertEquals("bar", roundInfo.getPlayer());
		assertNull(roundInfo.getSentence());
		assertEquals(1, roundInfo.getParticles().size());
	}

	@Test
	public void testSentence() {
		ParticleGame game = new ParticleGame.Builder().player("foo")
				.player("bar").particleCount(3).build();
		GameRound roundInfo = game.getRoundInfo();
		roundInfo.getParticles();
		assertEquals(SentenceResult.INVALID_SENTENCE,
				game.setSentence("foo", "了解C、仕事B、こそあど言葉、分からないですけど。"));
		assertEquals(SentenceResult.INVALID_SENTENCE,
				game.setSentence("foo", "了解C、仕事B、こそあど言葉C、分からないですけど。"));
		assertEquals(SentenceResult.WRONGUSER,
				game.setSentence("bar", "了解C、仕事B、こそあど言葉A、分からないですけど。"));
		assertEquals(SentenceResult.SENTENCE_SET,
				game.setSentence("foo", "了解C、仕事B、こそあど言葉A、分からないですけど。"));
		assertEquals(SentenceResult.WRONGTIME,
				game.setSentence("foo", "了解C、仕事B、こそあど言葉A、分からないですけど。"));
	}

	@Test
	public void testGuess() {
		ParticleGame game = new ParticleGame.Builder().player("foo")
				.player("bar").particleCount(3).build();
		game.setSentence("foo", "了解A、仕事B、こそあど言葉C、分からないですけど。");
		List<Particle> particles = new ArrayList<>(game.getRoundInfo()
				.getParticles());
		List<String> guess = new ArrayList<>();
		for (Particle p : particles) {
			guess.add(p.toString());
		}
		GuessResult result = game.guess("foo", guess);
		assertNull(result);
		Collections.reverse(guess);
		result = game.guess("bar", guess);
		assertEquals(1, result.getCorrect());
		assertEquals(2, result.getIncorrect());
		assertEquals(false, result.isSuccess());
		guess = new ArrayList<>();
		for (Particle p : particles) {
			guess.add(p.getPossibleNames().get(p.getPossibleNames().size() - 1));
		}
		result = game.guess("bar", guess);
		assertEquals(3, result.getCorrect());
		assertEquals(0, result.getIncorrect());
		assertEquals(true, result.isSuccess());
	}
}

package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freehat.particles.game.GameSentence.SentencePart;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameSentenceTest {

	@Test
	public void testBadSentence() {
		assertNull(GameSentence.create(null, new ArrayList<Particle>()));
		assertNull(GameSentence.create("", Arrays.asList(Particle.DAKE)));
		assertNull(GameSentence.create("B", Arrays.asList(Particle.DAKE)));
		assertNull(GameSentence.create("B",
				Arrays.asList(Particle.DAKE, Particle.BA)));
		assertNull(GameSentence.create("A",
				Arrays.asList(Particle.DAKE, Particle.BA)));
	}

	@Test
	public void testOneParticle() {
		GameSentence gs = GameSentence
				.create("A", Arrays.asList(Particle.DAKE));
		List<SentencePart> sentence = gs.getSentence();
		assertEquals(1, sentence.size());
		assertTrue(sentence.get(0).isParticle());
		assertEquals(Particle.DAKE, sentence.get(0).particle());
		gs = GameSentence.create("FooA", Arrays.asList(Particle.DAKE));
		sentence = gs.getSentence();
		assertEquals(2, sentence.size());
		assertTrue(sentence.get(1).isParticle());
		assertEquals(Particle.DAKE, sentence.get(1).particle());
		gs = GameSentence.create("AFoo", Arrays.asList(Particle.DAKE));
		sentence = gs.getSentence();
		assertEquals(2, sentence.size());
		assertTrue(sentence.get(0).isParticle());
		assertEquals(Particle.DAKE, sentence.get(0).particle());
	}

	@Test
	public void testOneParticleRepeating() {
		GameSentence gs = GameSentence.create("AA",
				Arrays.asList(Particle.DAKE));
		List<SentencePart> sentence = gs.getSentence();
		List<Particle> particles = gs.getParticles();
		assertEquals(2, sentence.size());
		assertEquals(1, particles.size());
		assertTrue(sentence.get(0).isParticle());
		assertEquals(Particle.DAKE, sentence.get(0).particle());
		assertEquals(Particle.DAKE, sentence.get(1).particle());
		assertEquals(Particle.DAKE, particles.get(0));
	}

	@Test
	public void testFullSentence() {
		GameSentence gs = GameSentence.create(
				"D面白いBA面白いBととととととと末位祭りCsomedsflkjtextA", Arrays.asList(
						Particle.BA, Particle.DE, Particle.GA, Particle.HA));
		List<SentencePart> sentence = gs.getSentence();
		List<Particle> particles = gs.getParticles();
		assertEquals(10, sentence.size());
		assertEquals(4, particles.size());
		assertEquals(Arrays.asList(Particle.HA, Particle.DE, Particle.BA,
				Particle.GA), particles);
		assertEquals(Particle.HA, sentence.get(0).particle());
		assertEquals("面白い", sentence.get(1).text());
		assertEquals(Particle.DE, sentence.get(5).particle());
	}

	@Test
	public void testGuessFullSentence() {
		GameSentence gs = GameSentence.create(
				"D面白いBA面白いBととととととと末位祭りCsomedsflkjtextA", Arrays.asList(
						Particle.BA, Particle.DE, Particle.GA, Particle.HA));
		GuessResult r = gs.guess(Arrays.asList("ha", "de", "ba", "ga"));
		assertTrue(r.isSuccess());
		assertTrue(r.getCorrect() == 4);
		assertTrue(r.getIncorrect() == 0);
		r = gs.guess(Arrays.asList("ba", "de", "ga", "ha"));
		assertFalse(r.isSuccess());
		assertTrue(r.getCorrect() == 1);
		assertTrue(r.getIncorrect() == 3);
		r = gs.guess(Arrays.asList("は", "で"));
		assertFalse(r.isSuccess());
		assertTrue(r.getCorrect() == 2);
		assertTrue(r.getIncorrect() == 0);

	}
}

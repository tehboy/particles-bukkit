package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

public class GameSentence {
	public enum SlotType {
		STR, PAR
	}

	public static class SentencePart {
		private final SlotType type;
		private final String val;

		public boolean isParticle() {
			return type == SlotType.PAR;
		}

		private SentencePart(SlotType type, String val) {
			this.type = type;
			this.val = val;
		}

		public Particle particle() {
			if (type != SlotType.PAR) {
				throw new UnsupportedOperationException();
			}
			return Particle.lookup(val);
		}

		public String text() {
			if (type != SlotType.STR) {
				throw new UnsupportedOperationException();
			}
			return val;
		}

		@Override
		public String toString() {
			return "SentencePart [type=" + type + ", val=" + val + "]";
		}

	}

	private final List<SentencePart> values;
	private final List<Particle> particles;

	private GameSentence(List<SentencePart> values, List<Particle> particles) {
		this.values = values;
		this.particles = particles;
	}

	public GuessResult guess(List<String> guess) {
		final int guessCnt = guess.size(), cnt = particles.size();
		int correct = 0, incorrect = 0;
		for (int i = 0; i < guessCnt && i < cnt; i++) {
			Particle g = Particle.lookup(guess.get(i));
			if (g != null && g.equals(particles.get(i))) {
				correct++;
			} else {
				incorrect++;
			}
		}
		GuessResult r = new GuessResult();
		r.setCorrect(correct);
		r.setIncorrect(incorrect);
		final boolean success = incorrect == 0 && guessCnt == cnt;
		r.setSuccess(success);
		return r;
	}

	public List<SentencePart> getSentence() {
		return values;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public static GameSentence create(String sentence, List<Particle> particles) {
		if (sentence == null) {
			return null;
		}
		int numParticles = particles.size();
		final TreeMap<Integer, Particle> partLoc = new TreeMap<>();
		for (int i = 0; i < numParticles; i++) {
			Character c = Character.valueOf((char) (65 + i));
			int currentPos = sentence.indexOf(c);
			if (currentPos == -1) {
				return null;
			}
			Particle p = particles.get(i);
			while (currentPos != -1) {
				partLoc.put(currentPos, p);
				currentPos = sentence.indexOf(c, currentPos + 1);
			}
		}
		List<SentencePart> values = new ArrayList<>();
		LinkedHashSet<Particle> orderedParticles = new LinkedHashSet<>();
		int idx = 0;
		for (Entry<Integer, Particle> e : partLoc.entrySet()) {
			int nxt = e.getKey();
			Particle p = e.getValue();
			orderedParticles.add(p);
			if (nxt != idx) {
				// There's some in-between text
				values.add(new SentencePart(SlotType.STR, sentence.substring(
						idx, nxt)));
			}
			values.add(new SentencePart(SlotType.PAR, p.toString()));
			idx = nxt + 1;
		}
		if (idx != sentence.length()) {
			values.add(new SentencePart(SlotType.STR, sentence.substring(idx)));
		}

		return new GameSentence(values, new ArrayList<>(orderedParticles));
	}

	@Override
	public String toString() {
		return "GameSentence [values=" + values + ", particles=" + particles
				+ "]";
	}

}

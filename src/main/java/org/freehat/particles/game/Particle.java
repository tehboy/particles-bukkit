package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.freehat.particles.game.ParticleLevel.*;

public enum Particle {
	HA(BEGINNER, "は", "ha"), GA(BEGINNER, "が", "ga"), KA(BEGINNER, "か", "ka"), NO(
			BEGINNER, "の", "no"), WO(BEGINNER, "を", "wo"), NI(BEGINNER, "に",
			"ni"), DE(BEGINNER, "で", "de"), KARA_FROM(BEGINNER, "から (from)",
			"から", "kara"), KARA_BEC(BEGINNER, "から (because)", "から", "kara"), MADE(
			BEGINNER, "まで", "made"), TO_WITH(BEGINNER, "と (with)", "と", "to"), TO_AND(
			BEGINNER, "と (and)", "と", "to"), MO(BEGINNER, "も (also)", "も", "mo"), NE(
			BEGINNER, "ね", "ne"), YO(BEGINNER, "よ", "yo"), DAKE(ADVANCED, "だけ",
			"dake"), SHIKA(ADVANCED, "しか", "shika"), KUNI(ADVANCED,
			"～くに (adverbial form of adjectives)", "くに", ""), TARA(ADVANCED,
			"～たら", "たら", "tara"), BA(ADVANCED, "～ば", "ば", "ba"), NARA(ADVANCED,
			"～なら", "なら", "nara"), SHI(ADVANCED, "～し (listing reasons)", "し",
			"shi");

	private static final Map<String, Particle> PARTICLES = new HashMap<>();
	static {
		for (Particle p : values()) {
			for (String name : p.names) {
				PARTICLES.put(name, p);
			}
		}
	}
	private final String[] names;
	private final ParticleLevel level;

	Particle(ParticleLevel level, String... matches) {
		this.names = matches;
		this.level = level;
	}

	public List<String> getPossibleNames() {
		return Arrays.asList(names);
	}

	@Override
	public String toString() {
		return names[0];
	}

	public static Particle lookup(String string) {
		return PARTICLES.get(string);
	}

	public static List<Particle> randomParticles(ParticleLevel level, int count) {
		List<Particle> ps = new ArrayList<>();
		for (Particle p : values()) {
			if (p.level.ordinal() <= level.ordinal()) {
				ps.add(p);
			}
		}
		Collections.shuffle(ps);
		return ps.subList(0, Math.min(ps.size(), count));
	}

	public static void main(String... args) {

		String a = "A";
		String b = "A";
		System.out.println(a.equals(b));
		System.out.println((int) a.charAt(0));
	}

}
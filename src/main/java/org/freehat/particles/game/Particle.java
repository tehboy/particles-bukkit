package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.freehat.particles.game.ParticleLevel.*;

public enum Particle {
	HA(BEGINNER, "は", "ha"), GA(BEGINNER, "が", "ga"), DE(BEGINNER, "で", "de"), NI(
			BEGINNER, "に", "ni"), KARA(BEGINNER, "から", "kara"), NO(BEGINNER,
					"の", "no"), MADE(BEGINNER, "まで", "made"), MO(BEGINNER, "も", "mo"), WO(
							BEGINNER, "を", "wo"), HE(BEGINNER, "へ", "he"), KA(BEGINNER, "か",
									"ka"), TO(BEGINNER, "と", "to"), YA(BEGINNER, "や", "ya"), NADO(
											ADVANCED, "など", "nado"), YORI(ADVANCED, "より", "yori"), DEMO(
													ADVANCED, "でも", "demo"), DAKE(ADVANCED, "だけ", "dake");

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
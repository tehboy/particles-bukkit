package org.freehat.particles.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Particle {
	HA("は", "ha"), GA("が", "ga"), DE("で", "de"), NI("に", "ni"), KARA("から",
			"kara"), NO("の", "no"), MADE("まで", "made"), MO("も", "mo"), WO("を",
			"wo"), HE("へ", "he"), KA("か", "ka"), TO("と", "to"), YA("や", "ya"), NADO(
			"など", "nado"), YORI("より", "yori"), DEMO("でも", "demo"), DAKE("だけ",
			"dake");

	private static final Map<String, Particle> PARTICLES = new HashMap<>();
	static {
		for (Particle p : values()) {
			for (String name : p.names) {
				PARTICLES.put(name, p);
			}
		}
	}
	private final String[] names;

	Particle(String... matches) {
		this.names = matches;
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

	public static List<Particle> randomParticles(int count) {
		Particle[] values = values();
		List<Particle> ps = new ArrayList<>(Arrays.asList(values()));
		Collections.shuffle(ps);
		return ps.subList(0, Math.min(values.length, count));
	}

	public static void main(String... args) {

		String a = "A";
		String b = "A";
		System.out.println(a.equals(b));
		System.out.println((int) a.charAt(0));
	}

}
package org.chaoticbits.collabcloud.codeprocessor.java;

import static org.chaoticbits.collabcloud.codeprocessor.java.JavaTokenType.CLASS;
import static org.chaoticbits.collabcloud.codeprocessor.java.JavaTokenType.ENUM;
import static org.chaoticbits.collabcloud.codeprocessor.java.JavaTokenType.METHOD;
import static org.chaoticbits.collabcloud.codeprocessor.java.JavaTokenType.PACKAGE;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.chaoticbits.collabcloud.CloudWeights;
import org.chaoticbits.collabcloud.ISummaryToken;
import org.chaoticbits.collabcloud.ITokenType;
import org.chaoticbits.collabcloud.visualizer.color.IColorScheme;

public class JavaColorScheme implements IColorScheme {

	private final Random randJitter;
	private final static Map<JavaTokenType, Color> colorMap = new HashMap<JavaTokenType, Color>();
	private final int jitterMax;

	public JavaColorScheme() {
		randJitter = null;
		jitterMax = 0;
		initColorMap();
	}

	public JavaColorScheme(Random randJitter, int jitterMax) {
		this.randJitter = randJitter;
		this.jitterMax = jitterMax;
		initColorMap();
	}

	private void initColorMap() {
		colorMap.put(CLASS, new Color(166, 4, 0));
		colorMap.put(METHOD, new Color(0, 96, 100));
		colorMap.put(PACKAGE, new Color(166, 81, 0));
		colorMap.put(ENUM, new Color(0, 130, 9));

	}

	public Color lookup(ISummaryToken token, CloudWeights weights) {
		return jittered(lookupColor(token.getType()));

	}

	private Color jittered(Color color) {
		if (randJitter == null)
			return new Color(color.getRGB());
		int jitter = randJitter.nextInt(jitterMax) - jitterMax / 2;
		return new Color(inBounds(color.getRed() + jitter), inBounds(color.getGreen() + jitter), inBounds(color.getBlue() + jitter));
	}

	private int inBounds(int i) {
		return Math.min(Math.max(i, 0), 255);
	}

	private Color lookupColor(ITokenType type) {
		Color color = colorMap.get(type);
		if (color == null)
			color = new Color(100, 100, 100);
		return color;
	}

}

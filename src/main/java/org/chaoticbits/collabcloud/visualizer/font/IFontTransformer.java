package org.chaoticbits.collabcloud.visualizer.font;

import java.awt.Font;

import org.chaoticbits.collabcloud.ISummaryToken;

public interface IFontTransformer {

	abstract public Font transform(ISummaryToken token, Double weight);

}

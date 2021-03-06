package org.chaoticbits.collabcloud.visualizer.placement;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import org.chaoticbits.collabcloud.ISummaryToken;

public class RandomPlacement implements IPlaceStrategy {

	private final Random rand;
	private final Rectangle2D boundary;

	public RandomPlacement(Random rand, Rectangle2D boundary) {
		this.rand = rand;
		this.boundary = boundary;
	}

	public Point2D getStartingPlace(ISummaryToken token, Shape shape) {
		return new Point2D.Double(boundary.getMinX() + rand.nextDouble() * boundary.getWidth(), boundary.getMinY() + rand.nextDouble()
				* boundary.getHeight());
	}

}

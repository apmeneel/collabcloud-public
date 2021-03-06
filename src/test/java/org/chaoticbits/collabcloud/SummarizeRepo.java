package org.chaoticbits.collabcloud;

import japa.parser.ParseException;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.log4j.PropertyConfigurator;
import org.chaoticbits.collabcloud.codeprocessor.IWeightModifier;
import org.chaoticbits.collabcloud.codeprocessor.MultiplyModifier;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaColorScheme;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaProjectSummarizer;
import org.chaoticbits.collabcloud.vc.git.GitLoader;
import org.chaoticbits.collabcloud.vc.git.GitLoaderTest;
import org.chaoticbits.collabcloud.visualizer.AWTIntersector;
import org.chaoticbits.collabcloud.visualizer.HierarchicalBoxIntersector;
import org.chaoticbits.collabcloud.visualizer.LastHitCache.IHitCheck;
import org.chaoticbits.collabcloud.visualizer.LayoutTokens;
import org.chaoticbits.collabcloud.visualizer.color.IColorScheme;
import org.chaoticbits.collabcloud.visualizer.font.BoundedLogFont;
import org.chaoticbits.collabcloud.visualizer.font.IFontTransformer;
import org.chaoticbits.collabcloud.visualizer.placement.CenteredTokenWrapper;
import org.chaoticbits.collabcloud.visualizer.placement.IPlaceStrategy;
import org.chaoticbits.collabcloud.visualizer.placement.ParentNetworkPlacement;
import org.chaoticbits.collabcloud.visualizer.placement.RandomPlacement;
import org.chaoticbits.collabcloud.visualizer.spiral.SpiralIterator;
import org.eclipse.osgi.service.resolver.NativeCodeDescription;

public class SummarizeRepo {
	private static final int WIDTH = 800;
	private static final int HEIGHT = 800;
	private static final int MAX_TOKENS = 100;
	private static final double LEAF_CUTOFF = 1.0d;
	private static final int SPIRAL_STEPS = 500;
	private static final double SPIRAL_MAX_RADIUS = 350.0d;
	private static final double SQUASHDOWN = 1;
	private static final SpiralIterator spiral = new SpiralIterator(SPIRAL_MAX_RADIUS, SPIRAL_STEPS, SQUASHDOWN);
	private static final File TEST_BED = new File("testgitrepo");
	private static final File THIS_REPO = new File("");
	private static final File JBOSS_AS_REPO = new File("c:/data/jboss-as");
	private static final String JBOSS_AS_COMMIT_ID = "8321b7f693275c23eb9a515f8a6aed958d49b3b2";
	private static final String THIS_REPO_SECOND_COMMIT_ID = "4cfde077a84185b06117bcff5d47c53644463b1f";
	private static final File JENKINS_REPO = new File("c:/local/data/jenkins");
	private static final String JENKINS_BACK_LIMIT_COMMIT_ID = "df1094651bdefeda57d974a97907521eb21aef7b";
	private static final File JUNIT_REPO = new File("c:/local/data/junit");
	private static final String JUNIT_BACK_LIMIT_COMMIT_ID = "403f761da11bdaf9a03538139e7ae51601c36b0e";
	private static final Random RAND = new Random();
	private static final IPlaceStrategy RANDOM_PLACE_STRATEGY = new CenteredTokenWrapper(new RandomPlacement(RAND,
			new Rectangle2D.Double(WIDTH / 4, HEIGHT / 4, WIDTH / 4, HEIGHT / 4)));

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SummarizeRepo.class);
	private static IWeightModifier modifier = new MultiplyModifier(1.2);
	private static Font INITIAL_FONT = new Font("Lucida Sans", Font.BOLD, 150);
	// private static IColorScheme COLOR_SCHEME = new RandomGrey(RAND, 25, 175);
	private static IColorScheme COLOR_SCHEME = new JavaColorScheme(RAND, 20);
	private static double MAX_FONT_SIZE = 50.0d;

	private static final AWTIntersector intersector = new AWTIntersector(10, LEAF_CUTOFF);
	private static final IHitCheck<Shape> checker = new IHitCheck<Shape>() {
		public boolean hits(Shape a, Shape b) {
			return intersector.hits(a, b);
		}
	};

	public static void main(String[] args) throws ParseException, IOException {
		PropertyConfigurator.configure("log4j.properties");
		CloudWeights weights;
		// weights = testBed();
		weights = junit();
		// weights = thisRepo();
		// weights = jenkins();
		// weights = jboss();
		// System.out.println("==Weights after Diff Adjustment==");
		// System.out.println(weights);
		IFontTransformer FONT_TRANSFORMER = new BoundedLogFont(INITIAL_FONT, weights, MAX_FONT_SIZE);
		// IFontTransformer FONT_TRANSFORMER = new NonParametricFont(INITIAL_FONT, weights,
		// MathTransforms.fourthPower, MAX_FONT_SIZE);
		IPlaceStrategy parentNetworkPlace = new CenteredTokenWrapper(new ParentNetworkPlacement(weights.tokens(),
				new Dimension(WIDTH / 2, HEIGHT / 2), new Point2D.Double(3 * WIDTH / 4, 3 * HEIGHT / 4)));
		// IPlaceStrategy contributionNetworkPlaceStrategy = new CenteredTokenWrapper(new
		// ContributionNetworkPlacement(weights.tokens(),
		// developers, new Dimension(WIDTH / 2, HEIGHT / 2), new Point2D.Double(2 * WIDTH / 3, 2 * HEIGHT /
		// 3)));
		BufferedImage bi = new LayoutTokens(WIDTH, HEIGHT, MAX_TOKENS, FONT_TRANSFORMER, checker, parentNetworkPlace,
				spiral, COLOR_SCHEME).makeImage(weights, new File("output/summarizerepo.png"), "PNG");
		// log.info("Shape-box intersection took: " + AWTIntersector.boxIntersectMS + "ms");
		log.info("Writing image...");
		ImageIO.write(bi, "PNG", new File("output/summarizerepo.png"));
		log.info("Done!");
		// System.out.println(PerformanceProfiler.getInstance().report());
	}

	private static CloudWeights jboss() throws IOException {
		log.info("Summarizing JBoss Application Server...");
		CloudWeights weights = new JavaProjectSummarizer().summarize(new File(JBOSS_AS_REPO.getAbsolutePath()));
		log.info("Weighting against the repo...");
		// weights = new GitLoader(new File(JBOSS_AS_REPO.getAbsolutePath() + "/.git"),
		// JBOSS_AS_COMMIT_ID).crossWithDiff(weights, modifier);
		return weights;
	}

	private static CloudWeights jenkins() throws IOException {
		log.info("Summarizing the project...");
		CloudWeights weights = new JavaProjectSummarizer().summarize(new File(JENKINS_REPO.getAbsolutePath()));
		log.info("Weighting against the repo...");
		weights = new GitLoader(new File(JENKINS_REPO.getAbsolutePath() + "/.git"), JENKINS_BACK_LIMIT_COMMIT_ID)
				.crossWithDiff(weights, modifier);
		return weights;
	}

	private static CloudWeights junit() throws IOException {
		log.info("Summarizing the project...");
		CloudWeights weights = new JavaProjectSummarizer().summarize(new File(JUNIT_REPO.getAbsolutePath()));
		log.info("Weighting against the repo...");
		weights = new GitLoader(new File(JUNIT_REPO.getAbsolutePath() + "/.git"), JUNIT_BACK_LIMIT_COMMIT_ID)
				.crossWithDiff(weights, modifier);
		return weights;
	}

	private static CloudWeights thisRepo() throws IOException {
		log.info("Summarizing the project...");
		CloudWeights weights = new JavaProjectSummarizer().summarize(new File(THIS_REPO.getAbsolutePath() + "/src"));
		log.info("Weighting against the repo...");
		weights = new GitLoader(new File(THIS_REPO.getAbsolutePath() + "/.git"), THIS_REPO_SECOND_COMMIT_ID)
				.crossWithDiff(weights, modifier);
		return weights;
	}

	private static CloudWeights testBed() throws IOException {
		log.info("Summarizing the project...");
		CloudWeights weights = new JavaProjectSummarizer().summarize(TEST_BED);
		log.info("Weighting against the repo...");
		GitLoader gitLoader = new GitLoader(new File(TEST_BED.getAbsolutePath() + "/.git"),
				GitLoaderTest.SECOND_COMMIT_ID);
		weights = gitLoader.crossWithDiff(weights, modifier);
		// TODO load contribution network stuff too
		return weights;
	}

}

package org.chaoticbits.collabcloud.codeprocessor;

import static org.junit.Assert.assertEquals;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;

import java.io.File;
import java.io.IOException;

import org.chaoticbits.collabcloud.CloudWeights;
import org.chaoticbits.collabcloud.ISummarizable;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaClassSummarizable;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaPackageSummarizable;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaSummarizeVisitor;
import org.chaoticbits.collabcloud.codeprocessor.java.JavaSummaryToken;
import org.junit.Test;

public class SummarizerTest {

	private final JavaClassSummarizable contrivedExample = new JavaClassSummarizable(source("ContrivedExample.java"));
	private final JavaClassSummarizable helloWorld = new JavaClassSummarizable(source("HelloWorld.java"));
	private final JavaClassSummarizable isPrime = new JavaClassSummarizable(source("IsPrime.java"));

	@Test
	public void declaredMethod() throws Exception {
		JavaSummarizeVisitor v = new JavaSummarizeVisitor(contrivedExample);
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(v, new CloudWeights());
		assertEquals("weight of 1.0", 1.0d, lookUp(weights, contrivedExample, "uncalledMethod"), 0.0000001);
	}

	@Test
	public void mainMethodIncluded() throws Exception {
		CloudWeights weights = JavaParser.parse(helloWorld.getFile()).accept(new JavaSummarizeVisitor(helloWorld), new CloudWeights());
		assertEquals("main method is included", 1.0d, lookUp(weights, helloWorld, "main"), 0.0000001);
	}

	@Test
	public void packageNames() throws Exception {
		CloudWeights weights = JavaParser.parse(isPrime.getFile()).accept(new JavaSummarizeVisitor(isPrime), new CloudWeights());
		assertEquals(
				"include last word of package name",
				0.5d,
				lookUp(weights, new JavaPackageSummarizable("org.chaoticbits.collabcloud.testinputs"), "org.chaoticbits.collabcloud.testinputs"),
				0.0000001);
	}

	@Test
	public void ignoreLocalVariables() throws Exception {
		CloudWeights weights = JavaParser.parse(isPrime.getFile()).accept(new JavaSummarizeVisitor(isPrime), new CloudWeights());
		assertEquals("local variables not included", 0.0d, lookUp(weights, contrivedExample, "factors"), 0.0000001);
	}

	@Test
	public void methodCalls() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("methods called 5 times (*0.20), declared once ", 2.0d, lookUp(weights, contrivedExample, "methodCalledMultipleTimes"),
				0.0000001);
	}

	@Test
	public void externalMethodCalls() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("external method calls", 0.20d, lookUp(weights, contrivedExample, "random"), 0.0000001);
	}

	@Test
	public void classOrInterfaceName() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("include the compilation unit name", 2.0d, lookUp(weights, contrivedExample, "ContrivedExample"), 0.0000001);
	}

	@Test
	public void enumDeclaration() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("include the enum declaration", 0.6d, lookUp(weights, contrivedExample, "FRUIT"), 0.0000001);
	}

	@Test
	public void enumConstants() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("include the enum declaration", 0.1d, lookUp(weights, contrivedExample, "Apples"), 0.0000001);
		assertEquals("include the enum declaration", 0.1d, lookUp(weights, contrivedExample, "Bananas"), 0.0000001);
		assertEquals("include the enum declaration", 0.1d, lookUp(weights, contrivedExample, "Oranges"), 0.0000001);
	}

	@Test
	public void ignoreToString() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("ignore toString declaration and call", 0.0d, lookUp(weights, contrivedExample, "toString"), 0.0000001);
	}

	@Test
	public void ignoreHashCode() throws Exception {
		CloudWeights weights = JavaParser.parse(contrivedExample.getFile()).accept(new JavaSummarizeVisitor(contrivedExample),
				new CloudWeights());
		assertEquals("ignore hashCode declaration and call", 0.0d, lookUp(weights, contrivedExample, "hashCode"), 0.0000001);
	}

	private File source(String name) {
		return new File("src/test/java/org/chaoticbits/collabcloud/testinputs/" + name);
	}

	public static void main(String[] args) throws ParseException, IOException {
		CompilationUnit unit = JavaParser.parse(new File("src/test/java/org/chaoticbits/collabcloud/testinputs/ContrivedExample.java"));
		System.out.println(unit);
	}

	private double lookUp(CloudWeights weights, ISummarizable artifact, String token) {
		return weights.get(new JavaSummaryToken(artifact, null, token, null));
	}

}

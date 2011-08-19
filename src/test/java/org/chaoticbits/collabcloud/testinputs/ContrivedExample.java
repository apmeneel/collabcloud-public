package org.chaoticbits.collabcloud.testinputs;

public class ContrivedExample {

	public static void main(String[] args) {
		printHelloWorld();
		methodCalledMultipleTimes();
		methodCalledMultipleTimes();
		methodCalledMultipleTimes();
		methodCalledMultipleTimes();
		methodCalledMultipleTimes();
		Math.random();
	}

	/* block! */
	private static void methodCalledMultipleTimes() {
		int localVariable = 0;
		localVariable = localVariable + 1;
		System.out.println("Printing out local variable");// inline!
	}

	/**
	 * Javadoc!
	 */
	private static void printHelloWorld() {
		System.out.println("Hello, World!");
	}
	
	@SuppressWarnings("unused")
	private static void uncalledMethod() {
		System.out.println("Hello, World!");
	}
}

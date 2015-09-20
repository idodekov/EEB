package org.redoubt.util;

import java.security.SecureRandom;

public class RandomUtils {
	private static SecureRandom rand = new SecureRandom();
	
	public static int nextInt() {
		return rand.nextInt();
	}
	
	public static int nextInt(int bound) {
		return rand.nextInt(bound);
	}
	
	public static int nextInt(int start, int bound) {
		int randomInt  = rand.nextInt(bound - start);
		if(randomInt < 0) {
			randomInt *= (-1);
		}
		
		return start + randomInt;
	}
}

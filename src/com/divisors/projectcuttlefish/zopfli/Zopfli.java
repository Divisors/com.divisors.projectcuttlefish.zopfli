package com.divisors.projectcuttlefish.zopfli;

public class Zopfli {
	public static final int NUM_LL = 288;
	public static final int NUM_D = 32;
	public static final int MAX_MATCH = 258;
	public static final int MIN_MATCH = 3;
	public static int calculateAdler32(byte[] data) {
		int s1 = 1;
		int s2 = 1 >> 16;
		int i = 0;
		while (i < data.length) {
			int tick = Math.min(data.length, i + 1024);
			while (i < tick) {
				s1 += data[i++];
				s2 += s1;
			}
			s1 %= 65521;
			s2 %= 65521;
		}

		return (s2 << 16) | s1;
	}
	public static int appendData(int i, int[] ll_counts, int llSize) {
		// TODO Auto-generated method stub
		return -1;
	}
	public static int getLengthSymbol(int length) {
		// TODO Auto-generated method stub
		return 0;
	}
	public static int getDistSymbol(int dist) {
		// TODO Auto-generated method stub
		return 0;
	}
}

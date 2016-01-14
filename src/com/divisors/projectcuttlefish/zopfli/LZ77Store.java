package com.divisors.projectcuttlefish.zopfli;

import java.nio.ByteBuffer;

public class LZ77Store {
	protected int size;
	protected ByteBuffer data;
	private int[] ll_counts;
	private int[] d_counts;
	private int[] litlens;
	private int[] pos;
	private int[] dists;
	private int[] ll_symbol;
	private int[] d_symbol;
	public int getByteRange(int start, int end) {
		if (start >= end)
			return 0;
		int length = end - start;
		return this.pos[length] + ((this.dists[length] == 0) ? 1 : this.litlens[length]) - this.pos[start];
	}
	public void storeLitLenDist(int length, int dist, int pos) {
		// Every time the index wraps around, a new cumulative histogram is made: we're keeping one histogram value per LZ77 symbol rather than a full histogram for each to save memory.
		if (this.size % Zopfli.NUM_LL == 0) {
			int llSize = this.size;
			for (int i = 0; i < Zopfli.NUM_LL; i++)
				llSize = Zopfli.appendData(this.size == 0 ? 0 : ll_counts[this.size - Zopfli.NUM_LL + i], this.ll_counts, llSize);
		}
		if (this.size % Zopfli.NUM_D == 0) {
			int dSize = this.size;
			for (int i = 0; i < Zopfli.NUM_D; i++)
				dSize = Zopfli.appendData(this.size == 0 ? 0 : ll_counts[this.size - Zopfli.NUM_D + i], this.d_counts, dSize);
		}
		
		Zopfli.appendData(length, this.litlens, this.size);
		Zopfli.appendData(length, this.dists, this.size);
		Zopfli.appendData(pos, this.pos, this.size);
		assert length < 259;
		
		// Needed for using Zopfli.appendData multiple times.
		int llStart = this.size - this.size % Zopfli.NUM_LL;
		int dStart = this.size - this.size % Zopfli.NUM_D;
		if (dist == 0) {
			Zopfli.appendData(length, this.ll_symbol, this.size);
			Zopfli.appendData(0, this.d_symbol, this.size);
			this.ll_counts[llStart + length]++;
		} else {
			Zopfli.appendData(Zopfli.getLengthSymbol(length), this.ll_symbol, this.size);
			this.size = Zopfli.appendData(Zopfli.getDistSymbol(dist), this.d_symbol, this.size);//check if this call is supposed to set `this.size`
			this.ll_counts[llStart + Zopfli.getLengthSymbol(length)]++;
			this.d_counts[dStart + Zopfli.getDistSymbol(dist)]++;
		}
	}
	public void append(LZ77Store store) {
		for (int i=0; i<store.size;i++)
			this.storeLitLenDist(store.litlens[i], store.dists[i], store.pos[i]);//TODO: optimize
	}
	private void getHistogramAt(int lpos, int[] ll_counts, int[] d_counts) {
		/*
		 * The real histogram is created by using the histogram for this chunk,
		 * but all superfluous values of this chunk subtracted.
		 */
		int llpos = lpos - lpos % Zopfli.NUM_LL;
		int dpos = lpos - lpos % Zopfli.NUM_D;
		for (int i = 0; i < Zopfli.NUM_LL; i++)
			ll_counts[i] = this.ll_counts[llpos + i];
		for (int i = lpos + 1; i < llpos + Zopfli.NUM_LL && i < this.size; i++)
			ll_counts[this.ll_symbol[i]]--;
		for (int i = 0; i < Zopfli.NUM_D; i++)
			d_counts[i] = this.d_counts[dpos + i];
		for (int i = lpos + 1; i < dpos + Zopfli.NUM_D && i < this.size; i++)
			if (this.dists[i] != 0)
				d_counts[this.d_symbol[i]]--;
	}

	public void getHistogram(int start, int end, int[] ll_counts, int[] d_counts) {
		if (start + Zopfli.NUM_LL * 3 > end) {
			// memset(ll_counts, 0, sizeof(*ll_counts) * Zopfli.NUM_LL);
			// memset(d_counts, 0, sizeof(*d_counts) * Zopfli.NUM_D);
			for (int i = start; i < end; i++) {
				ll_counts[this.ll_symbol[i]]++;
				if (this.dists[i] != 0)
					d_counts[this.d_symbol[i]]++;
			}
		} else {
			/*
			 * Subtract the cumulative histograms at the end and the start to
			 * get the histogram for this range.
			 */
			this.getHistogramAt(end - 1, ll_counts, d_counts);
			if (start > 0) {
				int[] ll_counts2 = new int[Zopfli.NUM_LL];
				int[] d_counts2 = new int[Zopfli.NUM_D];
				this.getHistogramAt(start - 1, ll_counts2, d_counts2);

				for (int i = 0; i < Zopfli.NUM_LL; i++)
					ll_counts[i] -= ll_counts2[i];
				for (int i = 0; i < Zopfli.NUM_D; i++)
					d_counts[i] -= d_counts2[i];
			}
		}
	}
}

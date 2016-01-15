package com.divisors.projectcuttlefish.zopfli;

public class ZopfliBlockState {
	final int blockStart, blockEnd;
	final ZopfliLongestMatchCache lmc;
	public ZopfliBlockState(int blockStart, int blockEnd, boolean add_lmc) {
		this.blockStart = blockStart;
		this.blockEnd = blockEnd;
		if (ZopfliUtil.LONGEST_MATCH_CASE && add_lmc)
			this.lmc = new ZopfliLongestMatchCache(blockEnd - blockStart);
		else
			this.lmc = null;
	}
	/**
	 * NOTE: length, limit, and distance ARE POINTERS, AND ARE SUPPOSED TO BE SET TODO: fix
	 * @param pos
	 * @param limit
	 * @param sublen
	 * @param distance
	 * @param length
	 * @return
	 */
	boolean tryGetFromLongestMatchCase(int pos, int limit, int[] sublen, int distance, int length) {
		//The LMC cache starts at the beginning of the block rather than the beginning of the whole array.
		int lmcPos = pos - this.blockStart;
		
		//Length > 0 and dist 0 is invalid combination, which indicates on purpose that this cache value is not filled in yet.
		boolean cache_available = (this.lmc != null) && (this.lmc.length[lmcPos] == 0 || this.lmc.dist[lmcPos] != 0);
		boolean limit_ok_for_cache = cache_available && (limit == Zopfli.MAX_MATCH || this.lmc.length[lmcPos] <= limit || ((sublen != null) && this.lmc.getMaxSublen(lmcPos, this.lmc.length[lmcPos]) >= limit));
		
		if ((this.lmc != null) && limit_ok_for_cache && cache_available) {
			if ((sublen == null) || this.lmc.length[lmcPos] <= this.lmc.getMaxSublen(lmcPos, this.lmc.length[lmcPos])) {
				length = this.lmc.length[lmcPos];
				if (length > limit)
					length = limit;
				if (sublen != null) {
					this.lmc.toSublen(lmcPos, length, sublen);
			        distance = sublen[length];
					if (limit == Zopfli.MAX_MATCH && length >= Zopfli.MIN_MATCH) {
						assert (sublen[length] == this.lmc.dist[lmcPos]);
					}
				} else {
					distance = this.lmc.dist[lmcPos];
				}
				return true;
			}
			//Can't use much of the cache, since the "sublens" need to be calculated, but at least we already know when to stop.
			limit = this.lmc.length[lmcPos];
		}
		return false;
	}
	/**
	 * Stores the found sublen, distance and length in the longest match cache, if
	 * possible.
	 */
	void StoreInLongestMatchCache(int pos, int limit, final int[] sublen, int distance, int length) {
	  /* The LMC cache starts at the beginning of the block rather than the
	     beginning of the whole array. */
	  int lmcpos = pos - this.blockStart;

	  /* Length > 0 and dist 0 is invalid combination, which indicates on purpose
	     that this cache value is not filled in yet. */
	  boolean cache_available = s->lmc && (this.lmc.length[lmcpos] == 0 || this.lmc.dist[lmcpos] != 0);

	  if (s->lmc && limit == ZOPFLI_MAX_MATCH && sublen && !cache_available) {
	    assert(this.lmc.length[lmcpos] == 1 && this.lmc.dist[lmcpos] == 0);
	    this.lmc.dist[lmcpos] = length < Zopfli.MIN_MATCH ? 0 : distance;
	    this.lmc.length[lmcpos] = length < Zopfli.MIN_MATCH ? 0 : length;
	    assert(!(this.lmc.length[lmcpos] == 1 && this.lmc.dist[lmcpos] == 0));
	    ZopfliSublenToCache(sublen, lmcpos, length, s->lmc);
	  }
	}
}

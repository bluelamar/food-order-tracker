/**
 * 
 */
package org.bluelamar.fotracker.service;

import org.bluelamar.fotracker.Decayer;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mark
 *
 */
public class FoodDecayerTest {

	Decayer decayer;
	private long shelfLife;
	private long curTimeSecs;
	private float decayRate;
	private long orderTimeSecs;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		decayer = new FoodDecayer();
		shelfLife = 120;
		curTimeSecs = System.currentTimeMillis() / 1000;
		decayRate = (float)3.5;
		orderTimeSecs = curTimeSecs - 60;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodDecayer#decay(long, long, long, float)}.
	 */
	@Test
	public void testDecay() throws Exception {
		
		long ret = decayer.decay(shelfLife, curTimeSecs, orderTimeSecs, decayRate);
		Assert.assertEquals(-150, ret);
		
		ret = decayer.decay(shelfLife, curTimeSecs, orderTimeSecs+60, decayRate);
		Assert.assertEquals(120, ret);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodDecayer#normalized(long, long, long, float)}.
	 */
	@Test
	public void testNormalized() throws Exception {
		long ret = decayer.normalized(shelfLife, curTimeSecs, orderTimeSecs, decayRate);
		Assert.assertEquals(-1, ret);
		
		ret = decayer.normalized(shelfLife, curTimeSecs, orderTimeSecs+60, decayRate);
		Assert.assertEquals(1, ret);
	}

}

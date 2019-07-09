/**
 * 
 */
package org.bluelamar.fotracker.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mark
 *
 */
public class FoodOrderTest {

	FoodOrder foodOrder;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		foodOrder = new FoodOrder();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.FoodOrder#toString()}.
	 */
	@Test
	public void testToString() throws Exception {
		String str = foodOrder.toString();
		Assert.assertTrue("actual str="+str, str.contains("Food[ name=null:"));
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.FoodOrder#getName()}.
	 */
	@Test
	public void testGetName() throws Exception {
		String val = foodOrder.getName();
		Assert.assertEquals(null, val);
		foodOrder.setName("jazzy");
		val = foodOrder.getName();
		Assert.assertEquals("jazzy", val);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.FoodOrder#getTemp()}.
	 */
	@Test
	public void testGetTemp() throws Exception {
		String val = foodOrder.getTemp();
		Assert.assertEquals(null, val);
		foodOrder.setTemp("burned");
		val = foodOrder.getTemp();
		Assert.assertEquals("burned", val);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.FoodOrder#getShelfLife()}.
	 */
	@Test
	public void testGetShelfLife() throws Exception {
		Integer val = foodOrder.getShelfLife();
		Assert.assertEquals(null, val);
		foodOrder.setShelfLife(360);
		val = foodOrder.getShelfLife();
		Assert.assertEquals(Integer.valueOf(360), val);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.FoodOrder#getDecayRate()}.
	 */
	@Test
	public void testGetDecayRate() throws Exception {
		Float val = foodOrder.getDecayRate();
		Assert.assertEquals(null, val);
		foodOrder.setDecayRate((float)3.5);
		val = foodOrder.getDecayRate();
		Assert.assertEquals(Float.valueOf((float)3.5), val);
	}

}

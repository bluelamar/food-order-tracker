/**
 * 
 */
package org.bluelamar.fotracker.model;

import org.bluelamar.fotracker.model.TrackedOrder.TempType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mark
 *
 */
public class TrackedOrderTest {

	TrackedOrder trkdOrder;
	FoodOrder foodOrder;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		trkdOrder = new TrackedOrder(1976, TempType.HOT);
		foodOrder = new FoodOrder();
		foodOrder.setName("gelato");
		foodOrder.setShelfLife(180);
		foodOrder.setDecayRate((float)2.75);
		foodOrder.setTemp("hOt");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#TrackedOrder(int, org.bluelamar.fotracker.model.TrackedOrder.TempType)}.
	 */
	@Test
	public void testTrackedOrder() throws Exception {
		TrackedOrder to = new TrackedOrder(99, TempType.AGNOSTIC);
		to = new TrackedOrder(55, TempType.xlate("agnostic"));
		try {
			TempType tt = TempType.xlate("burned");
			Assert.assertFalse(true);
		} catch (TrackedOrder.InvalidTempException exc) {
			Assert.assertTrue(true);
		}
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#toString()}.
	 */
	@Test
	public void testToString() throws Exception {
		String str = trkdOrder.toString();
		Assert.assertTrue("actual str="+str, str.contains("Order[ id=1976:"));
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#getOrderID()}.
	 */
	@Test
	public void testGetOrderID() throws Exception {
		int id = trkdOrder.getOrderID();
		Assert.assertTrue(id == 1976);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#getTemp()}.
	 */
	@Test
	public void testGetTemp() throws Exception {
		TempType tt = trkdOrder.getTemp();
		Assert.assertEquals(TempType.HOT, tt);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#getFoodOrder()}.
	 */
	@Test
	public void testGetFoodOrder() throws Exception {
		trkdOrder.setFoodOrder(null);
		trkdOrder.setFoodOrder(foodOrder);
		FoodOrder forder = trkdOrder.getFoodOrder();
		Assert.assertTrue(forder.equals(foodOrder));
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#getOrderTimeSecs()}.
	 */
	@Test
	public void testGetOrderTimeSecs() throws Exception {
		trkdOrder.setOrderTimeSecs(1000);
		trkdOrder.setOrderTimeSecs(-99);
		long otime = trkdOrder.getOrderTimeSecs();
		Assert.assertEquals(-99, otime);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.model.TrackedOrder#getPickupTimeSecs()}.
	 */
	@Test
	public void testGetPickupTimeSecs() throws Exception {
		trkdOrder.setPickupTimeSecs(55);
		trkdOrder.setPickupTimeSecs(440);
		long ptime = trkdOrder.getPickupTimeSecs();
		Assert.assertEquals(440, ptime);
	}

}

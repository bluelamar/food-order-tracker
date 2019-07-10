/**
 * 
 */
package org.bluelamar.fotracker.service;

import org.bluelamar.fotracker.model.TrackedOrder;
import org.bluelamar.fotracker.model.TrackedOrder.TempType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mark
 *
 */
public class FoodShelfTest {

	static final int MAX_CNT = 3;
	
	OrderIdGenerator idGenerator;
	FoodShelf foodShelf;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		foodShelf = new FoodShelf(MAX_CNT, TempType.COLD);
		idGenerator = new OrderIdGenerator();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#FoodShelf(int, org.bluelamar.fotracker.model.TrackedOrder.TempType)}.
	 */
	@Test
	public void testFoodShelf() throws Exception {
		FoodShelf shelf = new FoodShelf(-1, TempType.FROZEN);
		Assert.assertEquals(FoodShelf.DEF_MAX_CNT, shelf.getMaxCnt());
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#getOrderCnt()}.
	 */
	@Test
	public void testGetOrderCnt() throws Exception {
		int orderCnt = foodShelf.getOrderCnt();
		Assert.assertEquals(0, orderCnt);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#getMaxCnt()}.
	 */
	@Test
	public void testGetMaxCnt() throws Exception {

		Assert.assertEquals(this.MAX_CNT, foodShelf.getMaxCnt());
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#getTemp()}.
	 */
	@Test
	public void testGetTemp() throws Exception {
		Assert.assertEquals(TempType.COLD, foodShelf.getTemp());
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#addOrder(org.bluelamar.fotracker.model.TrackedOrder)}.
	 */
	@Test
	public void testAddOrder() throws Exception {
		TrackedOrder to = new TrackedOrder(idGenerator.generate(), TempType.COLD);
		foodShelf.addOrder(to);
		
		try {
			to = new TrackedOrder(idGenerator.generate(), TempType.HOT);
			foodShelf.addOrder(to);
			Assert.assertTrue(false);
		} catch (TrackedOrder.InvalidTempException exc) {
			Assert.assertTrue(true);
		}
		
		FoodShelf oflowShelf = new FoodShelf(99, TempType.AGNOSTIC);
		try {
			to = new TrackedOrder(idGenerator.generate(), TempType.HOT);
			oflowShelf.addOrder(to);
			Assert.assertTrue(true);
		} catch (TrackedOrder.InvalidTempException exc) {
			Assert.assertTrue(false);
		}
		
		to = new TrackedOrder(idGenerator.generate(), TempType.COLD);
		foodShelf.addOrder(to);
		to = new TrackedOrder(idGenerator.generate(), TempType.COLD);
		foodShelf.addOrder(to);
		try {
			to = new TrackedOrder(idGenerator.generate(), TempType.COLD);
			foodShelf.addOrder(to);
			Assert.assertTrue(false);
		} catch (FoodShelf.ShelfFullException exc) {
			Assert.assertTrue(true);
		}
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#removeOrder(int)}.
	 */
	@Test
	public void testRemoveOrder() throws Exception {
		TrackedOrder to = foodShelf.removeOrder(2019);
		Assert.assertNull(to);
		
		int id = idGenerator.generate();
		to = new TrackedOrder(id, TempType.COLD);
		foodShelf.addOrder(to);
		to = foodShelf.removeOrder(to.getOrderID());
		Assert.assertNotNull(to);
		
		foodShelf.addOrder(to);
	}

	/**
	 * Test method for {@link org.bluelamar.fotracker.service.FoodShelf#getOrders()}.
	 */
	@Test
	public void testGetOrders() throws Exception {
		java.util.Map<Integer, TrackedOrder> orders = foodShelf.getOrders();
		
		int cnt = orders.size();
		
		TrackedOrder to = new TrackedOrder(idGenerator.generate(), TempType.COLD);
		foodShelf.addOrder(to);
		to = new TrackedOrder(idGenerator.generate(), TempType.COLD);
		foodShelf.addOrder(to);
		orders = foodShelf.getOrders();
		Assert.assertEquals(cnt + 2, orders.size());
	}

}

package org.bluelamar.fotracker.service;

import java.util.Map;
import java.util.HashMap;

import org.bluelamar.fotracker.model.*;

public class FoodShelf {
	/* FIX
	static class InvalidTempException extends Exception {
		public InvalidTempException(String badTempStr) {
			super("Invalid temp specified: " + badTempStr);
		}
	}
	
	public enum TempType {
	    HOT,
	    COLD, 
	    FROZEN,
	    AGNOSTIC;
		
		static public TempType xlate(String temp) throws InvalidTempException {
			if (HOT.name().equalsIgnoreCase(temp)) {
				return HOT;
			}
			if (COLD.name().equalsIgnoreCase(temp)) {
				return COLD;
			}
			if (FROZEN.name().equalsIgnoreCase(temp)) {
				return FROZEN;
			}
			if (AGNOSTIC.name().equalsIgnoreCase(temp)) {
				return AGNOSTIC;
			}
			throw new InvalidTempException(temp);
		}
	} */
	
	final int maxCnt;
	final TrackedOrder.TempType tempType;  // shelf holds items of this temp
	final Map<Integer, TrackedOrder> orders;
	
	public FoodShelf(int maxCnt, TrackedOrder.TempType temp) {
		this.maxCnt = maxCnt;
		this.tempType = temp;
		orders = new HashMap<>();
	}
	
	public int getOrderCnt() {
		return orders.size();
	}
	public int getMaxCnt() {
		return maxCnt;
	}
	public TrackedOrder.TempType getTemp() {
		return tempType;
	}
	
	public void addOrder(TrackedOrder order) {
		orders.put(order.getOrderID(), order);
	}
	public TrackedOrder removeOrder(int orderID) {
		return orders.remove(orderID);
	}
	public Map<Integer, TrackedOrder> getOrders() {
		return orders;
	}
}

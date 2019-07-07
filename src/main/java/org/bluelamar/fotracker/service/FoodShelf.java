package org.bluelamar.fotracker.service;

import java.util.Map;
import java.util.HashMap;

import org.bluelamar.fotracker.model.*;

public class FoodShelf {
	
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
	}
	
	int orderCnt;
	final int maxCnt;
	final TempType tempType;  // shelf holds items of this temp
	final Map<Integer, TrackedOrder> orders;
	
	public FoodShelf(int maxCnt, TempType temp) {
		this.maxCnt = maxCnt;
		this.tempType = temp;
		orders = new HashMap<>();
	}
	
	public void setOrderCnt(int orderCnt) {
		this.orderCnt = orderCnt;
	}
	public int getOrderCnt() {
		return orderCnt;
	}
	public int getMaxCnt() {
		return maxCnt;
	}
	public TempType getTemp() {
		return tempType;
	}
	
	public void addOrder(TrackedOrder order) {
		orders.put(order.getOrderID(), order);
	}
}

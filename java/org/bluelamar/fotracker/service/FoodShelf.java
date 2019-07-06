package org.bluelamar.fotracker.service;

import java.util.Map;
import java.util.HashMap;

import org.bluelamar.fotracker.model.*;

public class FoodShelf {
	public enum TempType {
	    HOT,
	    COLD, 
	    FROZEN;
		
		static public TempType xlate(String temp) throws Exception {
			if (HOT.name().equalsIgnoreCase(temp)) {
				return HOT;
			}
			if (COLD.name().equalsIgnoreCase(temp)) {
				return COLD;
			}
			if (FROZEN.name().equalsIgnoreCase(temp)) {
				return FROZEN;
			}
			throw new Exception(); // FIX TODO create a checked exc
		}
	}
	
	int orderCnt;
	final int maxCnt;
	final TempType tempType;
	final Map<Integer, Order> orders;
	
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
	
	public void addOrder(Order order) {
		orders.put(order.getOrderID(), order);
	}
}

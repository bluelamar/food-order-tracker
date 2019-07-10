package org.bluelamar.fotracker.service;

import java.util.Map;
import java.util.HashMap;

import org.bluelamar.fotracker.model.*;
import org.bluelamar.fotracker.model.TrackedOrder.TempType;

public class FoodShelf {
	
	public static class ShelfFullException extends Exception {
        public ShelfFullException(int maxCnt) {
            super("Shelf is full: cannot add more orders: size=" + maxCnt);
        }
    }
    
	static final int DEF_MAX_CNT = 100;
	
    final int maxCnt;
    final TrackedOrder.TempType tempType;  // shelf holds items of this temp
    final Map<Integer, TrackedOrder> orders;
    
    public FoodShelf(int maxCnt, TempType temp) {
        this.maxCnt = maxCnt > 0 ? maxCnt : DEF_MAX_CNT;
        this.tempType = temp;
        orders = new HashMap<>();
    }
    
    public int getOrderCnt() {
        return orders.size();
    }
    public int getMaxCnt() {
        return maxCnt;
    }
    public TempType getTemp() {
        return tempType;
    }
    
    public void addOrder(TrackedOrder order) throws ShelfFullException, TrackedOrder.InvalidTempException {
    	if (orders.size() == maxCnt) {
    		throw new ShelfFullException(maxCnt);
    	}
    	if (tempType != TempType.AGNOSTIC && tempType != order.getTemp()) {
    		throw new TrackedOrder.InvalidTempException(tempType.name() + " shelf cannot hold order of temp=" + order.getTemp().name());
    	}
        orders.put(order.getOrderID(), order);
    }
    public TrackedOrder removeOrder(int orderID) {
        return orders.remove(orderID);
    }
    public Map<Integer, TrackedOrder> getOrders() {
        return orders;
    }
}

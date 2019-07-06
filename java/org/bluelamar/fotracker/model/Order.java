package org.bluelamar.fotracker.model;

public class Order {
	final int orderID;
	FoodOrder foodOrder;
	long orderTimeSecs;  // time order was made, in secs since Jan 1, 1970 UTC
	
	public Order(int orderID) {
		this.orderID = orderID;
	}
	
	public int getOrderID() {
		return orderID;
	}
	public void setFoodOrder(FoodOrder foodOrder) {
		this.foodOrder = foodOrder;
	}
	public FoodOrder getFoodOrder() {
		return foodOrder;
	}
	public void setOrderTimeSecs(long orderTimeSecs) {
		this.orderTimeSecs = orderTimeSecs;
	}
	public long getOrderTimeSecs() {
		return orderTimeSecs;
	}
}

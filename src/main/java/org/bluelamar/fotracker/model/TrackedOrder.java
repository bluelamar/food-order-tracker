package org.bluelamar.fotracker.model;


public class TrackedOrder {
	
	public static class InvalidTempException extends Exception {
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
	
	final int orderID;
	final TempType temp;
	FoodOrder foodOrder;
	long orderTimeSecs;  // time order was made, in secs since Jan 1, 1970 UTC
	long pickupTimeSecs;  // secs since Jan 1, 1970 UTC
	
	public TrackedOrder(int orderID, TempType temp) {
		this.orderID = orderID;
		this.temp = temp;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Order[ id=").append(orderID).append(": order-time=").
			append(orderTimeSecs).append(": pickup-time=").append(pickupTimeSecs).
			append(": ").append(foodOrder).append("]");
		return sb.toString();
	}
	
	public int getOrderID() {
		return orderID;
	}
	public TempType getTemp() {
		return temp;
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
	public void setPickupTimeSecs(long pickupTimeSecs) {
		this.pickupTimeSecs = pickupTimeSecs;
	}
	public long getPickupTimeSecs() {
		return pickupTimeSecs;
	}
}

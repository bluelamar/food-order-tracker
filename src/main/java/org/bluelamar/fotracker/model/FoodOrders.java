package org.bluelamar.fotracker.model;

import java.util.List;

/**
 * Data model for FoodOrders data object.
 *
 */
public class FoodOrders {

    List<FoodOrder> foodOrders;

    public FoodOrders() {
    }

    public void setFoodOrders(List<FoodOrder> foodOrders) {
        this.foodOrders = foodOrders;
    }

    public List<FoodOrder> getFoodOrders() {
        return foodOrders;
    }
}

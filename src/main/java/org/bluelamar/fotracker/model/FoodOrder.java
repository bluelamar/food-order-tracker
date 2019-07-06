package org.bluelamar.fotracker.model;

/**
 * Data model for FoodOrder data object.
 *
 */
public class FoodOrder {

    public String name;
    public String temp;

    public FoodOrder() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTemp() {
        return temp;
    }
    public void setTemp(String temp) {
        this.temp = temp;
    }
}

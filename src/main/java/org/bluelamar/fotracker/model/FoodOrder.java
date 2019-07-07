package org.bluelamar.fotracker.model;

/**
 * Data model for FoodOrder data object.
 *
 */
public class FoodOrder {

    public String name;
    public String temp;
    public Integer shelfLife;
    public Float decayRate;


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
    public Integer getShelfLife() {
        return shelfLife;
    }
    public void setShelfLife(Integer shelfLife) {
        this.shelfLife = shelfLife;
    }
    public Float getDecayRate() {
        return decayRate;
    }
    public void setDecayRate(Float decayRate) {
        this.decayRate = decayRate;
    }
}

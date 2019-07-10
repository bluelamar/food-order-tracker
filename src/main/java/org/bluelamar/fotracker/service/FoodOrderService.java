package org.bluelamar.fotracker.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bluelamar.fotracker.IdGenerator;
import org.bluelamar.fotracker.Decayer;
import org.bluelamar.fotracker.model.FoodOrder;
import org.bluelamar.fotracker.model.TrackedOrder;

public class FoodOrderService implements Runnable {
    
    enum RetCode {
        CREATED,
        BAD_REQ,
        INT_ERR,
        UNAVAIL
    }

    private static final Logger LOG = LoggerFactory.getLogger(FoodOrderService.class);

    static final String ID_GENERATOR_PROP = "fotracker.idgeneratorclass";
    static final String DEF_ID_GENERATOR = "org.bluelamar.fotracker.service.OrderIdGenerator";
    static final String FOOD_DECAYER_PROP = "fotracker.fooddecayerclass";
    static final String DEF_FOOD_DECAYER = "org.bluelamar.fotracker.service.FoodDecayer";
    static final String FOOD_OFLOW_DECAYER_PROP = "fotracker.food_oflow_decayerclass";
    static final String DEF_FOOD_OFLOW_DECAYER = "org.bluelamar.fotracker.service.FoodDecayerOFlow";
    static final String MAX_SHELF_CNT_PROP = "fotracker.shelfmaxcnt";
    static final int DEF_MAX_SHELF_CNT = 15;
    static final String MAX_OF_SHELF_CNT_PROP = "fotracker.overflowshelfmaxcnt";
    static final int DEF_MAX_OF_SHELF_CNT = 20;
    
    Map<TrackedOrder.TempType, FoodShelf> shelves;
    FoodShelf overFlowShelf;
    int maxShelfCnt;
    int maxOfShelfCnt;
    IdGenerator idGenerator;
    Decayer foodDecayer;
    Decayer foodDecayerOFlow;
    PriorityQueue<TrackedOrder> driverQueue;
    ScheduledExecutorService ses;
    Comparator<TrackedOrder> decayRateCompOF; // comparator for the overflow shelf

    public FoodOrderService() {
    }

    public void Init() {
        Object obj = loadClass(ID_GENERATOR_PROP, DEF_ID_GENERATOR);
        idGenerator = (IdGenerator)obj;
        
        obj = loadClass(FOOD_DECAYER_PROP, DEF_FOOD_DECAYER);
        foodDecayer = (Decayer)obj;
        
        obj = loadClass(FOOD_OFLOW_DECAYER_PROP, DEF_FOOD_OFLOW_DECAYER);
        foodDecayerOFlow = (Decayer)obj;
        
        maxShelfCnt = getNumProp(MAX_SHELF_CNT_PROP, DEF_MAX_SHELF_CNT);
        shelves = new HashMap<>();
        shelves.put(TrackedOrder.TempType.HOT, new FoodShelf(maxShelfCnt, TrackedOrder.TempType.HOT));
        shelves.put(TrackedOrder.TempType.COLD, new FoodShelf(maxShelfCnt, TrackedOrder.TempType.COLD));
        shelves.put(TrackedOrder.TempType.FROZEN, new FoodShelf(maxShelfCnt, TrackedOrder.TempType.FROZEN));
        
        maxOfShelfCnt = getNumProp(MAX_OF_SHELF_CNT_PROP, DEF_MAX_OF_SHELF_CNT);
        overFlowShelf = new FoodShelf(maxOfShelfCnt, TrackedOrder.TempType.AGNOSTIC);
    
        // delay rate comparator
        decayRateCompOF = (TrackedOrder to1, TrackedOrder to2) -> 
        {
            long curTime = System.currentTimeMillis() / 1000;
            long decay1 = foodDecayer.decay(to1.getFoodOrder().getShelfLife(), curTime, to1.getOrderTimeSecs(), to1.getFoodOrder().getDecayRate());
            long decay2 = foodDecayer.decay(to2.getFoodOrder().getShelfLife(), curTime, to2.getOrderTimeSecs(), to2.getFoodOrder().getDecayRate());
            return (int)(decay1 - decay2);
        };
        
        // compares times for the driver min-heap
        Comparator<TrackedOrder> comp = (TrackedOrder to1, TrackedOrder to2) -> 
            (int)(to1.getPickupTimeSecs() - to2.getPickupTimeSecs());
        driverQueue = new PriorityQueue<>(comp);
        ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(this, 1000, 1000, TimeUnit.MILLISECONDS);
    }
    
    public void Shutdown() {
        ses.shutdown();
    }

    public RetCode PlaceOrder(FoodOrder forder) {
        // check for valid temp
        TrackedOrder.TempType tt;
        try {
            tt = TrackedOrder.TempType.xlate(forder.getTemp());
        } catch (TrackedOrder.InvalidTempException exc) {
            return RetCode.BAD_REQ;
        }
        
        // synchronize on overFlowShelf before the embedded temperature shelf
        // since that is the same order it is done in the run method - avoid deadlock
        FoodShelf shelf = null;
        synchronized(overFlowShelf) {
	        shelf = shelves.get(tt);
	        synchronized(shelf) {
		        if (shelf.getOrderCnt() == shelf.getMaxCnt()) {
		            if (overFlowShelf.getOrderCnt() == overFlowShelf.getMaxCnt()) {
		                LOG.error("PlaceOrder: Cannot place order: All shelves full");
		                return RetCode.UNAVAIL;
		            }
		            shelf = overFlowShelf;
		        }
	        }
        }
        
        int id = idGenerator.generate();
        TrackedOrder to = new TrackedOrder(id, tt);
        to.setFoodOrder(forder);
        long orderTime = System.currentTimeMillis() / 1000;
        to.setOrderTimeSecs(orderTime);
        try {
        	synchronized(shelf) {
                shelf.addOrder(to);
        	}
        } catch (FoodShelf.ShelfFullException|TrackedOrder.InvalidTempException exc) {
        	LOG.error("PlaceOrder: Failed to place order: " + exc);
        	return RetCode.INT_ERR;
        }
        LOG.info("PlaceOrder: placed order on shelf: order=" + to);
        
        displayShelves();
        
        scheduleDriver(to);
 
        return RetCode.CREATED;
    }
 
    void displayShelves() {
        // display contents of each shelf
        LOG.info("DISPLAY Shelves:");
        long curTimeSecs = System.currentTimeMillis() / 1000;
        for (FoodShelf fs: shelves.values()) {
            LOG.info("DISPLAY Shelf " + fs.getTemp() + ":");
            synchronized(fs) {
                for (TrackedOrder to: fs.getOrders().values()) {
                    long norm = foodDecayer.normalized(to.getFoodOrder().getShelfLife(), curTimeSecs, to.getOrderTimeSecs(), to.getFoodOrder().getDecayRate());
                    LOG.info("DISPLAY-ORDER: " + to.toString() + "[normalized-decay=" + norm + "]");
                }
            }
        }
        LOG.info("DISPLAY Shelf Over-Flow:");
        synchronized(overFlowShelf) {
            for (TrackedOrder to: overFlowShelf.getOrders().values()) {
                long norm = foodDecayerOFlow.normalized(to.getFoodOrder().getShelfLife(), curTimeSecs, to.getOrderTimeSecs(), to.getFoodOrder().getDecayRate());
                LOG.info("DISPLAY-ORDER: " + to.toString() + "[normalized-decay=" + norm + "]");
            }
        }
        LOG.info("DISPLAY Shelves complete");
    }
    
    void scheduleDriver(TrackedOrder to) {
        // add job to schedule heap
        // calc a random value between 2 and 10 - add it to the order-time and set pickup-time
        // then add to to heap
        long secs = ThreadLocalRandom.current().nextLong(2, 11);
        to.setPickupTimeSecs(secs + to.getOrderTimeSecs());
        synchronized(driverQueue) {
            driverQueue.add(to);
        }
    }
    
    @Override
    public void run() {
        /*
          Start pulling off orders from the driverQueue if they have arrived
          Process each order left on the shelves for decay
          Transfer any over flow orders to temperature shelves if available
        */
        long curTimeSecs = System.currentTimeMillis() / 1000;
        boolean checkDrivers = true;
        boolean removedOrders = false;
        synchronized(driverQueue) {
            while (checkDrivers && driverQueue.size() > 0) {
                TrackedOrder to = driverQueue.peek();
                if (to.getPickupTimeSecs() <= curTimeSecs) {
                    to = driverQueue.poll();
                    // remove it from its shelf
                    FoodShelf shelf = shelves.get(to.getTemp());
                    TrackedOrder to2 = null;
                    synchronized(shelf) {
                        to2 = shelf.removeOrder(to.getOrderID());
                    }
                    boolean tempShelfRemoval = true;
                    if (to2 == null) {
                    	tempShelfRemoval = false;
                        synchronized(overFlowShelf) {
                            to2 = overFlowShelf.removeOrder(to.getOrderID());
                        }
                    }
                    if (to2 != null) {
                        removedOrders = true;
                        if (tempShelfRemoval) {
                            LOG.info("DQ.task: Remove delivered order from " + to.getTemp() + " shelf: order=" + to);
                        } else {
                        	LOG.info("DQ.task: Remove delivered order from Over-flow shelf: order=" + to);
                        }
                    }
                } else {
                    checkDrivers = false;
                }
            }
        }

        // process the over flow shelf for decay
        // collect orders according to temperature for possible move to temp shelves
        PriorityQueue<TrackedOrder> ofHot = new PriorityQueue<>(decayRateCompOF);
        PriorityQueue<TrackedOrder> ofCold = new PriorityQueue<>(decayRateCompOF);
        PriorityQueue<TrackedOrder> ofFrzn = new PriorityQueue<>(decayRateCompOF);
        synchronized(overFlowShelf) {
            for (TrackedOrder to: overFlowShelf.getOrders().values()) {
                long curTime = System.currentTimeMillis() / 1000;
                long decay = foodDecayerOFlow.decay(to.getFoodOrder().getShelfLife(), curTime, to.getOrderTimeSecs(), to.getFoodOrder().getDecayRate());
                if (decay <= 0) {
                    // remove the order
                    overFlowShelf.removeOrder(to.getOrderID());
                    removedOrders = true;
                    LOG.info("DQ.task: Remove decayed order from Overflow shelf: order=" + to);
                } else {
                    if (to.getTemp() == TrackedOrder.TempType.HOT) {
                        ofHot.add(to);
                    } else if (to.getTemp() == TrackedOrder.TempType.COLD) {
                        ofCold.add(to);
                    } else {
                        ofFrzn.add(to);
                    }
                }
            }
        }

        // process the temperature shelves for decay
        for (FoodShelf fs: shelves.values()) {
            synchronized(fs) {
                for (TrackedOrder to: fs.getOrders().values()) {
                    long curTime = System.currentTimeMillis() / 1000;
                    long decay = foodDecayer.decay(to.getFoodOrder().getShelfLife(), curTime, to.getOrderTimeSecs(), to.getFoodOrder().getDecayRate());
                    if (decay <= 0) {
                        // remove the order
                        fs.removeOrder(to.getOrderID());
                        removedOrders = true;
                        LOG.info("DQ.task: Remove decayed order from " + to.getTemp() + " shelf: order=" + to);
                    }
                }
            }

            // check if there is space on the temperature shelf and if there are overflow
            // items of the same temperature type that can be moved
            PriorityQueue<TrackedOrder> pq = null;
            synchronized(overFlowShelf) {
                if (!overFlowShelf.getOrders().isEmpty()) {
                    synchronized(fs) {
                        int available = fs.getMaxCnt() - fs.getOrders().size();
                        if (available > 0) {
                            // remove oldest orders of the temp type to fill available slots
                            if (fs.getTemp() == TrackedOrder.TempType.HOT) {
                                pq = ofHot;
                            } else if (fs.getTemp() == TrackedOrder.TempType.COLD) {
                                pq = ofCold;
                            } else {
                                pq = ofFrzn;
                            }
                            for (int cnt = available; cnt > 0 && !pq.isEmpty(); cnt--) {
                                TrackedOrder to = pq.poll();
                                if (to != null) {
                                	try {
	                                    fs.addOrder(to);
	                                    overFlowShelf.removeOrder(to.getOrderID());
	                                    removedOrders = true;
	                                    LOG.info("DQ.task: Moved order from Overflow shelf to " + fs.getTemp() + " shelf: order=" + to);
                                	} catch (FoodShelf.ShelfFullException|TrackedOrder.InvalidTempException exc) {
                                		LOG.error("DQ.task: Failed to move order from Overflow shelf to temp shelf: Lost order=" + to + " : " + exc);
                                	}
                                }
                            }
                        }
                    }
                }
            }
        }
        if (removedOrders) {
            displayShelves();
        }
    }

    Object loadClass(String propName, String defaultClass) {

        String className = System.getProperty(propName, defaultClass);
        Object obj;
        try {
            obj = Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid class specified=").append(className).
                append(" : for property=").append(propName).
                append(" : error=").append(e.getMessage());
            LOG.error(sb.toString());
            throw new java.util.MissingResourceException(sb.toString(), className, propName);
        }

        return obj;
    }
    
    int getNumProp(String propName, int defaultVal) {
        String numStr = System.getProperty(propName);
        if (numStr != null) {
            // convert string to a number
            return Integer.parseInt(numStr);
        }
        return defaultVal;
    }

}

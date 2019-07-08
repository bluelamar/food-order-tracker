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
// FIX import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bluelamar.fotracker.IdGenerator;
import org.bluelamar.fotracker.Decayer;
import org.bluelamar.fotracker.model.FoodOrder;
import org.bluelamar.fotracker.model.FoodOrders;
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
    PriorityQueue<TrackedOrder> driverQueue;
    ScheduledExecutorService ses;
    Comparator<TrackedOrder> compOF; // comparator for the overflow shelf

	public FoodOrderService() {
	}

	public void Init() {
		Object obj = loadClass(ID_GENERATOR_PROP, DEF_ID_GENERATOR);
        idGenerator = (IdGenerator)obj;
        
        obj = loadClass(FOOD_DECAYER_PROP, DEF_FOOD_DECAYER);
        foodDecayer = (FoodDecayer)obj;
        
        maxShelfCnt = getNumProp(MAX_SHELF_CNT_PROP, DEF_MAX_SHELF_CNT);
        shelves = new HashMap<>();
        shelves.put(TrackedOrder.TempType.HOT, new FoodShelf(maxShelfCnt, TrackedOrder.TempType.HOT));
        shelves.put(TrackedOrder.TempType.COLD, new FoodShelf(maxShelfCnt, TrackedOrder.TempType.COLD));
        shelves.put(TrackedOrder.TempType.FROZEN, new FoodShelf(maxShelfCnt, TrackedOrder.TempType.FROZEN));
        
        maxOfShelfCnt = getNumProp(MAX_OF_SHELF_CNT_PROP, DEF_MAX_OF_SHELF_CNT);
        overFlowShelf = new FoodShelf(maxOfShelfCnt, TrackedOrder.TempType.AGNOSTIC);
	
        // delay rate comp
        compOF = (TrackedOrder to1, TrackedOrder to2) -> 
		{
			long curTime = System.currentTimeMillis() / 1000;
			long orderAge = curTime - to1.getOrderTimeSecs();
			long decay1 = foodDecayer.decay(to1.getFoodOrder().getShelfLife(), orderAge, to1.getFoodOrder().getDecayRate());
			orderAge = curTime - to2.getOrderTimeSecs();
			long decay2 = foodDecayer.decay(to2.getFoodOrder().getShelfLife(), orderAge, to2.getFoodOrder().getDecayRate());
			return (int)(decay1 - decay2);
		};
		
        Comparator<TrackedOrder> comp = (TrackedOrder to1, TrackedOrder to2) -> 
			(int)(to1.getPickupTimeSecs() - to2.getPickupTimeSecs());
        driverQueue = new PriorityQueue<>(comp);
        /* FIX
        Runnable task1 = new Runnable() {
        	final PriorityQueue<TrackedOrder> dq = driverQueue;
            // start pulling off orders from the driverQueue if they have arrrived
        	// process each order left on the shelves for decay
        	// transfer any over flow orders to temp shelves if can
        	public void run() {
        		long curTimeSecs = System.currentTimeMillis() / 1000;
        		boolean checkDrivers = true;
        		synchronized(dq) {
	        		while (checkDrivers && dq.size() > 0) {
	        			TrackedOrder to = dq.peek();
	        			if (to.getPickupTimeSecs() <= curTimeSecs) {
	        				to = dq.poll();
	        				LOG.info("DQ.task: process: " + to);
	        				// remove it from its shelf
	        				FoodShelf shelf = shelves.get(to.getTemp());
	        				TrackedOrder to2 = null;
	        				synchronized(shelf) {
	        					to2 = shelf.removeOrder(to.getOrderID());
	        				}
	        				if (to2 == null) {
	        					synchronized(overFlowShelf) {
	        						// check overflow shelf
	        						to2 = overFlowShelf.removeOrder(to.getOrderID());
	        					}
	        				}
	        				
	        			} else {
	        				checkDrivers = false;
	        			}
	        		}
        		}
        		// FIX process shelves for decay
        	}
        }; */
        ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(this, 1000, 1500, TimeUnit.MILLISECONDS);
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
    	
    	FoodShelf shelf = shelves.get(tt);
    	if (shelf.getOrderCnt() == shelf.getMaxCnt()) {
    		if (overFlowShelf.getOrderCnt() == overFlowShelf.getMaxCnt()) {
    			LOG.info("CFO:PlaceOrder: Cannot place order: Full shelves");
    			return RetCode.UNAVAIL;
    		}
    		shelf = overFlowShelf;
    	}
    	
    	int id = idGenerator.generate();
    	TrackedOrder to = new TrackedOrder(id, tt);
    	to.setFoodOrder(forder);
    	long orderTime = System.currentTimeMillis() / 1000;
    	to.setOrderTimeSecs(orderTime);
    	shelf.addOrder(to);
    	
    	displayShelves();
    	
    	scheduleDriver(to);
 
    	LOG.info("CFO:PlaceOrder: placed order on shelf: order=" + to);
		return RetCode.CREATED;
	}
	
	void displayShelves() {
		// display contents of each shelf
		LOG.info("Display Shelves:");
		for (FoodShelf fs: shelves.values()) {
			LOG.info("Display Shelf " + fs.getTemp() + ":");
			synchronized(fs) {
				for (TrackedOrder to: fs.getOrders().values()) {
					LOG.info("ORDER: " + to.toString());
				}
			}
		}
		LOG.info("Display Shelf Over-Flow:");
		synchronized(overFlowShelf) {
			for (TrackedOrder to: overFlowShelf.getOrders().values()) {
				LOG.info("ORDER: " + to.toString());
			}
		}
		LOG.info("Display Shelves complete");
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
		 start pulling off orders from the driverQueue if they have arrived
		 process each order left on the shelves for decay
		 transfer any over flow orders to temp shelves if available
		*/
		long curTimeSecs = System.currentTimeMillis() / 1000;
		//LOG.info("DQ.task: check orders against current time=" + curTimeSecs);
		boolean checkDrivers = true;
		boolean removedOrders = false;
		synchronized(driverQueue) {
    		while (checkDrivers && driverQueue.size() > 0) {
    			TrackedOrder to = driverQueue.peek();
    			if (to.getPickupTimeSecs() <= curTimeSecs) {
    				to = driverQueue.poll();
    				LOG.info("DQ.task: process: " + to);
    				// remove it from its shelf
    				FoodShelf shelf = shelves.get(to.getTemp());
    				TrackedOrder to2 = null;
    				synchronized(shelf) {
    					to2 = shelf.removeOrder(to.getOrderID());
    				}
    				if (to2 == null) {
    					synchronized(overFlowShelf) {
    						to2 = overFlowShelf.removeOrder(to.getOrderID());
    					}
    				}
    				if (to2 != null) {
    					removedOrders = true;
    				}
    			} else {
    				checkDrivers = false;
    			}
    		}
		}
		// process shelves for decay
		// seperate the temp types for removal to temp shelves if available space
		PriorityQueue<TrackedOrder> ofHot = new PriorityQueue<>(compOF);
		PriorityQueue<TrackedOrder> ofCold = new PriorityQueue<>(compOF);
		PriorityQueue<TrackedOrder> ofFrzn = new PriorityQueue<>(compOF);
		synchronized(overFlowShelf) {
			for (TrackedOrder to: overFlowShelf.getOrders().values()) {
				LOG.info("TRACKED-ORDER:OF: " + to.toString());
				long orderAge = (System.currentTimeMillis() / 1000) - to.getOrderTimeSecs();
				long decay = foodDecayer.decay(to.getFoodOrder().getShelfLife(), orderAge, to.getFoodOrder().getDecayRate());
				if (decay <= 0) {
					// remove the order
					overFlowShelf.removeOrder(to.getOrderID());
					removedOrders = true;
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
		for (FoodShelf fs: shelves.values()) {
			synchronized(fs) {
				for (TrackedOrder to: fs.getOrders().values()) {
					LOG.info("TRACKED-ORDER: " + to.toString());
					long orderAge = (System.currentTimeMillis() / 1000) - to.getOrderTimeSecs();
					long decay = foodDecayer.decay(to.getFoodOrder().getShelfLife(), orderAge, to.getFoodOrder().getDecayRate());
					if (decay <= 0) {
						// remove the order
						fs.removeOrder(to.getOrderID());
						removedOrders = true;
					}
				}
			}
			// check if there is space on this shelf and if there are overflow items
			PriorityQueue<TrackedOrder> pq = null;
			synchronized(overFlowShelf) {
				if (overFlowShelf.getOrders().size() > 0) {
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
									fs.addOrder(to);
									// FIX overFlowShelf.getOrders().remove(to);
									overFlowShelf.removeOrder(to.getOrderID());
									removedOrders = true;
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
    		// FIX Constructor.newInstance()
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
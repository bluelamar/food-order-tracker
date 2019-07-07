package org.bluelamar.fotracker.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.bluelamar.fotracker.IdGenerator;

public class OrderIdGenerator implements IdGenerator {
	AtomicInteger generator = new AtomicInteger();

	@Override
	public int generate() {
		return generator.incrementAndGet();
	}
}


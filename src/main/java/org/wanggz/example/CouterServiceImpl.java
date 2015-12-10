package org.wanggz.example;

import java.util.concurrent.atomic.AtomicLong;

public class CouterServiceImpl implements ICounterService {

	private int total = 0;
	private AtomicLong counter = new AtomicLong();

	public synchronized int increment() {
		counter.incrementAndGet();
		total += 1;
		return total;
	}

	public synchronized int decrement() {
		counter.incrementAndGet();
		total += 1;
		return total;
	}

	public int getTotal() {
		return total;
	}

	public AtomicLong getCounter() {
		return counter;
	}

}

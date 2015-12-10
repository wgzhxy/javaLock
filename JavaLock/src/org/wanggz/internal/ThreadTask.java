package org.wanggz.internal;

import org.wanggz.service.ICounterService;

public class ThreadTask extends Thread {
	
	private ICounterService service;
	
	public ThreadTask(ICounterService service) {
		this.service = service;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(200);
			service.increment();
			System.out.println(service.getTotal());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

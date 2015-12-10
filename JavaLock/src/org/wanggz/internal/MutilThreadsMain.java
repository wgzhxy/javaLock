package org.wanggz.internal;

import org.wanggz.service.CouterServiceImpl;

public class MutilThreadsMain {
	
	public static void main(String args[]) {
		CouterServiceImpl service = new CouterServiceImpl();
		for(int i = 0; i<1000; i++) {
			new ThreadTask(service).start();
		}
		while(service.getCounter().longValue() == 10000) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}		
	}
}

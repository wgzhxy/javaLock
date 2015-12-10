package org.wanggz.internal;

public class ThreadTestObjectLock extends Thread {

	private int threadNo;

	public ThreadTestObjectLock(int threadNo) {
		this.threadNo = threadNo;
	}

	public static void main(String[] args) throws Exception {
		for (int i = 1; i < 10; i++) {
			new ThreadTestObjectLock(i).start();
			Thread.sleep(1);
		}
	}

	@Override
	public synchronized void run() {
		for (int i = 1; i < 10000; i++) {
			System.out.println("No." + threadNo + ":" + i);
		}
	}
}
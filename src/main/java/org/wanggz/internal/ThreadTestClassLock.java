package org.wanggz.internal;

public class ThreadTestClassLock extends Thread {

	private int threadNo;
	private String lock;

	public ThreadTestClassLock(int threadNo, String lock) {
		this.threadNo = threadNo;
		this.lock = lock;
	}

	public static void main(String[] args) throws Exception {
		String lock = new String("lock");
		for (int i = 1; i < 10; i++) {
			new ThreadTestClassLock(i, lock).start();
			Thread.sleep(1);
		}
	}

	public void run() {
		synchronized (lock) {
			for (int i = 1; i < 10000; i++) {
				System.out.println("No." + threadNo + ":" + i);
			}
		}
	}
}
package com.anxin.replile.comms.bean;

public class ReadWriteLock {

	private int reader=0;//执行读线程数量
	private int writer=0;//执行读线程数量
	private int waitingWriter=0;//等待写的线程数量
	private boolean preferWriter=true;//写入优先
	
	public synchronized void readLock() throws InterruptedException{
		while(writer>0 || preferWriter && waitingWriter>0){
			wait();
		}
		reader++;
	}
	
	public synchronized void readUnlock(){
		reader--;
		preferWriter=true;
		notifyAll();
	}
	
	public synchronized void writeLock() throws InterruptedException{
		waitingWriter++;
		try{
			while(writer>0 || reader>0){
				wait();
			}
		}finally{
			waitingWriter--;
		}
			writer++;
	}
	
	public synchronized void writeUnlock(){
		writer--;
		preferWriter=false;
		notifyAll();
	}
}

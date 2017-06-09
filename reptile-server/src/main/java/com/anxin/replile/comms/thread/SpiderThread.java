package com.anxin.replile.comms.thread;

import java.util.ArrayList;
import java.util.List;

public class SpiderThread extends Thread {

	private TaskQueue taskQueue;
	private List<String> lists;//保存爬取用户名
	
	public SpiderThread(TaskQueue taskqueue){
		this.taskQueue=taskqueue;
		this.lists=new ArrayList<String>();
	}

	@Override
	public void run() {
		while(true){
			String username=taskQueue.getTaskbuffer().peek();
			//爬虫
			taskQueue.getTaskbuffer().addAll(lists);
		}
	}
	
	
}

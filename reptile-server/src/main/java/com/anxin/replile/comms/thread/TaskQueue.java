package com.anxin.replile.comms.thread;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskQueue {

	//private int maxQueue;//任务队里长度
	private Queue<String> taskbuffer;//任务队列
	//private int taskcount;//当前任务数量
	
	public TaskQueue(){
		this.taskbuffer=new LinkedBlockingQueue<String>();
	}

	public Queue<String> getTaskbuffer() {
		return taskbuffer;
	}

	public void setTaskbuffer(Queue<String> taskbuffer) {
		this.taskbuffer = taskbuffer;
	}
}

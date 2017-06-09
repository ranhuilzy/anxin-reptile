package com.anxin.replile.comms.utils;

import com.anxin.replile.comms.bean.ReadWriteLock;

import java.util.HashSet;

public class CheckUrlUtil {

	private HashSet<String> checkedurl;//存储已经爬取得url
	ReadWriteLock lock=new ReadWriteLock();
	
	public CheckUrlUtil(){
		checkedurl=new HashSet<String>();
	}
	
	
	//查询url是否已读
	public boolean check(String name) throws InterruptedException{
		lock.readLock();
		try{
			return checkedurl.contains(name);
		}finally{
			lock.readUnlock();
		}
	}
	
	//存取已读的URL
	public void save(String name) throws InterruptedException{
		lock.writeLock();
		try{
			checkedurl.add(name);
		}finally{
			lock.writeUnlock();
		}
	}
}

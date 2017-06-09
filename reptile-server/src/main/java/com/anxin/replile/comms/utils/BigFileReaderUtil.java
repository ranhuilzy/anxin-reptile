/**
 * 
 */
package com.anxin.replile.comms.utils;

/**
 * @author hui.ran
 *
 */

import com.anxin.replile.interfaces.ICallBackHandle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class BigFileReaderUtil {
	private Logger logger = LoggerFactory.getLogger(getClass());
	private int threadSize;
	private String charset;
	private int bufferSize;
	private ICallBackHandle callBack;
	private ExecutorService executorService;
	private long fileLength;
	private BufferedRandomAccessFile rAccessFile;
	private Set<StartEndPair> startEndPairs;
	private CyclicBarrier cyclicBarrier;
	private AtomicLong counter = new AtomicLong(0);
	private AtomicLong sucess = new AtomicLong(0);

	private BigFileReaderUtil(File file,ICallBackHandle callBack, String charset, int bufferSize, int threadSize) {
		this.fileLength = file.length();
		this.callBack = callBack;
		this.charset = charset;
		this.bufferSize = bufferSize;
		this.threadSize = threadSize;
		try {
			this.rAccessFile = new BufferedRandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			logger.error("初始文件错误,FileNotFoundException>>{}", e.getMessage());
		} catch (IOException e) {
			logger.error("初始文件错误,IOException>>{}", e.getMessage());
		}
		this.executorService = Executors.newFixedThreadPool(threadSize);
		startEndPairs = new HashSet<StartEndPair>();
	}

	public void start() {
		long everySize = this.fileLength / this.threadSize;
		try {
			calculateStartEnd(0, everySize);
		} catch (IOException e) {
			logger.error("读取文件错误,IOException>>{}", e.getMessage());
			return;
		}
		final long startTime = System.currentTimeMillis();
		cyclicBarrier = new CyclicBarrier(startEndPairs.size(), new Runnable() {
			@Override
			public void run() {
				logger.info("记录条数:{},成功记录条数[{}],执行时间:{}", counter.get(),sucess.get(),(System.currentTimeMillis() - startTime));
				boolean flag=false;
				if(counter.get()==sucess.get()){
					flag=true;
				}
				callBack.callBack(flag);
				shutdown();
			}
		});
		for (BigFileReaderUtil.StartEndPair pair : startEndPairs) {
			logger.info("分配分片:{}", pair);
			this.executorService.execute(new BigFileReaderUtil.SliceReaderTask(pair,charset,callBack));
		}
	}

	private void calculateStartEnd(long start, long size) throws IOException {
		if (start > fileLength - 1) {
			return;
		}
		BigFileReaderUtil.StartEndPair pair = new BigFileReaderUtil.StartEndPair();
		pair.start = start;
		long endPosition = start + size - 1;
		if (endPosition >= fileLength - 1) {
			pair.end = fileLength - 1;
			startEndPairs.add(pair);
			return;
		}
		rAccessFile.seek(endPosition);
		byte tmp = (byte) rAccessFile.read();
		while (tmp != '\n' && tmp != '\r') {
			endPosition++;
			if (endPosition >= fileLength - 1) {
				endPosition = fileLength - 1;
				break;
			}
			rAccessFile.seek(endPosition);
			tmp = (byte) rAccessFile.read();
		}
		pair.end = endPosition;
		startEndPairs.add(pair);
		calculateStartEnd(endPosition + 1, size);
	}

	public void shutdown() {
		try {
			this.rAccessFile.close();
		} catch (IOException e) {
			logger.error("关闭文件错误,IOException>>{}", e.getMessage(), e);
		}
		this.executorService.shutdown();
	}



	private static class StartEndPair {
		public long start;
		public long end;
		@Override
		public String toString() {
			return "star=" + start + ";end=" + end;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (end ^ (end >>> 32));
			result = prime * result + (int) (start ^ (start >>> 32));
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BigFileReaderUtil.StartEndPair other = (BigFileReaderUtil.StartEndPair) obj;
			if (end != other.end)
				return false;
			if (start != other.start)
				return false;
			return true;
		}

	}

	private class SliceReaderTask implements Runnable {
		private ICallBackHandle handle;
		private long start;
		private long sliceSize;
		private byte[] readBuff;
		private String charset;
		List<Object> dataList=null;
		/**
		 * @param pair
		 *            read position (include)
		 * @param charset
		 *            the position read to(include)
		 * @param handle
		 *            the position read to(include)
		 */
		public SliceReaderTask(BigFileReaderUtil.StartEndPair pair, String charset, ICallBackHandle handle) {
			this.start = pair.start;
			this.sliceSize = pair.end - pair.start + 1;
			this.readBuff = new byte[bufferSize];
			this.charset=charset;
			this.handle=handle;
			dataList = new ArrayList<>();
		}

		@Override
		public void run() {
			try {
				MappedByteBuffer mapBuffer = rAccessFile.getChannel().map(MapMode.READ_ONLY, start, this.sliceSize);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				for (int offset = 0; offset < sliceSize; offset += bufferSize) {
					int readLength=0;
					if (offset + bufferSize <= sliceSize) {
						readLength = bufferSize;
					} else {
						readLength = (int) (sliceSize - offset);
					}
					mapBuffer.get(readBuff, 0, readLength);
					for (int i = 0; i < readLength; i++) {
						byte tmp = readBuff[i];
						if (tmp == '\n' || tmp == '\r') {
							handle(bos.toByteArray());
							bos.reset();
						} else {
							bos.write(tmp);
						}
					}
				}
				if (bos.size() > 0) {
					handle(bos.toByteArray());
				}
				sucess.getAndAdd(this.handle.doResult(dataList));
				dataList.clear();
				cyclicBarrier.await();// 测试性能用
			} catch (Exception e) {
				logger.error("SliceReaderTask执行Run失败,Exception>>{}", e.getMessage(), e);
			}
		}
		private void handle(byte[] bytes) throws UnsupportedEncodingException {
			String line = null;
			if (this.charset == null) {
				line = new String(bytes);
			} else {
				line = new String(bytes, charset);
			}
			if (StringUtils.isNotBlank(line)) {
				dataList.add(this.handle.dataConver(line));
				counter.incrementAndGet();
			}
		}
	}

	public static class Builder<T> {
		private int threadSize = 1;
		private String charset = null;
		private int bufferSize = 1024 * 1024;
		private ICallBackHandle callBack;
		private File file;

		public Builder(String file,ICallBackHandle callBack) {
			this.file = new File(file);
			if (!this.file.exists())
				throw new IllegalArgumentException("文件不存在！");
			this.callBack=callBack;
		}

		public BigFileReaderUtil.Builder withTreahdSize(int size) {
			this.threadSize = size;
			return this;
		}

		public BigFileReaderUtil.Builder withCharset(String charset) {
			this.charset = charset;
			return this;
		}

		public BigFileReaderUtil.Builder withBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public BigFileReaderUtil build() {
			return new BigFileReaderUtil(this.file,this.callBack, this.charset, this.bufferSize, this.threadSize);
		}
	}


}
package com.anxin.replile.comms.utils;

import com.alibaba.fastjson.JSONObject;
import com.anxin.replile.interfaces.AbstractHttpRequest;
import com.anxin.replile.interfaces.IHttpHandle;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.comms.utils
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/5 16:20
 */
public class MultithreadHttpRequestUtil {
    private final static Logger logger = LoggerFactory.getLogger(MultithreadHttpRequestUtil.class);
    private IHttpHandle callBack;
    private AbstractHttpRequest httpRequest;
    private int threadSize;
    private int writThredSize;
    private String charset;
    private long reqNum;
    private long totalNum;
    private int startWith;
    private ExecutorService executorService;
    private CyclicBarrier cyclicBarrier;
    private AtomicLong counter = new AtomicLong(0);
    private String filPath;
    private String filName;
    private ConcurrentLinkedQueue<String> queueData;
    private MultithreadFileWriteUtil fileWriteUtil;
    private MultithreadHttpRequestUtil(long totalNum, long reqNum, int threadSize,int startWith, String charset, IHttpHandle callBack, AbstractHttpRequest httpRequest) {
        this.totalNum=totalNum;
        this.reqNum = reqNum;
        this.threadSize = threadSize;
        this.startWith=startWith;
        this.charset = charset;
        this.callBack = callBack;
        this.httpRequest=httpRequest;
        this.executorService = Executors.newFixedThreadPool(this.threadSize);
        this.queueData=new ConcurrentLinkedQueue<String>();
    }
    private void setFilName(String filName){
        this.filName=filName;
    }
    private void setFilPath(String filPath){
        this.filPath=filPath;
    }
    private void withWritThredSize(int writThredSize){
        this.writThredSize=writThredSize;
    }
    public void start() {
        this.calculateStartEnd(this.threadSize);
        final long startTime = System.nanoTime();
        cyclicBarrier = new CyclicBarrier(this.threadSize, new Runnable() {
            @Override
            public void run() {
                logger.info("执行请求条数:{},执行时间:{} 秒", counter.get(),((System.nanoTime() - startTime)/ 1000000000));
                boolean flag=false;
                if(counter.get()==totalNum){
                    flag=true;
                }
                callBack.callBack(flag);
                shutdown();
            }
        });
        this.fileWriteUtil=new MultithreadFileWriteUtil(this.writThredSize);
        this.fileWriteUtil.build(this.filPath,this.filName,queueData);
        this.fileWriteUtil.start();
    }

    private void calculateStartEnd(int threadSize){
        long end=this.reqNum;
        long start=this.startWith;
        MultithreadHttpRequestUtil.SliceHttpRequestTask httpRequestTask=null;
        for (int thread=1;thread<=threadSize;thread++){
            if(thread>1){
                start=end+1;
                end=this.reqNum+start-1;
            }
            if(end>=this.totalNum){
                end=this.totalNum;
            }
            MultithreadHttpRequestUtil.StartEndPair pair = new MultithreadHttpRequestUtil.StartEndPair(start,end,thread);
            logger.info("*******************Http请求总记录条数:{},每个线程执行分配数量:{},分片数:{}",this.totalNum, pair,threadSize);
           httpRequestTask=new MultithreadHttpRequestUtil.SliceHttpRequestTask(queueData,pair,charset,callBack,this.httpRequest);
            this.executorService.submit(httpRequestTask);
        }
    }

    public void shutdown() {
        this.executorService.shutdown();
    }

    private class SliceHttpRequestTask implements Runnable {
        private String charset;
        private AbstractHttpRequest httpRequest;
        private IHttpHandle callHandle;
        private StartEndPair pair;
        private ConcurrentLinkedQueue<String> queue;
        private List<T> dataList=null;
        /**
         * @param pair
         *            read position (include)
         * @param charset
         *            the position read to(include)
         * @param handle
         *            the position read to(include)
         */
        public SliceHttpRequestTask(ConcurrentLinkedQueue<String> queueData,StartEndPair pair, String charset,IHttpHandle callHandle,AbstractHttpRequest httpRequest) {
            this.charset=charset;
            this.callHandle=callHandle;
            this.httpRequest=httpRequest;
            this.pair=pair;
            this.queue=queueData;
            this.dataList= Collections.synchronizedList(new ArrayList<T>());
        }
        @Override
        public void run() {
            try {
                while (this.pair.start<=this.pair.end) {
                    long startTime = System.nanoTime();
                    Map<String,Object> params= new HashMap();
                    params.put("charset",this.charset);
                    params.put("page",this.pair.start);
                    this.dataList.addAll(this.callHandle.httpReqData(this.httpRequest,params));
                    this.doResult(this.dataList);
                    this.dataList.clear();
                    this.pair.start=this.pair.start+1;
                    counter.incrementAndGet();
                    long endTime = System.nanoTime();
                    logger.info("****************执行请求条数:{},处理耗时为[{}] 秒", counter.get(),(endTime-startTime)/1000000000);
                }
                cyclicBarrier.await();// 测试性能用
            } catch (InterruptedException e) {
                logger.error("SliceHttpRequestTask执行Run失败,InterruptedException>>{}", e.getMessage(), e);
            } catch (BrokenBarrierException e) {
                logger.error("SliceHttpRequestTask执行Run失败,BrokenBarrierException>>{}", e.getMessage(), e);
            }finally {
            }
        }
        private void doResult(List<T> datas){
            if(CollectionUtils.isNotEmpty(datas)){
                for (Object obj:datas) {
                    try {
                        String jsonStr = JSONObject.toJSONString(obj);
                        queue.add(jsonStr);
                        Thread.sleep(10);
                    }catch (Exception e) {
                        logger.error("***************数据转换Json Str失败,Exception>>{}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    private static class StartEndPair {
        public long start;
        public long end;
        public int threadNum;
        public StartEndPair(long start,long end,int threadNum){
            this.start=start;
            this.end=end;
            this.threadNum=threadNum;
        }
        @Override
        public String toString() {
            return "threadNum="+threadNum+";star=" + start + ";end=" + end;
        }
    }

    public static class Builder<T> {
        private int threadSize = 1;
        private int startWith=1;
        private IHttpHandle callBack;
        private AbstractHttpRequest httpRequest;
        private String charset;
        private long reqNum;
        private long totalNum;
        private String filePath;
        private String fileName;
        private int writThredSize;

        public Builder(long reqNum,long totalNum,int threadSize,IHttpHandle callBack,AbstractHttpRequest httpRequest) {
            this.reqNum=reqNum;
            this.totalNum=totalNum;
            this.threadSize=threadSize;
            this.callBack=callBack;
            this.httpRequest=httpRequest;
            this.charset="UTF-8";
        }
        public Builder(IHttpHandle callBack,AbstractHttpRequest httpRequest) {
            this.callBack=callBack;
            this.httpRequest=httpRequest;
            this.charset="UTF-8";
        }
        public MultithreadHttpRequestUtil.Builder withTotalNum(long totalNum) {
            this.totalNum = totalNum;
            return this;
        }
        public MultithreadHttpRequestUtil.Builder withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        public MultithreadHttpRequestUtil.Builder withFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }
        public MultithreadHttpRequestUtil.Builder withReqNum(long reqNum) {
            this.reqNum = reqNum;
            return this;
        }
        public MultithreadHttpRequestUtil.Builder withStartWith(int startWith) {
            this.startWith = startWith;
            return this;
        }
        public MultithreadHttpRequestUtil.Builder withThreadSize(int threadSize) {
            this.threadSize = threadSize;
            return this;
        }

        public MultithreadHttpRequestUtil.Builder withCharset(String charset) {
            this.charset = charset;
            return this;
        }
        public MultithreadHttpRequestUtil.Builder withWritThredSize(int writThredSize) {
            this.writThredSize = writThredSize;
            return this;
        }

        public MultithreadHttpRequestUtil build() {
            MultithreadHttpRequestUtil httpRequestUtil=new MultithreadHttpRequestUtil(this.totalNum,this.reqNum,this.threadSize,this.startWith,this.charset,this.callBack,this.httpRequest);
            httpRequestUtil.setFilName(this.fileName);
            httpRequestUtil.setFilPath(this.filePath);
            httpRequestUtil.withWritThredSize(this.writThredSize);
            return httpRequestUtil;
        }
    }
}

package com.anxin.replile.comms.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.comms.utils
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/7 11:24
 */
public class MultithreadFileWriteUtil {
    private final static Logger logger = LoggerFactory.getLogger(MultithreadFileWriteUtil.class);
    public ExecutorService executorService=null;
    private Set<BufferedRandomAccessFile> bufferedWriters;
    private int threadSize;
    public CyclicBarrier cyclicBarrier =null;
    List<FileWriter> list = Collections.synchronizedList(new ArrayList<FileWriter>());
    public MultithreadFileWriteUtil(int threadSize){
        this.threadSize=threadSize;
    }

    public void build(String filePath,String fileNam,ConcurrentLinkedQueue<String> dataQueue){
        FileWriter fileWriter=null;
        executorService=Executors.newFixedThreadPool(this.threadSize);
        BufferedRandomAccessFile bufferedFile=null;
        bufferedWriters=new HashSet<BufferedRandomAccessFile>();
        for(int i=1;i<=threadSize;i++){
            if(filePath.lastIndexOf(File.separator)<=-1){
                filePath=filePath+File.separator;
            }
            String file=filePath+i+"_"+fileNam;
            try {
                bufferedFile=new BufferedRandomAccessFile(file,"rw");
                bufferedWriters.add(bufferedFile);
                fileWriter=new FileWriter(bufferedFile,dataQueue);
                list.add(fileWriter);
            }catch (IOException e){
                logger.error("************************初始化文件写入对象BufferedRandomAccessFile失败!IOException>>",e.getMessage());
            }
        }
    }
    public void  stop (){
        if(CollectionUtils.isNotEmpty(list)){
            for (FileWriter fileWriter:list) {
                fileWriter.stop();
            }
        }
        if(CollectionUtils.isNotEmpty(bufferedWriters)){
            for (BufferedRandomAccessFile fileWriter:bufferedWriters) {
                try {
                    logger.info("************************开始关闭文件写入流*************************");
                    fileWriter.close();
                }catch (IOException e){
                    logger.error("************************关闭文件写入对象BufferedRandomAccessFile失败!IOException>>",e.getMessage());
                }
            }
        }
        if(null!=executorService){
            logger.info("************************开始关闭文件写入流*************************");
            executorService.shutdown();
        }
    }

    public void  start (){
        final long startTime = System.nanoTime();
        cyclicBarrier= new CyclicBarrier(this.threadSize, new Runnable() {
            @Override
            public void run() {
                logger.info("*****************写文件总的执行时间:{} 秒",((System.nanoTime() - startTime)/ 1000000000));
                stop();
            }
        });
        if(null!=executorService){
            if(CollectionUtils.isNotEmpty(list)){
                for (FileWriter fileWriter:list) {
                    executorService.submit(fileWriter);
                }
            }
        }
    }

    /**
     * 将队列中的数据写入到文件
     */
    private class FileWriter implements Runnable{
        private BufferedRandomAccessFile bufferedFile;
        private ConcurrentLinkedQueue<String> queue;
        private boolean operFlag=true;
        public FileWriter(BufferedRandomAccessFile bufferedFile,ConcurrentLinkedQueue<String> queue){
            this.bufferedFile = bufferedFile;
            this.queue = queue;
        }
        @Override
        public void run() {
            //循环监听
            while(this.operFlag){
                if(!queue.isEmpty()){
                    try {
                        byte[] byteString = queue.poll().getBytes("utf-8");
                        bufferedFile.write(byteString);
                        bufferedFile.write("\r\n".getBytes());
                        bufferedFile.flush();
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.error("数据写入文件失败 InterruptedException>>{}",e.getMessage());
                    } catch (IOException e) {
                        logger.error("数据写入文件失败",e.getMessage());
                    }
                }
            }
            try {
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                logger.error("******************数据写入文件失败 InterruptedException>>{}",e.getMessage());
            } catch (BrokenBarrierException e) {
                logger.error("******************数据写入文件失败 BrokenBarrierException>>{}",e.getMessage());
            }
        }
        public void  stop (){
            this.operFlag=false;
        }
    }

}

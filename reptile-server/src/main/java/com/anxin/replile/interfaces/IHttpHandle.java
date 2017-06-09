package com.anxin.replile.interfaces;

import java.util.List;
import java.util.Map;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.interfaces
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/5 16:19
 */
public interface IHttpHandle<T> {
    public List<T> httpReqData(AbstractHttpRequest httpRequest,Map<String,Object> params);
    //public boolean doResult(List<T> dataList,BufferedRandomAccessFile bufferFile);
    public void callBack(boolean flag);
}

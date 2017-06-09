package com.anxin.replile.interfaces;

import java.util.List;

/**
 * Created by hui.ran on 2017/3/8.
 */
public interface ICallBackHandle<T> {
    public T dataConver(String readline);

    public long doResult(List<T> dataList);

    public void callBack(boolean flag);
}

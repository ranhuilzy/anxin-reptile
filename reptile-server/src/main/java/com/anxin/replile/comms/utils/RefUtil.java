package com.anxin.replile.comms.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 * @author 余万水    wanshui.yu@msxf.com
 * @version  0.0.9.yuws
 * @Title: RefUtil
 * @Description: 单个字段转换类
 * @date 2016/10/25
 */

public class RefUtil {

    /**
     * 根据属性名获得setter方法的方法名
     * @param fieldName
     * @return
     */
    public static String getGetterMethodName(String fieldName) {
        String begin = fieldName.substring(0, 1).toUpperCase();
        String end = fieldName.substring(1, fieldName.length());
        String methodName = "get" + begin + end;
        return methodName;
    }

    /**
     *
     * @param obj
     * @param type
     * @return
     */
    public static String parseTypetoValue(Object obj,Class<?> type){
        if (type.equals(Integer.class) || type.equals(Double.class)
                || type.equals(String.class) || type.equals(Boolean.class)
                || type.equals(Long.class) || type.equals(Short.class)
                || type.equals(Byte.class) || type.equals(Float.class)
                || type.isEnum() || type.equals(BigDecimal.class)
                || type.equals(BigInteger.class)) {
            return obj == null ? "" : obj.toString();
        }else if(type.equals(Date.class)){
            return obj == null ? "" : DateFormatUtils.format((Date)obj, "yyyyMMddHHmmss");
        }
        return "";
    }

    /**
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

}


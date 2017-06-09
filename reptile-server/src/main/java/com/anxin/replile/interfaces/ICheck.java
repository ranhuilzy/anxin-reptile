package com.anxin.replile.interfaces;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 余万水    wanshui.yu@msxf.com
 * @version  0.0.9.yuws
 * @Title: ICheck
 * @Description: 检查实体属性
 * @date 2016/10/25
 */

@Documented
@Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ICheck {
    public abstract long lengths();//长度
    public abstract boolean notEmpty() default false;//是否非空
    public abstract boolean fixed() default false;//是否固定长度
    public abstract boolean isNumber() default false;//是否数字
    public abstract String regular() default "";//正则
}

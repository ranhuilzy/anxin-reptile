package com.anxin.replile.comms.bean;

import com.anxin.replile.interfaces.ICBinaryInt;
import com.anxin.replile.interfaces.ICChar;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CStruct<T> {
    private Logger logger;
    private List<Field> fields;
    private String charset;
    private Class<T> clazz;
    private int byteLength;

    public CStruct(Class<T> clazz) {
        this(clazz, "utf-8");
    }

    public CStruct(Class<T> clazz, String charset) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.fields = new ArrayList();
        this.clazz = clazz;
        Collections.addAll(this.fields, clazz.getFields());
        this.charset = charset;
        this.byteLength = this.calcByteLength();
        Collections.sort(this.fields, new Comparator<Field>() {
            @Autowired
            public int compare(Field o1, Field o2) {
                int order1 = this.getOrder(o1);
                int order2 = this.getOrder(o2);
                return order1 < order2?-1:(order1 == order2?0:1);
            }

            private int getOrder(Field field) {
                ICChar cc = (ICChar)field.getAnnotation(ICChar.class);
                if(cc != null) {
                    return cc.order();
                } else {
                    ICBinaryInt cbi = (ICBinaryInt)field.getAnnotation(ICBinaryInt.class);
                    return cbi != null?cbi.order():0;
                }
            }
        });
        logger.info("Fields:"+this.fields);
    }

    private int calcByteLength() {
        int sum = 0;
        Iterator i$ = this.fields.iterator();

        while(i$.hasNext()) {
            Field field = (Field)i$.next();
            if((field.getModifiers() & 8) <= 0) {
                ICChar annoChar = (ICChar)field.getAnnotation(ICChar.class);
                ICBinaryInt annoInt = (ICBinaryInt)field.getAnnotation(ICBinaryInt.class);
                if(annoChar != null) {
                    sum += annoChar.value();
                } else if(annoInt != null) {
                    sum += annoInt.length();
                }
            }
        }

        return sum;
    }

    public void writeByteBuffer(T source, ByteBuffer buffer) {
        assert source != null;
        try {
            Iterator e = this.fields.iterator();
            while(true) {
                while(true) {
                    while(true) {
                        Field field;
                        do {
                            if(!e.hasNext()) {
                                return;
                            }

                            field = (Field)e.next();
                        } while((field.getModifiers() & 8) > 0);

                        Class type = field.getType();
                        Object value = field.get(source);
                        ICChar annoChar = (ICChar)field.getAnnotation(ICChar.class);
                        ICBinaryInt annoInt = (ICBinaryInt)field.getAnnotation(ICBinaryInt.class);
                        if(annoChar == null) {
                            if(annoInt != null) {
                                assert value instanceof Number;

                                assert annoInt.length() >= 1 && annoInt.length() <= 8 : "二进制字段长度必须在1到8之间";

                                long var17 = ((Number)value).longValue();
                                byte[] var19 = new byte[annoInt.length()];

                                int i;
                                for(i = 0; i < var19.length; ++i) {
                                    var19[i] = (byte)((int)(var17 & 255L));
                                }

                                if(annoInt.bigEndian()) {
                                    for(i = var19.length - 1; i >= 0; --i) {
                                        buffer.put(var19[i]);
                                    }
                                } else {
                                    buffer.put(var19);
                                }
                            } else {
                                assert false : field.getName() + " 必须指定字段类型注释";
                            }
                        } else {
                            String l;
                            if(value == null) {
                                l = "";
                            } else if(StringUtils.isNotBlank(annoChar.formatPattern())) {
                                l = MessageFormat.format(annoChar.formatPattern(), new Object[]{value});
                            } else if(type.equals(Date.class)) {
                                if(StringUtils.isNotBlank(annoChar.datePattern())) {
                                    SimpleDateFormat bytes = new SimpleDateFormat(annoChar.datePattern());
                                    l = bytes.format((Date)value);
                                } else {
                                    l = value.toString();
                                    this.logger.warn("使用toString格式化Date类型字段[{}/{}](是否漏加了datePattern属性？)", this.clazz.getCanonicalName(), field.getName());
                                }
                            } else if(type.equals(String.class)) {
                                l = (String)value;
                            } else if(!Number.class.isAssignableFrom(type) && type != Integer.TYPE && type != Long.TYPE) {
                                l = value.toString();
                            } else {
                                String var14 = "";
                                if(type.equals(BigDecimal.class)) {
                                    BigDecimal bytes1 = (BigDecimal)value;
                                    bytes1 = bytes1.setScale(annoChar.precision(), annoChar.rounding());
                                    if(bytes1.signum() == -1) {
                                        var14 = "-";
                                        bytes1 = bytes1.abs();
                                    }

                                    l = MessageFormat.format("{0,number,0}", new Object[]{bytes1.unscaledValue()});
                                } else {
                                    long var16 = ((Number)value).longValue();
                                    if(var16 < 0L) {
                                        var14 = "-";
                                        var16 = -var16;
                                    }

                                    l = MessageFormat.format("{0,number,0}", new Object[]{Long.valueOf(var16)});
                                }

                                if(annoChar.zeroPadding()) {
                                    l = StringUtils.leftPad(l, annoChar.value() - var14.length(), "0");
                                }

                                l = var14 + l;
                            }

                            byte[] var15 = l.getBytes(this.charset);
                            if(var15.length > annoChar.value()) {
                                buffer.put(var15, 0, annoChar.value());
                            } else {
                                int var18;
                                if(!annoChar.leftPadding()) {
                                    buffer.put(var15);

                                    for(var18 = 0; var18 < annoChar.value() - var15.length; ++var18) {
                                        buffer.putInt(32);
                                    }
                                } else {
                                    for(var18 = 0; var18 < annoChar.value() - var15.length; ++var18) {
                                        buffer.putInt(32);
                                    }

                                    buffer.put(var15);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception var13) {
            this.logger.error("结构体解析出错:" + this.clazz.getCanonicalName(), var13);
            throw new IllegalArgumentException(var13);
        }
    }

    public Object parseByteBuffer(ByteBuffer buffer) {
        try {
            Object e = this.clazz.newInstance();
            Iterator i$ = this.fields.iterator();

            while(true) {
                while(true) {
                    while(true) {
                        Field field;
                        do {
                            if(!i$.hasNext()) {
                                return e;
                            }

                            field = (Field)i$.next();
                        } while((field.getModifiers() & 8) > 0);

                        Class type = field.getType();
                        ICChar annoChar = (ICChar)field.getAnnotation(ICChar.class);
                        ICBinaryInt annoInt = (ICBinaryInt)field.getAnnotation(ICBinaryInt.class);
                        int value;
                        if(annoChar != null) {
                            value = annoChar.value();
                            byte[] bytes = new byte[value];
                            buffer.get(bytes);
                            String value1 = new String(bytes, this.charset);
                            if(StringUtils.isBlank(value1) && !annoChar.required() && !type.equals(String.class)) {
                                field.set(e, (Object)null);
                            } else if(type.equals(Date.class)) {
                                SimpleDateFormat sdf = new SimpleDateFormat(annoChar.datePattern());
                                sdf.setLenient(true);
                                String datePattern = annoChar.datePattern();
                                if(datePattern.length() < value1.length()) {
                                    if(annoChar.leftPadding()) {
                                        value1 = StringUtils.right(value1, datePattern.length());
                                    } else {
                                        value1 = StringUtils.left(value1, datePattern.length());
                                    }
                                }

                                field.set(e, sdf.parse(value1));
                            } else if(type.equals(String.class)) {
                                if(annoChar.autoTrim()) {
                                    value1 = value1.trim();
                                }

                                field.set(e, value1);
                            } else if(!type.equals(Integer.class) && !type.equals(Integer.TYPE)) {
                                if(!type.equals(Long.class) && !type.equals(Long.TYPE)) {
                                    if(type.equals(BigDecimal.class)) {
                                        field.set(e, BigDecimal.valueOf(Long.valueOf(value1).longValue(), annoChar.precision()));
                                    } else {
                                        if(!type.isEnum()) {
                                            throw new IllegalArgumentException("不支持的字段类型:" + type);
                                        }

                                        if(StringUtils.isNotBlank(value1)) {
                                            field.set(e, Enum.valueOf(type, value1.trim()));
                                        }
                                    }
                                } else {
                                    field.set(e, Long.valueOf(value1));
                                }
                            } else {
                                field.set(e, Integer.valueOf(value1.trim()));
                            }
                        } else if(annoInt != null) {
                            value = buffer.getInt();
                            field.set(e, Integer.valueOf(value));
                        } else {
                            assert false : field.getName() + " 必须指定字段类型注释";
                        }
                    }
                }
            }
        } catch (Exception var13) {
            throw new IllegalArgumentException(var13);
        }
    }

    public Object parseLine(String[] values) {
        Object obj;
        try {
            obj = this.clazz.newInstance();
        } catch (Exception var11) {
            throw new IllegalArgumentException("实例化对象出错，Class:" + this.clazz.getCanonicalName());
        }

        int i = 0;
        Iterator i$ = this.fields.iterator();
        while(i$.hasNext()) {
            Field field = (Field)i$.next();
            try {
                if((field.getModifiers() & 8) <= 0) {
                    Class e = field.getType();
                    ICChar annoChar = (ICChar)field.getAnnotation(ICChar.class);
                    if(annoChar != null) {
                        String value = values[i];
                        ++i;
                        if(StringUtils.isBlank(value) && !annoChar.required() && !e.equals(String.class)) {
                            field.set(obj, (Object)null);
                        } else if(e.equals(Date.class)) {
                            SimpleDateFormat sdf = new SimpleDateFormat(annoChar.datePattern());
                            sdf.setLenient(true);
                            String datePattern = annoChar.datePattern();
                            if(datePattern.length() < value.length()) {
                                if(annoChar.leftPadding()) {
                                    value = StringUtils.right(value, datePattern.length());
                                } else {
                                    value = StringUtils.left(value, datePattern.length());
                                }
                            }

                            field.set(obj, sdf.parse(value));
                        } else if(e.equals(String.class)) {
                            if(annoChar.autoTrim()) {
                                value = value.trim();
                            }

                            field.set(obj, value);
                        } else if(!e.equals(Integer.class) && !e.equals(Integer.TYPE)) {
                            if(!e.equals(Long.class) && !e.equals(Long.TYPE)) {
                                if(e.equals(BigDecimal.class)) {
                                    if(annoChar.pointSupported()) {
                                        field.set(obj, BigDecimal.valueOf(Double.valueOf(value).doubleValue()));
                                    } else {
                                        field.set(obj, BigDecimal.valueOf(Long.valueOf(value).longValue(), annoChar.precision()));
                                    }
                                } else {
                                    if(!e.isEnum()) {
                                        throw new IllegalArgumentException("不支持的字段类型:" + e);
                                    }

                                    if(StringUtils.isNotBlank(value)) {
                                        field.set(obj, Enum.valueOf(e, value.trim()));
                                    }
                                }
                            } else {
                                field.set(obj, Long.valueOf(value));
                            }
                        } else {
                            field.set(obj, Integer.valueOf(value.trim()));
                        }
                    } else {
                        assert false : field.getName() + " 必须指定字段类型注释";
                    }
                }
            } catch (Exception var12) {
                this.logger.error("结构体解析出错，域：{}，类型：{}。", field.getName(), field.getType().getCanonicalName());
                throw new IllegalArgumentException(var12);
            }
        }

        return obj;
    }

    public String writeLine(T source, String separator) {
        assert source != null;

        String line = "";

        try {
            Iterator e = this.fields.iterator();

            while(true) {
                Field field;
                do {
                    if(!e.hasNext()) {
                        return line;
                    }

                    field = (Field)e.next();
                } while((field.getModifiers() & 8) > 0);

                Class type = field.getType();
                Object value = field.get(source);
                ICChar annoChar = (ICChar)field.getAnnotation(ICChar.class);
                String out = "";
                if(annoChar != null) {
                    if(value == null) {
                        out = "";
                    } else if(StringUtils.isNotBlank(annoChar.formatPattern())) {
                        out = MessageFormat.format(annoChar.formatPattern(), new Object[]{value});
                    } else if(type.equals(Date.class)) {
                        if(StringUtils.isNotBlank(annoChar.datePattern())) {
                            SimpleDateFormat sdf = new SimpleDateFormat(annoChar.datePattern());
                            out = sdf.format((Date)value);
                        } else {
                            out = value.toString();
                            this.logger.warn("使用toString格式化Date类型字段[{}/{}](是否漏加了datePattern属性？)", this.clazz.getCanonicalName(), field.getName());
                        }
                    } else if(type.equals(String.class)) {
                        out = (String)value;
                    } else if(!Number.class.isAssignableFrom(type) && type != Integer.TYPE && type != Long.TYPE) {
                        out = value.toString();
                    } else {
                        out = value.toString();
                    }
                } else {
                    assert false : field.getName() + " 必须指定字段类型注释";
                }

                line = line + out + separator;
            }
        } catch (Exception var11) {
            this.logger.error("结构体解析出错:" + this.clazz.getCanonicalName(), var11);
            throw new IllegalArgumentException(var11);
        }
    }

    public String summaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("fld\tlen\tstart\n");
        int start = 0;
        Iterator i$ = this.fields.iterator();

        while(i$.hasNext()) {
            Field field = (Field)i$.next();
            ICChar c = (ICChar)field.getAnnotation(ICChar.class);
            if(c != null) {
                sb.append(field.getName());
                sb.append('\t');
                sb.append(c.value());
                sb.append('\t');
                sb.append(start);
                sb.append('\n');
                start += c.value();
            }
        }

        return sb.toString();
    }

    public int getByteLength() {
        return this.byteLength;
    }

    public String getCharset() {
        return this.charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}

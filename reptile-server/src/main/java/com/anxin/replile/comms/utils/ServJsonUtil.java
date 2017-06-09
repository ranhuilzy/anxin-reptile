package com.anxin.replile.comms.utils;

import com.anxin.replile.comms.constants.BuaConstants;
import com.anxin.replile.comms.exceptions.SerializerException;
import com.anxin.replile.interfaces.ICheck;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ServJsonUtil {

    private Logger log = LoggerFactory.getLogger(ServJsonUtil.class);

    public static final String NATIVE_PIK_INDEX = "global.pik.tps";

    // 卡号请求字段
    public static final String FIELD_CARD_NO = "card_no";
    // 翻页起
    public static final String FIELD_FIRSTROW = "firstrow";
    // 翻页止
    public static final String FIELD_LASTROW = "lastrow";
    // 操作码
    public static final String FIELD_OPT = "opt";
    //密码解锁交易，错误类型
    public static final String FIELD_ERROR_TYPE = "error_type";
    public static final String FIELD_PAGE_SIZE = "PAGESIZE"; //页大小
    public static final Integer NAME_MAX_LENGTH=80;
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final int maxRetrunRow = 100;

    /**
     *
     * @param requestnodelists 接口中的request
     * @param obj 实体
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void populateObject(Object requestnodelists, Object obj) {
        if(null == requestnodelists) {
            return ;
        }

        Throwable exception = null;
        Map<String, Object> valueMap = new HashMap<String, Object>();
        if (requestnodelists instanceof Map) {
            valueMap = (Map)requestnodelists;
        } else {
            valueMap = JsonSerializeUtil.jsonReSerializerNoType(JsonSerializeUtil.jsonSerializerNoType(requestnodelists), Map.class);
        }

        try {
            Map<String, Field> fieldMap = getFields(obj.getClass());
            Map<String, String> namedMap = getFieldNameMap(obj.getClass());
            Map<String, ICheck> checkMap = getFieldCheckMap(obj.getClass());

            // 是否存在翻页的标志
            boolean isPage = false;
            Integer firstRow = 0;
            Integer lastRow = 0;

            for (String key : namedMap.keySet()) {
                Object node = valueMap.get(key);
                String nodeNameStr = namedMap.get(key);

                //检查必输及长度
                this.check(checkMap.get(nodeNameStr), node, key);

                if (node != null) {
                    // 判断是否是枚举类型
                    PropertyDescriptor propertyDescriptor = BeanUtilsBean.getInstance().
                            getPropertyUtils().getPropertyDescriptor(obj, nodeNameStr);
                    if (propertyDescriptor == null) {
                        // propertyDescriptor 如果为空，意味这上送的报文接口在ccs的接口中不存在；抛非法的接口
                        log.info("非法的请求字段，字段名称{} 字段数值{},接口对象类型{}",nodeNameStr, node, obj.getClass().getName());
                        throw new SerializerException("字段名称{" +nodeNameStr + "}");
                    }

                    Class type = propertyDescriptor.getPropertyType();
                    log.debug("匹配报文字段，字段名称{} 字段数值{},数据类型{}", nodeNameStr, node, type.getName());
                    if (type.isEnum()) {
                        try{
                            // 使用字符串格式化当前节点，获取节点value
                            String nodeValue = node.toString();
                            if (!StringUtils.isEmpty(nodeValue)) {
                                BeanUtils.setProperty(obj, nodeNameStr, Enum.valueOf(type, nodeValue));
                            }
                        }catch(Exception e){
                            throw new SerializerException("字段名称{" +nodeNameStr + "}");
                        }
                    } else if (type.equals(Date.class)) {
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        String[] pattern = new String[] {"yyyyMMdd", "yyyyMM", "yyMM", "yyyyMMddHHmmss"};
                        if (!StringUtils.isEmpty(nodeValue)) {
                            if (nodeValue.length() != 8 && nodeValue.length() != 6
                                    && nodeValue.length() != 4 && nodeValue.length() != 14) {
                                throw new SerializerException("字段名称{" +nodeNameStr + "}");
                            }
                            try{
                                Date value = DateUtils.parseDate(nodeValue, pattern);
                                BeanUtils.setProperty(obj, nodeNameStr, value);
                            }catch(Exception e){
                                throw new SerializerException("字段名称{" +nodeNameStr + "}");
                            }
                        }
                    } else if(type.equals(BigDecimal.class)){
                        String nodeValue = node.toString();
                        String[] arry = nodeValue.split("\\.");
                        String applyPattern = "#####.00";//金额
                        if (arry.length > 1 &&arry[1].length()>2){
                            applyPattern = "#####.000000";//利率
//							throw new SerializerException(",字段名称{" +nodeNameStr + "},小数点后位数不能大于2位");
                        }

                        try{
                            DecimalFormat myformat = new DecimalFormat();
                            myformat.applyPattern(applyPattern);
                            nodeValue = myformat.format(new BigDecimal(nodeValue));

                            BeanUtils.setProperty(obj, nodeNameStr, nodeValue);
                        }catch(Exception e){
                            throw new SerializerException( "字段名称{" +nodeNameStr + "}");
                        }
                    }else if (type.equals(Integer.class) || type.equals(Long.class) || type.equals(Short.class)) {
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        if (StringUtils.isNotBlank(nodeValue)) {
                            if (RefUtil.isNumeric(nodeValue)) {
                                BeanUtils.setProperty(obj, nodeNameStr, nodeValue);
                            } else {
                                throw new SerializerException( "字段名称{" +nodeNameStr + "}");
                            }
                        }
                    } else if (type.isAssignableFrom(List.class) || type.isAssignableFrom(ArrayList.class)) {
                        List listNodes = null;
                        Map map = null;

                        if (node instanceof Map) {
                            map = (Map) node;
                            if (map.size() < 1) {
                                continue;
                            }
                            listNodes = (List) map.values().iterator().next();
                        } else {
                            listNodes = (List) node;
                        }

                        Field field = fieldMap.get(nodeNameStr); //List field
                        if(null == field || !(field.getGenericType() instanceof ParameterizedType)) {
                            throw new SerializerException("字段名称{" +nodeNameStr + "}");
                        }

                        ParameterizedType pt = (ParameterizedType)field.getGenericType();
                        if(pt.getActualTypeArguments() == null || pt.getActualTypeArguments().length < 1) {
                            throw new SerializerException("字段名称{" +nodeNameStr + "}");
                        }

                        Class<?> itemClass = Class.forName(pt.getActualTypeArguments()[0].toString().substring(6));

                        ArrayList<Object> list = new ArrayList<Object>();

                        for(int k = 0; k < listNodes.size(); k++) {
                            Object itemObj = itemClass.newInstance();
                            populateObject((Serializable)listNodes.get(k), itemObj);
                            list.add(itemObj);
                        }

                        BeanUtils.setProperty(obj, nodeNameStr, list);
                    } else if(type.equals(String.class)) {
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        if (StringUtils.isNotEmpty(nodeValue)) {
                            BeanUtils.setProperty(obj, nodeNameStr, nodeValue);
                        }
                    } else {
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        if (StringUtils.isNotEmpty(nodeValue)) {
                            BeanUtils.setProperty(obj, nodeNameStr, nodeValue);
                        }
                    }
                    // 记录firstrow
                    if (nodeNameStr.equalsIgnoreCase(FIELD_FIRSTROW)) {
                        isPage = true;
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        if (StringUtils.isEmpty(nodeValue)) {
                            throw new SerializerException(BuaConstants.ERRS008_CODE,
                                    BuaConstants.ERRS008_MES);
                        }
                        firstRow = Integer.parseInt(nodeValue);
                    }
                    // 记录lastrow
                    if (nodeNameStr.equalsIgnoreCase(FIELD_LASTROW)) {
                        isPage = true;
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        if (StringUtils.isEmpty(nodeValue)) {
                            throw new SerializerException(BuaConstants.ERRS008_CODE,
                                    BuaConstants.ERRS008_MES);
                        }
                        lastRow = Integer.parseInt(nodeValue);
                        if (lastRow < 0) {
                            throw new SerializerException(BuaConstants.ERRS011_CODE,
                                    BuaConstants.ERRS011_MES);
                        }
                    }
                    //查询清单的页大小
                    if (nodeNameStr.equalsIgnoreCase(FIELD_PAGE_SIZE)) {
                        // 使用字符串格式化当前节点，获取节点value
                        String nodeValue = node.toString();
                        if (StringUtils.isEmpty(nodeValue) || !nodeValue.trim().matches("[0-9]+")) {
                            throw new SerializerException(BuaConstants.ERRS008_CODE,
                                    BuaConstants.ERRS008_MES);
                        }
                        if (Integer.parseInt(nodeValue.trim()) > maxRetrunRow) {
                            throw new SerializerException(BuaConstants.ERRS010_CODE, BuaConstants.ERRS010_MES);
                        }
                    }

                }
            }
            // 存在翻页标志，处理翻页逻辑
            if (isPage) {
                if (firstRow > lastRow) {
                    throw new SerializerException(BuaConstants.ERRS009_CODE, BuaConstants.ERRS009_MES);
                }
                if (lastRow - firstRow + 1 > maxRetrunRow) {
                    throw new SerializerException(BuaConstants.ERRS010_CODE, BuaConstants.ERRS010_MES);
                }
            }

        } catch (SerializerException pe) {
            log.error(pe.getMessage(),pe);
            throw pe;
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new SerializerException();
        } finally {
            if(null != exception) {
                log.warn("报文格式错误>> [" + exception.getClass().getSimpleName()+"]["
                        + exception.getMessage()+"]", exception);
            }
        }

    }

    /**
     * 将大写+下划线的变量名称转换为驼峰命名
     * @param name
     * @return
     */
    private String changeNameToStandard(String name) {
        Character underlineChar = '_';
        // 空串直接返回
        if (name == null || name.trim().length() == 0) {
            return name;
        }
        // 无下划线且首字母小写的不转换
        if (name.indexOf(underlineChar) < 0 && Character.isLowerCase(name.charAt(0))) {
            return name;
        }
        // 先全部转成小写
        name = name.toLowerCase();
        Character preC = null;
        StringBuilder _name = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            Character c = name.charAt(i);
            // 前一字符是下划线，且当前字符非下划线
            if (preC == underlineChar && !c.equals('_')) {
                _name.append(Character.toUpperCase(c));
                // 当前字符非下划线
            } else if (!c.equals('_')) {
                _name.append(c);
            }
            // 跳过开头前N个下划线字符
            if (!(preC == null && c.equals('_'))) {
                preC = c;
            }
        }
        return _name.toString();
    }

    /**
     * 获取域
     * @param cls
     * @return
     */
    private  Map<String, Field> getFields(Class<?> cls) {
        Map<String, Field> fieldMap = new HashMap<String, Field>();

        Field[] fields = cls.getDeclaredFields();
        for(Field f : fields) {
            fieldMap.put(f.getName(), f);
        }

        return fieldMap;
    }

    /**
     * 获取域名map NAME:name
     * @param cls
     * @return
     */
    private Map<String, String> getFieldNameMap(Class<?> cls){
        Map<String, String> nameMap = new HashMap<String, String>();
        Field[] fields = cls.getDeclaredFields();
        for(Field f : fields) {
            JsonProperty anno = f.getAnnotation(JsonProperty.class);
            if(anno != null){
                nameMap.put(anno.value(), f.getName());
            }
        }
        return nameMap;
    }

    /**
     * 获取域名map name:Check
     * @param cls
     * @return
     */
    private Map<String, ICheck> getFieldCheckMap(Class<?> cls){
        Map<String, ICheck> nameMap = new HashMap<String, ICheck>();
        Field[] fields = cls.getDeclaredFields();
        for(Field f : fields) {
            ICheck anno = f.getAnnotation(ICheck.class);
            if(anno != null){
                nameMap.put(f.getName(), anno);
            }
        }
        return nameMap;
    }

    /**
     * 校验实体字段
     * @param check
     * @throws SerializerException
     */
    public void check(ICheck check,Object value,String fieldName) throws SerializerException{
        if(check!=null){
            if(check.notEmpty()==true){
                if(value==null||StringUtils.isBlank(value.toString())){
                    throw new SerializerException("字段名称{" +fieldName+ "},字段为空");
                }
            }
            if(value!=null&&StringUtils.isNotBlank(value.toString())){
                if(check.fixed()&&value.toString().length() != check.lengths()){
                    throw new SerializerException("字段名称{" +fieldName+ "},字段固定长度为"+check.lengths());
                }
                if(check.isNumber()&&!RefUtil.isNumeric(value.toString())){
                    throw new SerializerException("字段名称{" +fieldName + "},必须为数字");
                }
                if(value.toString().length()>check.lengths()){
                    throw new SerializerException("字段名称{" +fieldName+ "},字段超长");
                }

                if(StringUtils.isNotBlank(check.regular())){
                    if(log.isDebugEnabled())
                        log.debug("正则:[{}],检查值:[{}]",check.regular(),value.toString());
                    Pattern pattern = Pattern.compile(check.regular());
                    String valueTmp = value.toString();
                    //精度超过4，传入数据是科学计数，需要转换为数据正常字符串
                    if(value.getClass() == Double.class){
                        valueTmp = new BigDecimal(value.toString()).toPlainString();
                    }

                    Matcher matcher = pattern.matcher(valueTmp);
                    if(!matcher.matches()){
                        throw new SerializerException(",字段名称{" +fieldName+ "}");
                    }
                }
            }
        }
    }
}

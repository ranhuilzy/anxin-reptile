package com.anxin.replile.comms.utils;

import com.anxin.replile.comms.exceptions.SerializerException;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.concurrent.ConcurrentHashMap;
/**
 * JSON序列化，基于流API实现，尽可能的提高效
 *
 * @author 余万水
 */
public class JsonSerializeUtil {
    @SuppressWarnings("rawtypes")
    private static ConcurrentHashMap objWriterCache = new ConcurrentHashMap ();    // 缓存class到ObjectWriter

    // 映射关系,格式为Class - > ObjectWriter对象
    @SuppressWarnings("rawtypes")
    private static ConcurrentHashMap objWriterCacheNoType = new ConcurrentHashMap ();    // 缓存class

    // ObjectWriter的映射关,格式为Class - > ObjectWriter对象
    @SuppressWarnings("rawtypes")
    private static ConcurrentHashMap objWriterCacheNoNull = new ConcurrentHashMap ();    // 缓存class

    // ObjectWriter的映射关,格式为Class - > ObjectWriter对象
    private static ObjectMapper mapper = new ObjectMapper ();
    private static ObjectMapper mapperNoType = new ObjectMapper ();
    private static ObjectMapper mapperNoNull = new ObjectMapper ();

    public static <T> T jsonReSerializer(String jsonStr, Class <T> calzz) {
        try {
            mapper.enableDefaultTyping ( DefaultTyping.NON_FINAL );

            ObjectReader reader = mapper.reader ( calzz );
            T object = reader.readValue ( jsonStr );

            return object;
        } catch (Exception e) {
            throw new SerializerException( String.format ( "字符串[%.20s]...反序列化失败", jsonStr ), e );
        }
    }

    public static <T> T jsonReSerializerNoNull(String jsonStr, Class <T> calzz) {
        try {
            ObjectReader reader = mapperNoNull.reader ( calzz );
            T object = reader.readValue ( jsonStr );

            return object;
        } catch (Exception e) {
            throw new SerializerException ( String.format ( "字符串[%.20s]...反序列化失败", jsonStr ), e );
        }
    }

    public static <T> T jsonReSerializerNoType(String jsonStr, Class <T> calzz) {
        try {
            ObjectReader reader = mapperNoType.reader ( calzz );
            T object = reader.readValue ( jsonStr );

            return object;
        } catch (Exception e) {
            throw new SerializerException ( String.format ( "字符串[%.20s]...反序列化失败", jsonStr ), e );
        }
    }

    public static String jsonSerializer(Object originalObject) {
        try {
            mapper.enableDefaultTyping ( DefaultTyping.NON_FINAL );

            ObjectWriter objectWriter = getObjWriter ( originalObject.getClass () );
            String json = objectWriter.writeValueAsString ( originalObject );

            return json;
        } catch (Exception e) {
            throw new SerializerException ( String.format ( "对象%s序列化失败", originalObject.getClass ().getName () ), e );
        }
    }

    public static String jsonSerializerNoNull(Object originalObject) {
        try {
            ObjectWriter objectWriter = getObjWriterNoNull ( originalObject.getClass () );
            String json = objectWriter.writeValueAsString ( originalObject );

            return json;
        } catch (Exception e) {
            throw new SerializerException ( String.format ( "对象%s序列化失败", originalObject.getClass ().getName () ), e );
        }
    }

    public static String jsonSerializerNoType(Object originalObject) {
        try {
            ObjectWriter objectWriter = getObjWriterNoType ( originalObject.getClass () );
            String json = objectWriter.writeValueAsString ( originalObject );

            return json;
        } catch (Exception e) {
            throw new SerializerException ( String.format ( "对象%s序列化失败", originalObject.getClass ().getName () ), e );
        }
    }

    @SuppressWarnings("unchecked")
    private static ObjectWriter getObjWriter(Class <?> serializationView) {
        if ( objWriterCache.get ( serializationView ) != null )
            return (ObjectWriter) objWriterCache.get ( serializationView );
        else {
            ObjectWriter temp = mapper.writerWithView ( serializationView );

            objWriterCache.put ( serializationView, temp );

            return temp;
        }
    }

    @SuppressWarnings("unchecked")
    private static ObjectWriter getObjWriterNoNull(Class <?> serializationView) {
        if ( objWriterCacheNoNull.get ( serializationView ) != null ) {
            return (ObjectWriter) objWriterCacheNoNull.get ( serializationView );
        } else {
            mapperNoNull.setSerializationInclusion ( Include.NON_NULL );

            ObjectWriter temp = mapperNoNull.writerWithView ( serializationView );

            objWriterCacheNoNull.put ( serializationView, temp );

            return temp;
        }
    }

    @SuppressWarnings("unchecked")
    private static ObjectWriter getObjWriterNoType(Class <?> serializationView) {
        if ( objWriterCacheNoType.get ( serializationView ) != null ) {
            return (ObjectWriter) objWriterCacheNoType.get ( serializationView );
        } else {
            ObjectWriter temp = mapperNoType.writerWithView ( serializationView );

            objWriterCacheNoType.put ( serializationView, temp );

            return temp;
        }
    }
}
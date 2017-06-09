package com.anxin.replile.comms.exceptions;

/**
 * @author RANHUI
 * @version V1.0.0
 * @Created with: anxin-reptile
 * @Title: ${FILE_NAME}
 * @Package com.anxin.replile.comms.exceptions
 * @ClassName: ${TYPE_NAME}
 * @Description: ${TODO}(用一句话描述该文件做什么)
 * @date 2017/6/5 13:40
 */
public class EngineException extends RuntimeException {
    private static final long serialVersionUID = 601366631919634564L;
    private int code;
    private String message;

    public EngineException format(Object... messages){
        if(null!=messages && messages.length>0){
            this.message = String.format(this.message, messages);
        }
        return this;
    }

    public EngineException(int code, String message){
        super(message);
        this.code = code;
        this.message = message;
    }

    public EngineException(int code, String message, Throwable cause){
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }


    @Override
    public String getMessage() {
        return message;
    }
}

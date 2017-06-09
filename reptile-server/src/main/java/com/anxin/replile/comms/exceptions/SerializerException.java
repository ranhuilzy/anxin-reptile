package com.anxin.replile.comms.exceptions;


/**
 * 搴忓垪鍖栨垨鑰呭弽搴忓垪鍖栧紓甯�
 *
 * @author jjb
 *
 */
public class SerializerException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // 错误code
    public String errorCode;

    public SerializerException() {

    }

    public SerializerException(String exceptionInfo) {
        super(exceptionInfo);
    }

    public SerializerException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SerializerException(String errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

}


package com.tmax.proobject.whojes.exception;

import com.tmax.proobject.core.constant.ExceptionCode;
import com.tmax.proobject.core.exception.ProObjectWebException;

public class CkException extends ProObjectWebException {

    private static final long serialVersionUID = 7569756804891154396L;

    public CkException(ExceptionCode exceptionCode, int statusCode) {
        super(statusCode, exceptionCode.getCode(), exceptionCode.getCodeName(),
                "");
    }

    public CkException(ExceptionCode exceptionCode, String exceptionMsg, int statusCode) {
        super(statusCode, exceptionCode.getCode(), exceptionCode.getCodeName(),
                exceptionMsg);
    }

    public CkException(ExceptionCode exceptionCode, String exceptionMsg, Throwable cause, int statusCode) {
        super(statusCode, exceptionCode.getCode(), exceptionCode.getCodeName(),
                exceptionMsg, cause);
    }
}

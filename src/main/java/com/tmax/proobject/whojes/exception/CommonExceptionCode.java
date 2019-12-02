package com.tmax.proobject.whojes.exception;

import com.tmax.proobject.core.constant.ExceptionCode;

/**
 * CkErrorCode
 */
public enum CommonExceptionCode implements ExceptionCode {
    BAD_REQUEST_EXCEPTION("10001", "BAD_REQUEST_EXCEPTION"),
    UNAUTHORIZED_EXCEPTION("10002", "UNAUTHORIZED_EXCEPTION"),
    FORBIDDEN_EXCEPTION("10003", "FORBIDDEN_EXCEPTION"),
    CONFLICT_EXCEPTION("10004", "CONFLICT_EXCEPTION"),
    INTERNAL_SERVER_ERROR_EXCEPTION("10005", "INTERNAL_SERVER_ERROR_EXCEPTION"),
    NOT_IMPLEMENTED_EXCEPTION("10006", "NOT_IMPLEMENTED_EXCEPTION"),
    SERVICE_NOT_AVAILABLE_EXCEPTION("10007", "SERVICE_NOT_AVAILABLE_EXCEPTION"),

    
    DATASOURCE_MUST_BE_INITIALIZED("20001", "DATASOURCE_MUST_BE_INITIALIZED"),
    ;

    private final String code;
    private final String codeName;

    CommonExceptionCode(String code, String codeName) {
        this.code = code;
        this.codeName = codeName;
    };

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getCodeName() {
        return codeName;
    }
}
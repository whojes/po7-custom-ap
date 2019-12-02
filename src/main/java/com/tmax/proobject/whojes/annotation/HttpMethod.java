package com.tmax.proobject.whojes.annotation;

public enum HttpMethod {
    GET("Get"),
    POST("Create"),
    PUT("Update"),
    DELETE("Delete"),
    CUSTOM("Custom"),
    ;

    private String value;
    private HttpMethod(String val) {
        this.value = val;
    }
    public String getValue() {
        return this.value;
    }
}
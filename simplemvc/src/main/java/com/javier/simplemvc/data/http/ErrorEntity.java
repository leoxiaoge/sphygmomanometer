package com.javier.simplemvc.data.http;

/**
 * Created by javier on 2016/3/29.
 * <p>
 * 描述错误的对象
 */
@SuppressWarnings("unused")
public class ErrorEntity {
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

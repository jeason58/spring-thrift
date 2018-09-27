package com.jeason.springthrift.exception;

/**
 * @Auther: jeason
 * @Date: 2018/8/4 20:25
 * @Description:
 */
public class ThriftServerException extends Exception {
    public ThriftServerException() {
        super();
    }

    public ThriftServerException(String message) {
        super(message);
    }
}

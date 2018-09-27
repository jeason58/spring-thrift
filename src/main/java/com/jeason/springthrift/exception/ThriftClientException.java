package com.jeason.springthrift.exception;

/**
 * @Auther: jeason
 * @Date: 2018/8/4 23:49
 * @Description:
 */
public class ThriftClientException extends Exception {
    public ThriftClientException() {
        super();
    }

    public ThriftClientException(String message) {
        super(message);
    }
}

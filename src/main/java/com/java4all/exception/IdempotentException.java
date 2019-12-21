package com.java4all.exception;

import java.security.PrivilegedActionException;

/**
 * Idempotent Exception
 * If there is a custom global exception, you need to inherit the custom global exception.
 * @author ITyunqing
 * @email 1186355422@qq.com
 */
public class IdempotentException extends Exception{

    public IdempotentException() {
        super();
    }

    public IdempotentException(String message) {
        super(message);
    }

    public IdempotentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdempotentException(Throwable cause) {
        super(cause);
    }

    protected IdempotentException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

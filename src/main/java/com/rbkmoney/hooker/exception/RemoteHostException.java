package com.rbkmoney.hooker.exception;

public class RemoteHostException extends RuntimeException {
    public RemoteHostException() {
        super();
    }

    public RemoteHostException(String message) {
        super(message);
    }

    public RemoteHostException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteHostException(Throwable cause) {
        super(cause);
    }

    protected RemoteHostException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

package com.rbkmoney.hooker.service.err;

public class PostRequestException extends Exception {
    public PostRequestException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return "Unknown error during request to merchant execution. \n" + getCause().getMessage();
    }
}

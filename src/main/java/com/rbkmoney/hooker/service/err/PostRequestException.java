package com.rbkmoney.hooker.service.err;

public class PostRequestException extends Exception {
    public PostRequestException(Throwable cause) {
        super(cause);
    }

    public PostRequestException(String errMessage) {
        super(errMessage);
    }

    @Override
    public String getMessage() {
        return "Unknown error during request to merchant execution. \n" + getCause().getMessage();
    }
}

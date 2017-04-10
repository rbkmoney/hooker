package com.rbkmoney.hooker.handler;

/**
 * Created by inalarsanukaev on 10.04.17.
 */
public class PollingException extends Exception {
    public PollingException(String s) {
        super(s);
    }

    public PollingException(Throwable e) {
        super(e);
    }
}

package com.rbkmoney.hooker.service.crypt;

public interface Signer {
    String sign(String data, String secret);

    KeyPair generateKeys();
}

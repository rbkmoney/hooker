package com.rbkmoney.hooker.service.crypt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class KeyPair {
    private String privKey;
    private String publKey;
}

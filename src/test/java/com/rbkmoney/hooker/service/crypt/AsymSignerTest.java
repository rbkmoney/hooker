package com.rbkmoney.hooker.service.crypt;

import org.junit.Test;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by inalarsanukaev on 14.12.16.
 */

public class AsymSignerTest {
    @Test
    public void test() throws Exception {
        AsymSigner asymSigner = new AsymSigner();
        KeyPair keyPair = asymSigner.generateKeys();
        String data = "{\"invoice_id\":\"2Dbs4d4Dw\",\"amount\":120000,\"currency\":\"RUB\",\"created_at\":\"2011-07-01T09:00:00Z\",\"context\":{\"type\":null,\"data\":\"eyJvcmRlcl9pZCI6Im15X29yZGVyX2lkIn0=\",\"setType\":false,\"setData\":true},\"status\":\"PAID\"}";
        String sign = asymSigner.sign(data, keyPair.getPrivKey());
        byte[] sigBytes = Base64.getDecoder().decode(sign);

        byte[] publicBytes = Base64.getDecoder().decode(keyPair.getPublKey());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(AsymSigner.KEY_ALGORITHM);
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        Signature signature1 = Signature.getInstance(AsymSigner.HASH_ALGORITHM);
        signature1.initVerify(pubKey);
        signature1.update(data.getBytes());
        assertTrue(signature1.verify(sigBytes));

        Signature signature2 = Signature.getInstance(AsymSigner.HASH_ALGORITHM);
        signature2.initVerify(pubKey);
        signature2.update("other text".getBytes());
        assertFalse(signature2.verify(sigBytes));
    }
}

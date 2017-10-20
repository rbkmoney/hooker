package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.rbkmoney.damsel.json.Value;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by inalarsanukaev on 20.10.17.
 */
public class CustomerUtilsTest {

    @Test
    public void testObj() throws IOException {
        ArrayList<Value> value1 = new ArrayList<>();

        Value value4 = new Value();
        value4.setI(123);
        value1.add(value4);
        value1.add(value4);
        value1.add(value4);

        Value value = new Value();
        value.setArr(value1);

        JsonNode result = new CustomerUtils().getResult(value);
        assertTrue("[123,123,123]".equals(result.toString()));
    }

    @Test
    public void testArr() throws IOException {
        Value value = new Value();
        HashMap<String, Value> map = new HashMap<>();
        Value value1 = new Value();
        value1.setStr("value1");
        map.put("field1", value1);
        Value value2 = new Value();
        ArrayList<Value> value3 = new ArrayList<>();
        Value value4 = new Value();
        value4.setI(123);
        value3.add(value4);
        value3.add(value4);
        value3.add(value4);
        value2.setArr(value3);
        map.put("field2", value2);
        value.setObj(map);
        JsonNode result = new CustomerUtils().getResult(value);
        assertTrue("{\"field1\":\"value1\",\"field2\":[123,123,123]}".equals(result.toString()));
    }

}

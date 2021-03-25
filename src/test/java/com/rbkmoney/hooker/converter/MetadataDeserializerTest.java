package com.rbkmoney.hooker.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.json.Null;
import com.rbkmoney.damsel.json.Value;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetadataDeserializerTest {

    private final MetadataDeserializer metadataDeserializer = new MetadataDeserializer(new ObjectMapper());

    @Test
    public void deserialize() {
        Value value = Value.obj(Map.of(
                "bool", Value.b(true),
                "int", Value.i(1),
                "float", Value.flt(1.2),
                "string", Value.str("keksik"),
                "array", Value.arr(List.of(Value.i(12), Value.nl(new Null()))),
                "nested", Value.obj(Map.of("nestedStr", Value.str("nestedKeks")))));
        Object object = metadataDeserializer.deserialize(value);
        assertTrue(object instanceof Map);
        Map map = (Map) object;
        assertEquals(true, map.get("bool"));
        assertEquals(1, map.get("int"));
        assertEquals("keksik", map.get("string"));
        Object array = map.get("array");
        assertTrue(array instanceof List);
        assertEquals(12, ((List) array).get(0));
        Object nestedObject = map.get("nested");
        assertTrue(nestedObject instanceof Map);
        assertEquals("nestedKeks", ((Map) nestedObject).get("nestedStr"));
    }
}

package com.rbkmoney.hooker.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TBase;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class MetadataDeserializer {
    private final ObjectMapper objectMapper;

    public Object deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, HashMap.class);
        } catch (Exception e) {
            return null;
        }
    }

    public JsonNode deserialize(TBase tBase) {
        try {
            return new TBaseProcessor().process(tBase, new JsonHandler());
        } catch (Exception e) {
            return null;
        }
    }
}

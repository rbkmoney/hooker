package com.rbkmoney.hooker.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.geck.serializer.kit.json.JsonHandler;
import com.rbkmoney.geck.serializer.kit.tbase.TBaseProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataDeserializer {

    private final ObjectMapper objectMapper;

    public Object deserialize(byte[] data) {
        try {
            return objectMapper.readValue(data, Map.class);
        } catch (Exception e) {
            log.error("Error when deserialize byte array. It must be json.");
            return null;
        }
    }

    @SneakyThrows
    public JsonNode deserialize(TBase tBase) {
        return new TBaseProcessor().process(tBase, new JsonHandler());
    }
}

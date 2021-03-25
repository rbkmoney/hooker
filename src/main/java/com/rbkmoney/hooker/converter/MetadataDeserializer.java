package com.rbkmoney.hooker.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.json.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

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

    public Object deserialize(Value value) {
        if (!value.isSetObj()) {
            throw new IllegalArgumentException("Wrong metadata format. It should be obj: " + value);
        }
        return deserializeValue(value);
    }

    private Object deserializeValue(Value value) {
        if (value.isSetObj()) {
            return value.getObj().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> deserializeValue(e.getValue())));
        } else if (value.isSetArr()) {
            return value.getArr().stream().map(this::deserializeValue).collect(Collectors.toList());
        } else if (value.isSetNl()) {
            return null;
        } else {
            return value.getFieldValue();
        }
    }
}

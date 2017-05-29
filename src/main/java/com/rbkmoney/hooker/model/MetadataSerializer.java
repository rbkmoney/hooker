package com.rbkmoney.hooker.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by inalarsanukaev on 26.05.17.
 */
public class MetadataSerializer extends JsonSerializer<InvoiceContent> {

    //it's thread-safe object, parni
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void serialize(InvoiceContent ic, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (ic != null && ic.getData() != null) {
            jsonGenerator.writeObject(objectMapper.readValue(ic.getData(), HashMap.class));
        } else {
            jsonGenerator.writeNull();
        }
    }
}

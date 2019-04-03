package com.rbkmoney.hooker.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rbkmoney.damsel.json.Value;
import com.rbkmoney.geck.common.stack.ObjectStack;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Created by inalarsanukaev on 20.10.17.
 */
@Slf4j
public class CustomerUtils {

    private ObjectStack<String> names = new ObjectStack<>();
    private ObjectStack<JsonNodeWrapper> nodes = new ObjectStack<>();
    private JsonNode rootNode;
    private ObjectMapper mapper = new ObjectMapper();

    public JsonNode getResult(Value value) {
        try {
            String ROOT = "root";
            names.push(ROOT);
            if (value.isSetNl()) {
                return rootNode;
            } else if (value.isSetObj() || value.isSetArr()) {
                ObjectNode objectNode = mapper.createObjectNode();
                rootNode = objectNode;
                nodes.push(new ObjectNodeWrapper(names, objectNode));
            } else {
                throw new BadFormatException("Incorrect value of thrift-object " + value + ". Must be object, array or null");
            }
            process(value);
            nodes.pop();
            if (!(nodes.isEmpty() && names.isEmpty())) {
                throw new BadFormatException("Something went wrong: stacks is not empty!");
            }
            return rootNode.get(ROOT);
        } catch (Exception e) {
            log.warn("Unexpected error when converting thrift-model {}", value, e);
        }
        return null;
    }

    private void process(Value value) {
        if (value.isSetNl()) {
            nodes.peek().addNull();
        } else if (value.isSetB()){
            nodes.peek().add(value.getB());
        } else if (value.isSetI()) {
            nodes.peek().add(value.getI());
        } else if (value.isSetFlt()) {
            nodes.peek().add(value.getFlt());
        } else if (value.isSetStr()) {
            nodes.peek().add(value.getStr());
        } else if (value.isSetObj()) {
            nodes.push(nodes.peek().addObject(names));
            value.getObj().entrySet().forEach(e -> {
                names.push(e.getKey());
                process(e.getValue());
            });
            nodes.pop();
        } else if (value.isSetArr()) {
            nodes.push(nodes.peek().addArray());
            value.getArr().forEach(v -> process(v));
            nodes.pop();
        }
    }

    private interface JsonNodeWrapper {
        ArrayNodeWrapper addArray();
        ObjectNodeWrapper addObject(ObjectStack<String> names);
        void add(boolean value);
        void add(String value);
        void add(double value);
        void add(long value);
        void add(byte[] value);
        void addNull();
    }

    private static class ObjectNodeWrapper implements JsonNodeWrapper {
        private ObjectStack<String> names;
        private ObjectNode node;

        public ObjectNodeWrapper(ObjectStack<String> names, ObjectNode node) {
            this.names = names;
            this.node = node;
        }

        @Override
        public ArrayNodeWrapper addArray() {
            return new ArrayNodeWrapper(node.putArray(names.pop()));
        }

        @Override
        public ObjectNodeWrapper addObject(ObjectStack<String> names) {
            return new ObjectNodeWrapper(names, node.putObject(names.pop()));
        }

        @Override
        public void add(boolean value) {
            node.put(names.pop(), value);
        }

        @Override
        public void add(String value) {
            node.put(names.pop(), value);
        }

        @Override
        public void add(double value) {
            node.put(names.pop(), value);
        }

        @Override
        public void add(long value) {
            node.put(names.pop(), value);
        }

        @Override
        public void add(byte[] value) {
            node.put(names.pop(), value);
        }

        @Override
        public void addNull() {
            node.putNull(names.pop());
        }
    }

    private static class ArrayNodeWrapper implements JsonNodeWrapper {
        private ArrayNode node;

        public ArrayNodeWrapper(ArrayNode node) {
            this.node = node;
        }

        @Override
        public ArrayNodeWrapper addArray() {
            return new ArrayNodeWrapper(node.addArray());
        }

        @Override
        public ObjectNodeWrapper addObject(ObjectStack<String> names) {
            return new ObjectNodeWrapper(names, node.addObject());
        }

        @Override
        public void add(boolean value) {
            node.add(value);
        }

        @Override
        public void add(String value) {
            node.add(value);
        }

        @Override
        public void add(double value) {
            node.add(value);
        }

        @Override
        public void add(long value) {
            node.add(value);
        }

        @Override
        public void add(byte[] value) {
            node.add(value);
        }

        @Override
        public void addNull() {
            node.addNull();
        }
    }

    private class BadFormatException extends RuntimeException {
        public BadFormatException(String s) {
            super(s);
        }
    }

    public static JsonNode getJsonObject(String json) {
        if (json != null) {
            try {
                return new ObjectMapper().readTree(json);
            } catch (IOException e) {
                log.warn("Unexpected error when converting json {}", json, e);
            }
        }
        return null;
    }
}

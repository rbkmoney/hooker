package com.rbkmoney.hooker.converter;

public interface BinaryConverter<T> {

    T convert(byte[] bin, Class<T> clazz);

}

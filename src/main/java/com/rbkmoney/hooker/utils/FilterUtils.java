package com.rbkmoney.hooker.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FilterUtils {
    public static<T> List<T> filter(int[] batchResult, List<T> messages) {
        return IntStream.range(0, batchResult.length)
                .filter(i -> batchResult[i] != 0)
                .mapToObj(messages::get)
                .collect(Collectors.toList());
    }

    public static int[] filter(int[] batchResult) {
        return Arrays.stream(batchResult).filter(x -> x != 0).toArray();
    }
}

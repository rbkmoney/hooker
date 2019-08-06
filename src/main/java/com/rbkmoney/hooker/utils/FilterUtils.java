package com.rbkmoney.hooker.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterUtils {
    public static<T> List<T> filter(int[] batchResult, List<T> messages) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < batchResult.length; i++) {
            if (batchResult[i] != 0) {
                result.add(messages.get(i));
            }
        }
        return result;
    }

    public static int[] filter(int[] batchResult) {
        return Arrays.stream(batchResult).filter(x -> x != 0).toArray();
    }
}

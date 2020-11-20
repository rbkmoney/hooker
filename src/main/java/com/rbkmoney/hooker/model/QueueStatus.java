package com.rbkmoney.hooker.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueueStatus {
    private Queue queue;
    private boolean isSuccess;
    private List<Long> messagesDone = new ArrayList<>();
}

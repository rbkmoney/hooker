package com.rbkmoney.hooker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by jeckep on 17.04.17.
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Task {
    long messageId;
    long queueId;
}

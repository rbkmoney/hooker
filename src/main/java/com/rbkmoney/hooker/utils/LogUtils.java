package com.rbkmoney.hooker.utils;

import com.rbkmoney.machinegun.eventsink.SinkEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Instant;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

public class LogUtils {

    public static String toSummaryString(List<ConsumerRecord<String, SinkEvent>> records) {
        if (records.isEmpty()) {
            return "empty";
        }

        ConsumerRecord firstRecord = records.get(0);
        ConsumerRecord lastRecord = records.get(records.size() - 1);
        LongSummaryStatistics keySizeSummary = records.stream().mapToLong(ConsumerRecord::serializedKeySize).summaryStatistics();
        LongSummaryStatistics valueSizeSummary = records.stream().mapToLong(ConsumerRecord::serializedValueSize).summaryStatistics();
        return String.format("topic='%s', partition=%d, offset={%d...%d}, createdAt={%s...%s}, keySize={min=%d, max=%d, avg=%s}, valueSize={min=%d, max=%d, avg=%s}",
                firstRecord.topic(), firstRecord.partition(),
                firstRecord.offset(), lastRecord.offset(),
                Instant.ofEpochMilli(firstRecord.timestamp()), Instant.ofEpochMilli(lastRecord.timestamp()),
                keySizeSummary.getMin(), keySizeSummary.getMax(), keySizeSummary.getAverage(),
                valueSizeSummary.getMin(), valueSizeSummary.getMax(), valueSizeSummary.getAverage()
        );
    }

    public static String toSummaryStringWithValues(List<ConsumerRecord<String, SinkEvent>> records) {
        String valueKeysString = records.stream().map(ConsumerRecord::value)
                .map(value -> String.format("'%s.%d'", value.getEvent().getSourceId(), value.getEvent().getEventId()))
                .collect(Collectors.joining(", "));
        return String.format("%s, values={%s}", toSummaryString(records), valueKeysString);
    }
}

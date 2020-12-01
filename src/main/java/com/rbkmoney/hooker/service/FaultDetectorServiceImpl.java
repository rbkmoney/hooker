package com.rbkmoney.hooker.service;

import com.rbkmoney.damsel.fault_detector.*;
import com.rbkmoney.damsel.fault_detector.Error;
import com.rbkmoney.geck.common.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultDetectorServiceImpl implements FaultDetectorService {

    private final FaultDetectorSrv.Iface faultDetector;

    @Value("${service.fault-detector.slidingWindow}")
    private Long slidingWindowMillis;

    @Value("${service.fault-detector.operationTimeLimit}")
    private Long operationTimeLimit;

    private ServiceConfig serviceConfig;

    @PostConstruct
    public void init() {
        serviceConfig = new ServiceConfig()
                .setSlidingWindow(slidingWindowMillis)
                .setOperationTimeLimit(operationTimeLimit);
    }

    @Override
    public double getRate(long hookId) {
        try {
            List<ServiceStatistics> statistics = faultDetector.getStatistics(List.of(buildServiceId(hookId)));
            return statistics.isEmpty() ? 0 : statistics.get(0).getFailureRate();
        } catch (Exception e) {
            log.error("Error in FaultDetectorService when getStatistics", e);
            return 0;
        }
    }

    @Override
    public void startRequest(long hookId, long eventId) {
        registerOperation(hookId, eventId, OperationState.start(new Start(getNow())));
    }

    @Override
    public void finishRequest(long hookId, long eventId) {
        registerOperation(hookId, eventId, OperationState.finish(new Finish(getNow())));
    }

    @Override
    public void errorRequest(long hookId, long eventId) {
        registerOperation(hookId, eventId, OperationState.error(new Error(getNow())));
    }

    private void registerOperation(long hookId, long eventId, OperationState operationState) {
        Operation operation = new Operation()
                .setOperationId(String.valueOf(eventId))
                .setState(operationState);
        try {
            faultDetector.registerOperation(buildServiceId(hookId), operation, serviceConfig);
        } catch (Exception e) {
            log.error("Error in FaultDetectorService when registerOperation", e);
        }
    }

    private String getNow() {
        return TypeUtil.temporalToString(LocalDateTime.now(ZoneOffset.UTC));
    }

    private String buildServiceId(long id) {
        return "hooker-" + id;
    }
}

package ru.github.metrics.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.github.metrics.decompression.SnappyDecompression;
import ru.github.metrics.mapping.TimeSeriesUtils;
import ru.github.metrics.model.WriteRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class MetricsController {

    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    @Autowired
    public MetricsController(KafkaTemplate<String, Map<String, Object>> kafkaTemplate, TimeSeriesUtils timeSeriesMapper) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "${kafka-adapter.controller.path}", consumes = "application/x-protobuf")
    public void sendMetricsToKafka(@SnappyDecompression @RequestBody final WriteRequest writeRequest) {
        final String uid = UUID.randomUUID().toString();
        log.info("[{}] Start process prometheus metrics", uid);
        final long totalMetricsCount = writeRequest.getTimeseriesList().stream()
                .map(TimeSeriesUtils::mapToFlatMetrics)
                .flatMap(List::stream)
                .peek(this::send)
                .count();
        log.info("[{}] Processed {} metrics", uid, totalMetricsCount);
    }

    public void send(Map<String, Object> metrics) {
        kafkaTemplate.sendDefault(metrics).addCallback(
                (SendResult<String, Map<String, Object>> sendResult) -> log.debug("Metrics successfully sent. SendResult: {}", sendResult),
                (Throwable throwable) -> log.error("Error while sending metrics", throwable));
    }
}

package ru.github.metrics.mapping;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.github.metrics.model.types.Label;
import ru.github.metrics.model.types.Sample;
import ru.github.metrics.model.types.TimeSeries;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class TimeSeriesUtils {

    public static List<Map<String, Object>> mapToFlatMetrics(TimeSeries timeSeries) {
        try {
            return tryMapToFlatMetrics(timeSeries);
        } catch (Exception ex) {
            log.warn("Error while mapping TimeSeries", ex);
            return Collections.emptyList();
        }
    }

    private static List<Map<String, Object>> tryMapToFlatMetrics(TimeSeries timeSeries) {
        final List<Label> labels = timeSeries.getLabelsList();
        final MetricDescriptor metricDescriptor = new MetricDescriptor(labels);
        return timeSeries.getSamplesList().stream()
                .map((Sample sample) -> {
                    Map<String, Object> resultMetric = new HashMap<>();
                    resultMetric.put("name", metricDescriptor.getMetricName());
                    resultMetric.put("timestamp", timestampAsIsoUtcString(sample.getTimestamp()));
                    resultMetric.put("value", sample.getValue());
                    resultMetric.putAll(metricDescriptor.getModifiedLabels());
                    return resultMetric;
                })
                .collect(Collectors.toList());
    }

    private static String timestampAsIsoUtcString(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    }
}

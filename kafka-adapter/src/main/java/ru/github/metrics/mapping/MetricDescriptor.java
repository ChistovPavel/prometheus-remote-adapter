package ru.github.metrics.mapping;

import lombok.Getter;
import ru.github.metrics.model.types.Label;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
class MetricDescriptor {

    private static final String METRIC_NAME_LABEL = "__name__";

    private final String metricName;
    private final Map<String, String> modifiedLabels;

    MetricDescriptor(List<Label> labels) {
        this.metricName = labels.stream()
                .filter((Label label) -> METRIC_NAME_LABEL.equals(label.getName()))
                .map(Label::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Metrics without required %s label", METRIC_NAME_LABEL)));
        this.modifiedLabels = labels.stream().collect(Collectors.toMap((Label label) -> "labels." + label.getName(), Label::getValue));
    }
}

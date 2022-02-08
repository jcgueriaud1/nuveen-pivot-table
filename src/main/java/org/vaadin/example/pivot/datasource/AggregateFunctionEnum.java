package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lists the standard set of aggregate functions.
 * @author Martin Vysny <mavi@vaadin.com>
 */
public enum AggregateFunctionEnum implements AggregateFunction {
    AVERAGE("Average") {
        @Override
        @NotNull
        public Number compute(@NotNull Stream<?> values) {
            return values.mapToDouble(it -> ((Number) it).doubleValue()).average().orElse(0);
        }

        @Override
        public boolean canComputeOverAggregatedValues() {
            return false;
        }

        @NotNull
        @Override
        public Number computeOverAggregatedValues(@NotNull List<?> values) {
            throw new UnsupportedOperationException();
        }
    },
    SUM("Sum") {
        @Override
        @NotNull
        public Number compute(@NotNull Stream<?> values) {
            return values.mapToDouble(it -> ((Number) it).doubleValue()).sum();
        }

        @Override
        public boolean canComputeOverAggregatedValues() {
            return true;
        }

        @NotNull
        @Override
        public Number computeOverAggregatedValues(@NotNull List<?> values) {
            return compute(values.stream());
        }
    },
    COUNT("Count") {
        @Override
        @NotNull
        public Number compute(@NotNull Stream<?> values) {
            return values.count();
        }

        @Override
        public boolean canComputeOverAggregatedValues() {
            return true;
        }

        @NotNull
        @Override
        public Number computeOverAggregatedValues(@NotNull List<?> values) {
            return SUM.compute(values.stream());
        }
    },
    MIN("Min") {
        @SuppressWarnings({"unchecked", "rawtypes", "OptionalGetWithoutIsPresent"})
        @Override
        @NotNull
        public Number compute(@NotNull Stream<?> values) {
            return ((Number) values.min((Comparator<Object>) Comparator.comparing(o -> ((Comparable) o))).get());
        }

        @Override
        public boolean canComputeOverAggregatedValues() {
            return true;
        }

        @NotNull
        @Override
        public Number computeOverAggregatedValues(@NotNull List<?> values) {
            return compute(values.stream());
        }
    },

    MAX("Max") {
        @SuppressWarnings({"unchecked", "rawtypes", "OptionalGetWithoutIsPresent"})
        @Override
        @NotNull
        public Number compute(@NotNull Stream<?> values) {
            return ((Number) values.max((Comparator<Object>) Comparator.comparing(o -> ((Comparable) o))).get());
        }

        @Override
        public boolean canComputeOverAggregatedValues() {
            return true;
        }

        @NotNull
        @Override
        public Number computeOverAggregatedValues(@NotNull List<?> values) {
            return compute(values.stream());
        }
    },

    MEDIAN("Median") {
        @NotNull
        @Override
        public Number compute(@NotNull Stream<?> values) {
            final List<Double> list = values.map(it -> ((Number) it).doubleValue()).collect(Collectors.toList());
            return MathUtils.median(list);
        }

        @Override
        public boolean canComputeOverAggregatedValues() {
            return false;
        }

        @NotNull
        @Override
        public Number computeOverAggregatedValues(@NotNull List<?> values) {
            throw new UnsupportedOperationException();
        }
    };

    @NotNull
    private final String caption;

    AggregateFunctionEnum(@NotNull String caption) {
        this.caption = caption;
    }

    @NotNull
    @Override
    public String getCaption() {
        return caption;
    }
}

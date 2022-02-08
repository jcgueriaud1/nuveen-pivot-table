package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * An aggregate, capable of computing the aggregated value out of list of beans
 * based on aggregate {@link #function} - see {@link #computeAggregatedValue(List)}
 * for details.
 */
public final class Aggregate<T> implements Serializable {
    /**
     * An unique ID; this allows to reference the value computed by this aggregate
     * in {@link InMemoryPivot.Row}.
     */
    @NotNull
    public final String id;

    /**
     * Extracts the value-to-be-aggregated out of a single bean;
     * this enables us to use {@link #function} to compute the aggregated values out of a list of beans.
     * See {@link #computeAggregatedValue(List)} for more details.
     */
    @NotNull
    public final PivotProperty<T> property;
    /**
     * Computes the aggregated value.
     */
    @NotNull
    public final AggregateFunction function;

    private final boolean filterEnabled;

    public Aggregate(@NotNull PivotProperty<T> property, @NotNull AggregateFunction function, boolean filterEnabled) {
        this(property, function, property.caption + "/" + UUID.randomUUID(), filterEnabled);
    }

    public Aggregate(@NotNull PivotProperty<T> property, @NotNull AggregateFunction function, @NotNull String id, boolean filterEnabled) {
        this.id = Objects.requireNonNull(id);
        this.property = Objects.requireNonNull(property);
        this.function = Objects.requireNonNull(function);
        this.filterEnabled = filterEnabled;
    }

    /**
     * Computes the aggregated value over a list of beans.
     * @param groupedItems the list of beans, must not be empty.
     * @return the aggregated value as produced by {@link #function}.
     */
    @NotNull
    public Number computeAggregatedValue(@NotNull Collection<T> groupedItems) {
        if (groupedItems.isEmpty()) {
            throw new IllegalArgumentException("Parameter groupedItems: invalid value " + groupedItems + ": empty");
        }
        final Stream<?> numericValues = groupedItems.stream()
                .map(property.valueProvider);
        return function.compute(numericValues);
    }

    @NotNull
    public String getCaption() {
        return property.caption + " (" + function.getCaption() + ")";
    }

    @Override
    public String toString() {
        return "Aggregate{" +
                "id='" + id + '\'' +
                ", property=" + property +
                ", function=" + function +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregate<?> aggregate = (Aggregate<?>) o;
        return id.equals(aggregate.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @NotNull
    public Aggregate<T> withFunction(AggregateFunction function) {
        return new Aggregate<>(property, function, id, filterEnabled);
    }

    @NotNull
    public Aggregate<T> withFilterEnabled(boolean enabled) {
        return new Aggregate<>(property, function, id, enabled);
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }
}

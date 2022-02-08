package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * An aggregate function, takes a list of objects (usually Numbers) and computes
 * the aggregated value.
 * <p></p>
 * Usually the function can only take in Numbers; however certain functions can
 * take any objects (e.g. {@link AggregateFunctionEnum#COUNT}).
 * @author Martin Vysny <mavi@vaadin.com>
 */
public interface AggregateFunction {

    @NotNull
    String getCaption();

    /**
     * Perform the calculation on the input stream of values.
     * <p></p>
     * There is one value
     * per every bean (or maybe even fewer, since `null`s are filtered out);
     * therefore {@link AggregateFunctionEnum#AVERAGE} knows how many beans there are
     * and is able to calculate the average value correctly.
     *
     * @param values never null, never empty (always contains at least one value). Usually a number,
     *               but it may be a bean itself (e.g. when performing a count).
     * @return the outcome of the calculation.
     * @throws ClassCastException on unsupported type of value.
     */
    @NotNull
    Number compute(@NotNull Stream<?> values);

    /**
     * Checks whether this function is able to compute a grand total over a list of aggregated values
     * (the {@link #computeOverAggregatedValues(List)} function).
     * <p></p>
     * For example for {@link AggregateFunctionEnum#MIN}
     * and {@link AggregateFunctionEnum#MAX} it's possible to do so. However,
     * neither {@link AggregateFunctionEnum#AVERAGE} nor {@link AggregateFunctionEnum#MEDIAN}
     * can compute the value out of aggregated values, since it's not known how many
     * numbers were aggregated in the previous aggregation.
     * @return true if {@link #computeOverAggregatedValues(List)} works,
     * false if not.
     */
    boolean canComputeOverAggregatedValues();

    /**
     * Receives a stream of aggregated values and computes the grand total.
     * @param values never null, never empty (always contains at least one value). Usually a number,
     *               but it may be a bean itself (e.g. when performing a count).
     * @return the outcome of the calculation.
     * @throws UnsupportedOperationException if {@link #canComputeOverAggregatedValues()} returns false.
     * @throws ClassCastException on unsupported type of value.
     */
    @NotNull
    Number computeOverAggregatedValues(@NotNull List<?> values);
}

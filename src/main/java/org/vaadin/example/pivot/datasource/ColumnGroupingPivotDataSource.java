package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Allows one column to be grouped by horizontally. The DataSource creates a
 * column for every value of a particular {@link GroupBy} clause with {@link GroupBy#columnGrouping} being true.
 * <p></p>
 * The data is polled from the {@link #delegate}, then horizontally grouped in-memory.
 * <p></p>
 * Optionally you can add one final column, usually named "Grand Total", usually calculating
 * the sum of horizontally grouped values.
 *
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class ColumnGroupingPivotDataSource<T> implements PivotDataSource<T> {
    @NotNull
    private final PivotDataSource<T> delegate;
    @Nullable
    private final String grandTotalColumnCaption;

    /**
     * Creates the column/horizontal grouping data source.
     *
     * @param delegate                poll data from this data source. Can not be another {@link ColumnGroupingPivotDataSource}.
     * @param grandTotalColumnCaption if not null, a final "Grand Total" column is appended as the last one.
     *                                This value specifies the column's caption. If null, the final "Grand Total" column is not shown.
     */
    public ColumnGroupingPivotDataSource(
            @NotNull PivotDataSource<T> delegate,
            @Nullable String grandTotalColumnCaption
    ) {
        if (delegate instanceof ColumnGroupingPivotDataSource) {
            throw new IllegalArgumentException("Invalid arg: chaining ColumnGroupingPivotDataSource is not supported yet");
        }
        this.delegate = delegate;
        this.grandTotalColumnCaption = grandTotalColumnCaption;
    }

    @Override
    public String toString() {
        return "ColumnGroupingPivotDataSource{" +
                "delegate=" + delegate +
                ", grandTotalColumnCaption='" + grandTotalColumnCaption + '\'' +
                '}';
    }

    @NotNull
    private Object computeGroupingKey(@NotNull Row<T> item, @NotNull List<String> groupByIds) {
        return groupByIds.stream().map(it -> item.get(it)).collect(Collectors.toList());
    }

    @NotNull
    private String getId(@NotNull Object columnGroupValue, @NotNull String aggregateId) {
        return "dynamic-" + columnGroupValue + "-" + aggregateId;
    }

    /**
     * @return True if a final "Grand Total" column will be appended as the last column, in the
     * {@link PivotDataSource.PivotResult} produced by {@link PivotDataSource#computePivotRows(LinkedHashSet, LinkedHashSet, Set, List, PivotFilter)}.
     */
    public boolean hasGrandTotalColumn() {
        return grandTotalColumnCaption != null;
    }

    @Nullable
    private static <T> List<T> flattenBeans(@NotNull List<Row<T>> rows) {
        if (rows.stream().anyMatch(it -> it.sourceBeans == null)) {
            return null;
        }
        return rows.stream().flatMap(it -> it.sourceBeans.stream()).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public PivotResult<T> computePivotRows(
            @NotNull LinkedHashSet<GroupBy<T>> groupBy,
            @NotNull LinkedHashSet<Aggregate<T>> aggregates,
            @NotNull Set<String> grandTotalIDs,
            @NotNull List<T> items,
            @NotNull PivotFilter filters
    ) {
        final List<GroupBy<T>> columnGrouping = groupBy.stream().filter(it -> it.columnGrouping).collect(Collectors.toList());
        if (columnGrouping.isEmpty()) {
            return delegate.computePivotRows(groupBy, aggregates, grandTotalIDs, items, filters);
        }
        if (columnGrouping.size() > 1) {
            throw new IllegalArgumentException("Parameter groupBy: invalid value " + groupBy + ": at most 1 column grouping is supported but got " + columnGrouping);
        }
        final GroupBy<T> topGroup = columnGrouping.get(0);
        final String topGroupId = topGroup.id;

        groupBy = groupBy.stream().map(it -> it.withColumnGroup(false)).collect(Collectors.toCollection(LinkedHashSet::new));

        // create a sub filter, ignoring all dynamic or grand total values to prevent false filtering for the aggregate columns
        PivotFilter tmpFilter = new PivotFilter();
        Map<String, Object> map = filters.getFilterValues();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("dynamic-") && !key.startsWith("Grand Total-")) { // TODO extract "Grand Total" starter to a constant?
                tmpFilter.getFilterValues().put(key, entry.getValue());
            }
        }

        final PivotResult<T> result = delegate.computePivotRows(groupBy, aggregates, new LinkedHashSet<>(), items, tmpFilter);
        groupBy.remove(topGroup);

        final Map<String, Aggregate<T>> aggregateMap = new HashMap<>();
        for (Aggregate<T> aggregate : aggregates) {
            aggregateMap.put(aggregate.id, aggregate);
        }

        // group by all but the topGroupId; we'll create a column grouping out of that later on.
        final List<String> groupByIds = groupBy.stream().map(it -> it.id).collect(Collectors.toList());
        final Map<Object, List<Row<T>>> rows = result.rows.stream().collect(Collectors.groupingBy(it -> computeGroupingKey(it, groupByIds)));

        // collapse list of rows into a single row, populating columns properly.
        final List<Row<T>> newRows = new ArrayList<>();
        buildFinalRows: for (List<Row<T>> uncollapsed : rows.values()) {
            // the "collapsed" row holding horizontally grouped values.
            final Map<String, Object> newRow = new HashMap<>();
            // maps foldId to the list of values in the horizontally grouped row, so that we can compute grand totals.
            final Map<String, List<Object>> grandTotalsColumn = hasGrandTotalColumn() ? new HashMap<>() : null;

            // these values are the same for all rows present in the 'uncollapsed'
            for (String groupById : groupByIds) {
                newRow.put(groupById, uncollapsed.get(0).get(groupById));
            }

            for (Row<T> ungroupedRow : uncollapsed) {
                Object columnGroupValue = ungroupedRow.get(topGroupId);
                for (Aggregate<T> aggregate : aggregates) {
                    final String aggregateId = aggregate.id;
                    final Object value = ungroupedRow.get(aggregateId);
                    newRow.put(getId(columnGroupValue, aggregateId), value);

                    if (hasGrandTotalColumn()) {
                        grandTotalsColumn.computeIfAbsent(aggregateId, it -> new LinkedList<>()).add(value);
                    }
                }
            }

            if (grandTotalsColumn != null) {
                for (Map.Entry<String, List<Object>> entry : grandTotalsColumn.entrySet()) {
                    final Aggregate<T> grandTotalAggregate = aggregateMap.get(entry.getKey());
                    if (grandTotalAggregate != null) {
                        final Number grandTotal = grandTotalAggregate.function.compute(entry.getValue().stream());
                        newRow.put(grandTotalColumnCaption + "-" + entry.getKey(), grandTotal);
                    }
                }
            }

            Row<T> finalNewRow = new Row<>(newRow, flattenBeans(uncollapsed));
            Map<String, Object> values = filters.getFilterValues();

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                System.out.println("Test Objects.equals(" + entry.getValue() + "," + finalNewRow.get(entry.getKey()) + ": "
                        + (Objects.equals(entry.getValue(), finalNewRow.get(entry.getKey())) ? "allow" : "filter out"));
                if (!Objects.equals(entry.getValue(), finalNewRow.get(entry.getKey()))) {
                    continue buildFinalRows;
                }
            }

            newRows.add(finalNewRow);
        }

        // figure out the unique values of the columnGroupIds grouping column, so that we can create columns out of that.
        final List<Object> topGroupValuesSorted = result.rows.stream()
                .map(it -> it.get(topGroupId))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        // create the columns
        final List<PivotColumn<T>> columns = new ArrayList<>();
        for (GroupBy<T> clause : groupBy) {
            final PivotColumn<T> column = new PivotColumn<>(clause.id, null, null, clause);
            columns.add(column);
        }
        for (Object columnGroupValue : topGroupValuesSorted) {
            for (Aggregate<T> aggregate : aggregates) {
                final PivotColumn<T> column = new PivotColumn<>(
                        getId(columnGroupValue, aggregate.id),
                        columnGroupValue, aggregate, null
                );
                columns.add(column);
            }
        }
        if (hasGrandTotalColumn()) {
            for (Aggregate<T> aggregate : aggregates) {
                final PivotColumn<T> column = new PivotColumn<>(grandTotalColumnCaption + "-" + aggregate.id, grandTotalColumnCaption, aggregate, null);
                columns.add(column);
            }
        }

        // Computes footer grand totals.
        final HashMap<String, Object> grandTotalValues;
        // if this is true, we don't have to use computeOverAggregatedValues() (which is not supported for all functions)
        // but instead we can calculate the sum over the list of beans.
        final boolean hasBeans = newRows.stream().allMatch(it -> it.sourceBeans != null);
        if (hasBeans) {
            grandTotalValues = computeGrandTotalsInMemory(newRows, topGroup, grandTotalIDs, aggregateMap, topGroupValuesSorted);
        } else {
            grandTotalValues = computeGrandTotals(grandTotalIDs, aggregateMap, newRows, topGroupValuesSorted);
        }

        // create the result object
        return new PivotResult<>(newRows, columns, grandTotalValues);
    }

    /**
     * Computes footer grand totals. Uses {@link AggregateFunction#computeOverAggregatedValues(List)}.
     */
    @NotNull
    private HashMap<String, Object> computeGrandTotals(
            @NotNull Set<String> grandTotalIDs,
            @NotNull Map<String, Aggregate<T>> aggregateMap,
            @NotNull List<Row<T>> newRows,
            @NotNull List<Object> topGroupValuesSorted
    ) {
        final HashMap<String, Object> grandTotalValues = new HashMap<>();
        // fall back and use computeOverAggregatedValues()
        for (String grandTotalID : grandTotalIDs) {
            Aggregate<T> grandTotal = aggregateMap.get(grandTotalID);
            for (Object columnGroupValue : topGroupValuesSorted) {
                final String columnId = getId(columnGroupValue, grandTotal.id);
                final List<Object> aggregatedValues = newRows.stream()
                        .map(it -> it.get(columnId))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!aggregatedValues.isEmpty()) {
                    final Number aggregateValue = grandTotal.function.computeOverAggregatedValues(aggregatedValues);
                    grandTotalValues.put(columnId, aggregateValue);
                }
            }
        }
        return grandTotalValues;
    }

    /**
     * Computes footer grand totals from the in-memory list of beans.
     */
    @NotNull
    private HashMap<String, Object> computeGrandTotalsInMemory(
            @NotNull List<Row<T>> oldRows,
            @NotNull GroupBy<T> topGroup,
            @NotNull Set<String> grandTotalIDs,
            @NotNull Map<String, Aggregate<T>> aggregateMap,
            @NotNull List<Object> topGroupValuesSorted
    ) {
        final HashMap<String, Object> grandTotalValues = new HashMap<>();
        for (Object columnGroupValue : topGroupValuesSorted) {
            final List<T> beans = oldRows.stream()
                    .flatMap(it -> it.sourceBeans.stream())
                    .filter(it -> columnGroupValue.equals(topGroup.getValue(it)))
                    .collect(Collectors.toList());
            if (!beans.isEmpty()) {
                for (String grandTotalID : grandTotalIDs) {
                    final Aggregate<T> grandTotal = aggregateMap.get(grandTotalID);
                    final String columnId = getId(columnGroupValue, grandTotal.id);
                    final Number aggregatedValue = grandTotal.computeAggregatedValue(beans);
                    grandTotalValues.put(columnId, aggregatedValue);
                }
            }
        }
        return grandTotalValues;
    }
}

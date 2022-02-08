package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Computes pivot values from an in-memory data. Allows for arbitrary grouping and aggregating.
 */
public class InMemoryPivot<T> implements PivotDataSource<T> {
    /**
     * A group key for a particular row. Immutable. All rows with the same group key
     * are grouped together.
     */
    private static final class GroupKey {
        @NotNull
        private final Map<String, Object> idToGroupValue;

        public GroupKey(@NotNull Map<String, Object> idToGroupValue) {
            this.idToGroupValue = Objects.requireNonNull(idToGroupValue);
        }

        @Nullable
        public Object getValue(@NotNull String id) {
            return idToGroupValue.get(id);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupKey groupKey = (GroupKey) o;
            return idToGroupValue.equals(groupKey.idToGroupValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idToGroupValue);
        }

        @Override
        public String toString() {
            return "GroupKey{" + idToGroupValue + '}';
        }
    }

    @NotNull
    private GroupKey computeGroupingKey(@NotNull T item, @NotNull LinkedHashSet<GroupBy<T>> groupBy) {
        final Map<String, Object> idToGroupValue = new HashMap<>(groupBy.size());
        for (GroupBy<T> clause: groupBy) {
            idToGroupValue.put(clause.id, clause.getValue(item));
        }
        return new GroupKey(idToGroupValue);
    }

    @NotNull
    private Row<T> computeRow(@NotNull GroupKey key, @NotNull List<T> groupedItems, @NotNull LinkedHashSet<Aggregate<T>> aggregates) {
        final Map<String, Object> row = new HashMap<>(key.idToGroupValue.size() + aggregates.size());
        row.putAll(key.idToGroupValue);
        for (Aggregate<T> aggregate : aggregates) {
            final Number aggregatedValue = aggregate.computeAggregatedValue(groupedItems);
            row.put(aggregate.id, aggregatedValue);
        }
        return new Row<>(row, groupedItems);
    }

    @NotNull
    private Map<GroupKey, List<T>> computePivotData(
            @NotNull Collection<T> items,
            @NotNull LinkedHashSet<GroupBy<T>> groupBy
    ) {
        return items.stream().collect(Collectors.groupingBy(
                it -> computeGroupingKey(it, groupBy),
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    @NotNull
    @Override
    public PivotResult<T> computePivotRows(
            @NotNull LinkedHashSet<GroupBy<T>> groupBy,
            @NotNull LinkedHashSet<Aggregate<T>> aggregates,
            @NotNull Set<String> grandTotalIDs,
            @NotNull List<T> items,
            @NotNull PivotFilter filter
    ) {
        // parameter checking
        final Optional<GroupBy<T>> columnGrouping = groupBy.stream().filter(it -> it.columnGrouping).findAny();
        if (columnGrouping.isPresent()) {
            // you need to use ColumnGroupingPivotDataSource for column grouping
            throw new IllegalArgumentException("Parameter groupBy: invalid value " + groupBy + ": column grouping is not supported: " + columnGrouping.get());
        }
        final Map<String, Aggregate<T>> grandTotals = new HashMap<>();
        for (Aggregate<T> aggregate : aggregates) {
            if (grandTotalIDs.contains(aggregate.id)) {
                grandTotals.put(aggregate.id, aggregate);
            }
        }
        if (grandTotals.size() < grandTotalIDs.size()) {
            grandTotalIDs.removeAll(grandTotals.keySet());
            throw new IllegalArgumentException("Parameter grandTotalIDs: invalid value " + grandTotalIDs + ": no aggregates found for these IDs: " + grandTotalIDs);
        }

        // compute raw grouping data
        final Map<GroupKey, List<T>> pivotData = computePivotData(items, groupBy);

        // compute pivot row values
        final List<Row<T>> pivotRows = pivotData.entrySet().stream()
                .map(it -> computeRow(it.getKey(), it.getValue(), aggregates))
                .filter(row -> {
                    Set<Entry<String, Object>> filterValues = filter.getFilterValues().entrySet();

                    for (Entry<String, Object> filterValue : filterValues) {
                        String columnId = filterValue.getKey();
                        Object value = filterValue.getValue();
                        if (!Objects.equals(value, row.get(columnId))) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());


        // compute the list of columns
        final List<PivotColumn<T>> columns = new ArrayList<>();
        for (GroupBy<T> clause : groupBy) {
            columns.add(new PivotColumn<>(clause.id, null, null, clause));
        }
        for (Aggregate<T> aggregate : aggregates) {
            columns.add(new PivotColumn<>(aggregate.id, null, aggregate, null));
        }

        // compute the grand totals
        final HashMap<String, Object> grandTotalValues = new HashMap<>();
        if (!items.isEmpty()) {
            // only use items, for which rows are shown
            Collection<T> rowItems;
            if (filter.isEmpty()) {
                rowItems = items;
            } else {
                rowItems = pivotRows.stream().filter(r -> r.sourceBeans != null).flatMap(r -> r.sourceBeans.stream()).collect(Collectors.toSet());
            }

            for (Aggregate<T> grandTotal : grandTotals.values()) {
                final Number grandValue = grandTotal.computeAggregatedValue(rowItems);
                grandTotalValues.put(grandTotal.id, grandValue);
            }
        }

        return new PivotResult<>(pivotRows, columns, grandTotalValues);
    }

    @Override
    public String toString() {
        return "InMemoryPivot{}";
    }
}

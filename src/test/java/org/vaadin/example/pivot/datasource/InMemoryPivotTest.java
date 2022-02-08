package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.example.pivot.datasource.Utils.setOf;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class InMemoryPivotTest {

    @Test
    public void testEmptyList() {
        final PivotDataSource<Integer> pivot = new InMemoryPivot<>();
        final PivotProperty<Integer> identity = new PivotProperty<>("identity", it -> it);
        final Aggregate<Integer> sum = new Aggregate<>(identity, AggregateFunctionEnum.MEDIAN, "sum", false);
        final PivotDataSource.PivotResult<Integer> result =
                pivot.computePivotRows(setOf(),
                        setOf(sum),
                        setOf("sum"),
                        Collections.emptyList(),
                        new PivotDataSource.PivotFilter());

        // assert on the result meta-data
        assertNull(result.grandTotalValues.get("sum"));
        assertEquals(0, result.grandTotalValues.size());
        assertEquals(1, result.columns.size());
        final PivotDataSource.PivotColumn<Integer> col = result.getColumn("sum");
        assertNotNull(col);
        assertNull(col.columnGroupValue);
        assertNull(col.groupBy);
        assertEquals("sum", col.aggregate.id);

        // assert on the result list
        assertEquals(0, result.rows.size());
    }

    @Test
    public void simpleSumNoGrouping() {
        testSimpleAggregateOpNoGrouping(AggregateFunctionEnum.SUM, 10d);
    }

    @Test
    public void simpleAverageNoGrouping() {
        testSimpleAggregateOpNoGrouping(AggregateFunctionEnum.AVERAGE, 2d);
    }

    @Test
    public void simpleCountNoGrouping() {
        testSimpleAggregateOpNoGrouping(AggregateFunctionEnum.COUNT, 5L);
    }

    @Test
    public void simpleMaxNoGrouping() {
        testSimpleAggregateOpNoGrouping(AggregateFunctionEnum.MAX, 4);
    }

    @Test
    public void simpleMinNoGrouping() {
        testSimpleAggregateOpNoGrouping(AggregateFunctionEnum.MIN, 0);
    }

    @Test
    public void simpleMedianNoGrouping() {
        testSimpleAggregateOpNoGrouping(AggregateFunctionEnum.MEDIAN, 2d);
    }

    @Test
    public void simpleSumSimpleGrouping() {
        testSimpleAggregateOpSimpleGrouping(AggregateFunctionEnum.SUM, 10d, new Object[]{0d, 1d, 2d, 3d, 4d});
    }

    @Test
    public void simpleAverageSimpleGrouping() {
        testSimpleAggregateOpSimpleGrouping(AggregateFunctionEnum.AVERAGE, 2d, new Object[]{0d, 1d, 2d, 3d, 4d});
    }

    @Test
    public void simpleCountSimpleGrouping() {
        testSimpleAggregateOpSimpleGrouping(AggregateFunctionEnum.COUNT, 5L, new Object[]{1L, 1L, 1L, 1L, 1L});
    }

    @Test
    public void simpleMaxSimpleGrouping() {
        testSimpleAggregateOpSimpleGrouping(AggregateFunctionEnum.MAX, 4, new Object[]{0, 1, 2, 3, 4});
    }

    @Test
    public void simpleMinSimpleGrouping() {
        testSimpleAggregateOpSimpleGrouping(AggregateFunctionEnum.MIN, 0, new Object[]{0, 1, 2, 3, 4});
    }

    @Test
    public void simpleMedianSimpleGrouping() {
        testSimpleAggregateOpSimpleGrouping(AggregateFunctionEnum.MEDIAN, 2d, new Object[]{0d, 1d, 2d, 3d, 4d});
    }

    private void testSimpleAggregateOpNoGrouping(@NotNull AggregateFunction aggregate, @NotNull Object expectedValue) {
        final PivotDataSource<Integer> pivot = new InMemoryPivot<>();
        final PivotProperty<Integer> identity = new PivotProperty<>("identity", it -> it);
        final Aggregate<Integer> sum = new Aggregate<>(identity, aggregate, "sum", false);
        final PivotDataSource.PivotResult<Integer> result =
                pivot.computePivotRows(setOf(),
                        setOf(sum),
                        setOf("sum"),
                        Arrays.asList(0, 1, 2, 3, 4),
                        new PivotDataSource.PivotFilter());

        // assert on the result meta-data
        assertEquals(expectedValue, result.grandTotalValues.get("sum"));
        assertEquals(1, result.grandTotalValues.size());
        assertEquals(1, result.columns.size());
        final PivotDataSource.PivotColumn<Integer> col = result.getColumn("sum");
        assertNotNull(col);
        assertNull(col.columnGroupValue);
        assertNull(col.groupBy);
        assertEquals("sum", col.aggregate.id);

        // assert on the result list
        assertEquals(1, result.rows.size());
        final PivotDataSource.Row row = result.rows.get(0);
        assertEquals(expectedValue, row.get("sum"));
    }

    private void testSimpleAggregateOpSimpleGrouping(
            @NotNull AggregateFunction aggregate,
            @NotNull Object expectedGrandTotal,
            @NotNull Object[] expectedValue
    ) {
        final PivotDataSource<Integer> pivot = new InMemoryPivot<>();
        final PivotProperty<Integer> identity = new PivotProperty<>("identity", it -> it);
        final Aggregate<Integer> sum = new Aggregate<>(identity, aggregate, "sum", false);
        final PivotDataSource.PivotResult<Integer> result =
                pivot.computePivotRows(
                        setOf(new GroupBy<>(identity, false, "group", false)),
                        setOf(sum),
                        setOf("sum"),
                        Arrays.asList(0, 1, 2, 3, 4),
                        new PivotDataSource.PivotFilter());

        // assert on the result meta-data
        assertEquals(expectedGrandTotal, result.grandTotalValues.get("sum"));
        assertEquals(1, result.grandTotalValues.size());
        assertEquals(2, result.columns.size());
        final PivotDataSource.PivotColumn<Integer> col1 = result.getColumn("sum");
        assertNotNull(col1);
        assertNull(col1.columnGroupValue);
        assertNull(col1.groupBy);
        assertEquals("sum", col1.aggregate.id);
        final PivotDataSource.PivotColumn<Integer> col2 = result.getColumn("group");
        assertNotNull(col2);
        assertNull(col2.columnGroupValue);
        assertEquals("group", col2.groupBy.id);
        assertNull(col2.aggregate);

        // assert on the result list
        assertEquals(expectedValue.length, result.rows.size());
        Object[] actual = result.rows.stream()
                .map(it -> it.get("sum")).toArray();
        assertArrayEquals(expectedValue, actual, "Result: " + result.rows);
    }
}

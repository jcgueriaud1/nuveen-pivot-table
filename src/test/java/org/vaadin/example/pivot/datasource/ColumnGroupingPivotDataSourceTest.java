package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.vaadin.example.AssetException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.vaadin.example.pivot.datasource.Utils.setOf;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class ColumnGroupingPivotDataSourceTest {

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

    private void testSimpleAggregateOpSimpleGrouping(
            @NotNull AggregateFunction aggregate,
            @NotNull Object expectedRowGrandTotal,
            @NotNull Object[] expectedValue
    ) {
        PivotDataSource<Integer> pivot = new InMemoryPivot<>();
        pivot = new ColumnGroupingPivotDataSource<>(pivot, "Grand Total");
        final PivotProperty<Integer> identity = new PivotProperty<>("identity", it -> it);
        final Aggregate<Integer> sum = new Aggregate<>(identity, aggregate, "sum", false);
        final GroupBy<Integer> groupBy = new GroupBy<>(identity, true, false);
        final PivotDataSource.PivotResult<Integer> result =
                pivot.computePivotRows(setOf(groupBy),
                        setOf(sum),
                        setOf("sum"),
                        Arrays.asList(0, 1, 2, 3, 4),
                        new PivotDataSource.PivotFilter()
                );

        // assert on the result meta-data
        assertEquals(5, result.grandTotalValues.size());
        for (int i = 0; i < expectedValue.length; i++) {
            assertEquals(expectedValue[i], result.grandTotalValues.get("dynamic-" + i + "-sum"), "" + result.grandTotalValues);
        }
        assertEquals(6, result.columns.size());
        final PivotDataSource.PivotColumn<Integer> grandTotalCol = result.getColumn("Grand Total-sum");
        assertNotNull(grandTotalCol);
        assertEquals("Grand Total", grandTotalCol.columnGroupValue);
        assertNull(grandTotalCol.groupBy);
        assertEquals("sum", grandTotalCol.aggregate.id);

        // assert on the result list
        assertEquals(1, result.rows.size());
        for (int i = 0; i < expectedValue.length; i++) {
            assertEquals(expectedValue[i], result.rows.get(0).get("dynamic-" + i + "-sum"), "" + result.grandTotalValues);
        }
        assertEquals(expectedRowGrandTotal, result.rows.get(0).get("Grand Total-sum"));
    }

    @Test
    public void exceptionsTest() {
        final PivotDataSource<AssetException> pivot = new ColumnGroupingPivotDataSource<>(new InMemoryPivot<>(), "Grand Total");
        final PivotProperty<AssetException> identity = new PivotProperty<>("Exceptions", it -> it);
        final Aggregate<AssetException> count = new Aggregate<>(identity, AggregateFunctionEnum.COUNT, "count", false);
        final GroupBy<AssetException> groupByCouponType = new GroupBy<>(
                new PivotProperty<>("Coupon Type", AssetException::getCouponType), true, false);
        final GroupBy<AssetException> groupByAssetClass = new GroupBy<>(
                new PivotProperty<>("Asset Class", AssetException::getAssetClass), false, false);
        final PivotDataSource.PivotResult<AssetException> result = pivot.computePivotRows(setOf(groupByCouponType, groupByAssetClass),
                setOf(count),
                setOf("count"),
                AssetException.ALL_EXCEPTIONS,
                new PivotDataSource.PivotFilter());

        final String pivotResultString = Utils.toCsv(result);
        assertEquals("Asset Class: null,Exceptions (Count): Fixed,Exceptions (Count): Floating,Exceptions (Count): N/A,Exceptions (Count): Grand Total\n" +
                "======\n" +
                "Common Stock,null,null,2,1,\n" +
                "Muni,1,2,null,2,\n" +
                "Corp Debt,2,2,null,2,\n" +
                "======\n" +
                "null,3,4,2,null,\n", pivotResultString);
    }
}

package org.vaadin.example.pivot.datasource;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * @author Martin Vysny <mavi@vaadin.com>
 */
public class Utils {
    @SafeVarargs
    @NotNull
    public static <T> LinkedHashSet<T> setOf(@NotNull T... items) {
        return new LinkedHashSet<>(Arrays.asList(items));
    }

    @NotNull
    public static <T> String toCsv(@NotNull PivotDataSource.PivotResult<T> result) {
        final StringBuilder sb = new StringBuilder();
        sb.append(result.columns.stream().map(it -> it.getCaption() + ": " + it.columnGroupValue).collect(Collectors.joining(",")));
        sb.append('\n');
        sb.append("======\n");
        for (PivotDataSource.Row row : result.rows) {
            for (PivotDataSource.PivotColumn<?> col : result.columns) {
                sb.append(row.get(col));
                sb.append(",");
            }
            sb.append('\n');
        }
        if (result.hasGrandTotals()) {
            sb.append("======\n");
            for (PivotDataSource.PivotColumn<T> col : result.columns) {
                sb.append(result.getGrandTotal(col));
                sb.append(",");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
